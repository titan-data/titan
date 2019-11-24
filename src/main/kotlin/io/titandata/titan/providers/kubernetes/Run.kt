/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.kubernetes

import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.apis.VolumesApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.clients.Docker.Companion.fetchName
import io.titandata.titan.clients.Docker.Companion.hasDetach
import io.titandata.models.Repository
import io.titandata.models.Volume
import io.titandata.titan.clients.Kubernetes
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.utils.CommandExecutor
import org.json.JSONObject

class Run (
    private val exit: (message: String, code: Int) -> Unit,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
    private val kubernetes: Kubernetes = Kubernetes(),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
    private val volumesApi: VolumesApi = VolumesApi()
) {
    fun run(
            container: String,
            repository: String?,
            environment: List<String>,
            arguments: List<String>,
            disablePortMapping: Boolean,
            createRepo: Boolean = true
    ) {
        if (!arguments.isEmpty()) {
            exit("kubernetes provider doesn't support additional arguments", 1)
        }

        if(!repository.isNullOrEmpty() && repository.contains("/")) {
            exit("Repository name cannot contain a slash",1)
        }

        val repoName = when {
            repository.isNullOrEmpty() -> container
            else -> repository
        }
        val image = when{
            container.contains(":") -> container.split(":")[0]
            else -> container
        }
        val tag =  when {
            container.contains(":") -> container.split(":")[1]
            else -> "latest"
        }

        var imageInfo: JSONObject? = null
        try {
            imageInfo = docker.inspectImage("$image:$tag")
        } catch (e: CommandException) {
            docker.pull("$image:$tag")
            imageInfo = docker.inspectImage("$image:$tag")
        }
        if (imageInfo == null) {
            exit("Image information is not available",1)
        }
        val volumes = imageInfo!!.getJSONObject("Config").optJSONObject("Volumes")
        if (volumes == null) {
            exit("No volumes found for image $image",1)
        }

        println("Creating repository $repoName")
        val repo = Repository(repoName, emptyMap())
        if (createRepo) {
            repositoriesApi.createRepository(repo)
        }

        val titanVolumes = mutableListOf<Volume>()
        val metaVols = mutableListOf<Map<String, String>>()
        try {
            for ((index, path) in volumes.keys().withIndex()) {
                val volumeName = "v$index"
                println("Creating titan volume $volumeName with path $path")

                titanVolumes.add(volumesApi.createVolume(repoName, Volume(name = volumeName, properties = mapOf("path" to path))))

                val addVol = mapOf(
                        "name" to "v$index",
                        "path" to path
                )
                metaVols.add(addVol)
            }

            println("Waiting for volumes to be ready")
            var ready = false
            while (!ready) {
                ready = true
                for (volume in titanVolumes) {
                    val status = volumesApi.getVolumeStatus(repoName, volume.name)
                    if (!status.ready) {
                        ready = false
                        break
                    }
                    if (status.error != null) {
                        throw Exception("Error creating volume ${volume.properties["path"]}: ${status.error}")
                    }
                }
            }

            val repoDigest = imageInfo.optJSONArray("RepoDigests").optString(0)
            val imageId = if(repoDigest.isNullOrEmpty()) {
                "$image:$tag"
            } else  {
                repoDigest
            }
            val metadata = mapOf(
                    "container" to imageId,
                    "image" to image,
                    "tag" to tag,
                    "digest" to repoDigest,
                    "runtime" to emptyArray<String>()
            )
            val updateRepo = Repository(repoName, metadata)
            repositoriesApi.updateRepository(repoName, updateRepo)

        } catch (t: Throwable) {
            for (volume in titanVolumes) {
                volumesApi.deleteVolume(repoName, volume.name)
            }
            throw t
        }

        val metaPorts = mutableListOf<Map<String, String>>()
        val exposedPorts = imageInfo.getJSONObject("Config").optJSONObject("ExposedPorts")
        val ports = mutableListOf<Int>()
        for (rawPort in exposedPorts.keys()) {
            val port = rawPort.split("/")[0]
            val protocol = rawPort.split("/")[1]
            ports.add(port.toInt())
            val addPorts = mapOf(
                    "protocol" to protocol,
                    "port" to port
            )
            metaPorts.add(addPorts)
        }

        val repoDigest = imageInfo.optJSONArray("RepoDigests").optString(0)
        val containerImage = if(repoDigest.isNullOrEmpty()) {
            "$image:$tag"
        } else  {
            repoDigest
        }
        val metadata = mapOf(
                "disablePortMapping" to disablePortMapping,
                "v2" to mapOf(
                        "image" to mapOf(
                                "image" to image,
                                "tag" to tag,
                                "digest" to repoDigest
                        ),
                        "environment" to environment,
                        "ports" to metaPorts,
                        "volumes" to metaVols
                )
        )
        val updateRepo = Repository(repoName, metadata)
        repositoriesApi.updateRepository(repoName, updateRepo)

        println("Creating $repoName deployment")
        kubernetes.createStatefulSet(repoName, containerImage, ports, titanVolumes, environment)

        println("Waiting for deployment to be ready")
        kubernetes.waitForStatefulSet(repoName)

        if (!disablePortMapping) {
            println("Forwarding local ports")
            kubernetes.startPortForwarding(repoName)
        }
    }
}