/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.clients

import io.kubernetes.client.custom.V1Patch
import io.kubernetes.client.openapi.ApiException
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1ContainerBuilder
import io.kubernetes.client.openapi.models.V1ContainerPortBuilder
import io.kubernetes.client.openapi.models.V1EnvVarBuilder
import io.kubernetes.client.openapi.models.V1LabelSelectorBuilder
import io.kubernetes.client.openapi.models.V1ObjectMetaBuilder
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSourceBuilder
import io.kubernetes.client.openapi.models.V1PodSpecBuilder
import io.kubernetes.client.openapi.models.V1PodTemplateSpecBuilder
import io.kubernetes.client.openapi.models.V1ServiceBuilder
import io.kubernetes.client.openapi.models.V1ServicePortBuilder
import io.kubernetes.client.openapi.models.V1ServiceSpecBuilder
import io.kubernetes.client.openapi.models.V1StatefulSetBuilder
import io.kubernetes.client.openapi.models.V1StatefulSetSpecBuilder
import io.kubernetes.client.openapi.models.V1VolumeBuilder
import io.kubernetes.client.openapi.models.V1VolumeMountBuilder
import io.kubernetes.client.util.ClientBuilder
import io.kubernetes.client.util.Config
import io.titandata.models.Volume
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.utils.CommandExecutor

class Kubernetes() {
    private val executor = CommandExecutor()
    private var coreApi: CoreV1Api
    private var appsApi: AppsV1Api
    private var appsApiPatch: AppsV1Api
    private val defaultNamespace = "default"

    init {
        val client = Config.defaultClient()
        Configuration.setDefaultApiClient(client)
        coreApi = CoreV1Api()
        appsApi = AppsV1Api()

        val jsonPatchClient = ClientBuilder.standard().setOverridePatchFormat(V1Patch.PATCH_FORMAT_JSON_PATCH).build()
        appsApiPatch = AppsV1Api(jsonPatchClient)
    }

    /**
     * For our repositories, we keep it very simple. There is a single headless service that is responsible for exposing
     * the ports in the container. We then create a single replica stateful set with the given volumes (each with
     * existing PVCs) mapped in.
     */
    fun createStatefulSet(
        repoName: String,
        imageId: String,
        ports: List<Int>,
        volumes: List<Volume>,
        environment: List<String>
    ) {
        val metadata = V1ObjectMetaBuilder()
                .withName(repoName)
                .withLabels(mapOf("titanRepository" to repoName))
                .build()

        coreApi.createNamespacedService(defaultNamespace, V1ServiceBuilder()
                .withMetadata(metadata)
                .withSpec(V1ServiceSpecBuilder()
                        .withClusterIP("None")
                        .withSelector(mapOf("titanRepository" to repoName))
                        .withPorts(
                                ports.map { V1ServicePortBuilder().withPort(it).withName("port-$it").build() })
                        .build())
                .build(), null, null, null)

        appsApi.createNamespacedStatefulSet(defaultNamespace, V1StatefulSetBuilder()
                .withMetadata(metadata)
                .withSpec(V1StatefulSetSpecBuilder()
                        .withReplicas(1)
                        .withServiceName(repoName)
                        .withSelector(V1LabelSelectorBuilder().withMatchLabels(mapOf("titanRepository" to repoName)).build())
                        .withTemplate(V1PodTemplateSpecBuilder()
                                .withMetadata(metadata)
                                .withSpec(V1PodSpecBuilder()
                                        .withContainers(V1ContainerBuilder()
                                                .withName(repoName)
                                                .withImage(imageId)
                                                .withPorts(ports.map { V1ContainerPortBuilder().withContainerPort(it).withName("port-$it").build() })
                                                .withVolumeMounts(volumes.map { V1VolumeMountBuilder().withName(it.name).withMountPath(it.properties["path"] as String).build() })
                                                .withEnv(environment.map { V1EnvVarBuilder().withName(it.substringBefore("=")).withValue(it.substringAfter("=")).build() })
                                                .build())
                                        .withVolumes(volumes.map { V1VolumeBuilder()
                                                .withName(it.name)
                                                .withPersistentVolumeClaim(V1PersistentVolumeClaimVolumeSourceBuilder()
                                                        .withClaimName(it.config["pvc"] as String)
                                                        .build())
                                                .build() })
                                        .build())
                                .build())
                        .build())
                .build(), null, null, null)
    }

    fun deleteStatefulSpec(repoName: String) {
        try {
            appsApi.deleteNamespacedStatefulSet(repoName, defaultNamespace, null, null, 0, null, null, null)
        } catch (e: ApiException) {
            if (e.code != 404) {
                throw e
            }
        }
        try {
            coreApi.deleteNamespacedService(repoName, defaultNamespace, null, null, 0, null, null, null)
        } catch (e: ApiException) {
            if (e.code != 404) {
                throw e
            }
        }
    }

    /**
     * Gets the status of a stateful set. We use the following:
     *
     *      detached        No such statefulset present (user deleted it)
     *      updating        Update revision doesn't match current revision
     *      stopped         Number of replicas is 0
     *      running         Number of replicas and ready replicas is 1
     *      failed          Terminal condition prevented stateful set from starting
     *      starting        Number of replicas is 1 but ready replicas is 0
     *
     * We also return a pair, with the second element providing addtional context for the "failed" state
     */
    fun getStatefulSetStatus(repoName: String): Pair<String, String?> {
        try {
            var set = appsApi.readNamespacedStatefulSet(repoName, defaultNamespace, null, null, null)

            if (set.status?.updateRevision != set.status?.currentRevision) {
                return "updating" to null
            }

            if (set.status?.replicas == 0) {
                return "stopped" to null
            }

            if (set.status?.replicas == set.status?.readyReplicas) {
                return "running" to null
            }

            try {
                var pod = coreApi.readNamespacedPod("$repoName-0", defaultNamespace, null, null, null)
                val conditions = pod.status?.conditions
                if (conditions != null) {
                    for (condition in conditions) {
                        if (condition.reason == "Unschedulable") {
                            return "failed" to "Pod failed to be scheduled: ${condition.message}"
                        }
                    }
                }
            } catch (e: ApiException) {
                // Pod may not exist, yet
                if (e.code != 404) {
                    throw e
                }
            }

            return "starting" to null
        } catch (e: ApiException) {
            if (e.code == 404) {
                return "detached" to null
            } else {
                throw e
            }
        }
    }

    /**
     * Wait for the given statefulset to reach a terminal state (running or stopped), throwing an error if we've
     * reached the failed state.
     */
    fun waitForStatefulSet(repoName: String) {
        while (true) {
            val (status, error) = getStatefulSetStatus(repoName)
            if (status == "failed") {
                throw Exception(error)
            }
            if (status == "running" || status == "stopped") {
                break
            }
            Thread.sleep(1000L)
        }
    }

    /**
     * Forward port for a container. For now, we're using a temporary solution of launching 'kubectl-forward' in the
     * background. This is totally brittle, as the commands will fail in the background as pods are stopped and
     * connections broken. And if you restart the host system, there is no way to restart them. But it's a quick
     * hack to demonstrate the desired experience until we can build out a more full-featured port forwarder, such
     * as: https://github.com/pixel-point/kube-forwarder
     */
    fun startPortForwarding(repoName: String) {
        val service = coreApi.readNamespacedService(repoName, defaultNamespace, null, null, null)
        val ports = service.spec?.ports
        if (ports != null) {
            for (port in ports) {
                executor.exec(listOf("sh", "-c", "kubectl port-forward svc/$repoName ${port.port} > /dev/null 2>&1 &"))
            }
        }
    }

    /**
     * This is horribly OS-specific, and should be replaced with a more complete solution as described above.
     */
    fun stopPortFowarding(repoName: String) {
        val service = coreApi.readNamespacedService(repoName, defaultNamespace, null, null, null)
        val ports = service.spec?.ports
        if (ports != null) {
            for (port in ports) {
                try {
                    val output = executor.exec(listOf("sh", "-c", "ps -ef | grep \"[k]ubectl port-forward svc/$repoName ${port.port}\""))
                    val pid = output.split("\\s+".toRegex())[2]
                    executor.exec(listOf("kill", pid))
                } catch (e: CommandException) {
                    // Ignore errors in case port forwarding dies
                }
            }
        }
    }

    /**
     * Update the volumes within a given StatefulSet.
     */
    fun updateStatefulSetVolumes(repoName: String, volumes: List<Volume>) {
        val set = appsApi.readNamespacedStatefulSet(repoName, defaultNamespace, null, null, null)

        val patches = mutableListOf<String>()

        val specVolumes = set.spec?.template?.spec?.volumes
        if (specVolumes != null) {
            for ((volumeIdx, volumeDef) in specVolumes.withIndex()) {
                for (vol in volumes) {
                    if (vol.name == volumeDef.name) {
                        patches.add("{\"op\":\"replace\",\"path\":\"/spec/template/spec/volumes/$volumeIdx/persistentVolumeClaim/claimName\",\"value\":\"${vol.config["pvc"]}\"}")
                    }
                }
            }
        }
        val json = "[" + patches.joinToString(",") + "]"
        appsApiPatch.patchNamespacedStatefulSet(repoName, defaultNamespace, V1Patch(json), null, null, null, null)
    }

    /**
     * Stops a stateful set. This is equivalent to setting the number of replicas to zero and waiting for the
     * deployment to update. Its up to callers to wait for the changes to take effect.
     */
    fun stopStatefulSet(repoName: String) {
        val patch = "[{\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":0}]"
        appsApiPatch.patchNamespacedStatefulSet(repoName, defaultNamespace, V1Patch(patch), null, null, null, null)
    }

    /**
     * Opposite of the above, set the number of replicas to one.
     */
    fun startStatefulSet(repoName: String) {
        val patch = "[{\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":1}]"
        appsApiPatch.patchNamespacedStatefulSet(repoName, defaultNamespace, V1Patch(patch), null, null, null, null)
    }
}
