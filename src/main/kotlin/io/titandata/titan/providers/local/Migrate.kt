/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.apis.VolumeApi
import io.titandata.titan.clients.Docker
import io.titandata.models.Repository
import io.titandata.models.VolumeMountRequest
import io.titandata.titan.utils.CommandExecutor
import org.json.JSONArray
import org.json.JSONObject

class Migrate (
        private val exit: (message: String, code: Int) -> Unit,
        private val commit: (container: String, message: String) -> Unit,
        private val commandExecutor: CommandExecutor = CommandExecutor(),
        private val docker: Docker = Docker(commandExecutor),
        private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
        private val volumeApi: VolumeApi = VolumeApi()
) {

    private fun getLocalSrcFromPath(path: String, containerInfo: JSONObject): String {
        var returnString = ""
        for (item in containerInfo.getJSONArray("Mounts")) {
            val mount = item as JSONObject
            if (mount.getString("Destination") == path) {
                returnString = mount.getString("Source")
            }
        }
        return returnString
    }

    fun migrate(container: String, name: String) {
        val containerInfo = docker.inspectContainer(container)
        if (containerInfo == null) {
            exit("Container information is not available",1)
        }
        if(containerInfo!!.getJSONObject("State").getBoolean("Running")) {
            exit("Cannot migrate a running container. Please stop $container",1)
        }
        if(name.contains("/")) {
            exit("Container name cannot contain a slash",1)
        }
        val image = containerInfo.getString("Image")
        val imageInfo = docker.inspectImage(image)
        if (imageInfo == null) {
            exit("Image information is not available",1)
        }
        val volumes = imageInfo!!.getJSONObject("Config").optJSONObject("Volumes")
        if (volumes == null) {
            exit("No volumes found for image $image", 1)
        }
        println("Creating repository $name")
        val arguments = mutableListOf("-d","--label","io.titandata.titan")
        val repo = Repository(name, emptyMap())
        repositoriesApi.createRepository(repo)
        var i = 0
        for (path in volumes.keys()) {
            val volumeName = "$name/v$i"
            println("Creating docker volume $volumeName with path $path")
            docker.createVolume(volumeName, path)
            val localSrc = getLocalSrcFromPath(path, containerInfo as JSONObject)
            if (localSrc.isNotEmpty()) {
                println("Copying data to $volumeName")
                val volMountRequest = VolumeMountRequest(volumeName, "")
                volumeApi.mountVolume(volMountRequest)
                docker.cp(localSrc, volumeName)
                volumeApi.unmountVolume(volMountRequest)
            }
            arguments.add("--mount")
            arguments.add("type=volume,src=$volumeName,dst=$path,volume-driver=titan")
            i++
        }

        val ports = containerInfo.getJSONObject("HostConfig").optJSONObject("PortBindings")
        for (item in ports.keys()) {
            val port = ports[item] as JSONArray
            val host = port[0] as JSONObject
            val containerPort = item.split("/")[0]
            arguments.add("-p")
            if (host.optString("HostIp").isNotEmpty()){
                arguments.add("${host.optString("HostIp")}:${host.getString("HostPort")}:$containerPort")
            } else {
                arguments.add("${host.getString("HostPort")}:$containerPort")
            }
        }
        arguments.add("--name")
        arguments.add(name)

        val repoDigest = imageInfo.getJSONArray("RepoDigests")[0] as String
        val metadata = mapOf(
                "container" to repoDigest,
                "runtime" to arguments.toString()
        )

        //TODO check for arguments to run validation since switch to array

        val updateRepo = Repository(name, metadata)
        repositoriesApi.updateRepository(name, updateRepo)
        docker.run(image, "", arguments)
        commit(name, "Initial Migration")
        println("$container migrated to controlled environment $name")
    }
}