/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.apis.VolumeApi
import io.titandata.titan.clients.Docker
import io.titandata.models.VolumeMountRequest
import io.titandata.models.VolumeRequest
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.utils.CommandExecutor
import java.lang.Exception

class Remove (
        private val exit: (message: String, code: Int) -> Unit,
        private val commandExecutor: CommandExecutor = CommandExecutor(),
        private val docker: Docker = Docker(commandExecutor),
        private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
        private val volumeApi: VolumeApi = VolumeApi()
) {
    fun remove(container: String, force: Boolean) {
        try {
            val containerInfo = docker.inspectContainer(container)
            if (containerInfo != null) {
                if (!force) {
                    if (containerInfo.getJSONObject("State").getString("Status") == "running") {
                        exit("container $container is running, stop or use '-f' to force", 1)
                    }
                }
                println("Removing container $container")
                if (docker.containerIsRunning(container)) {
                    docker.rm(container, force)
                } else  {
                    docker.rmStopped(container)
                }
            }
        } catch (e: Exception) { }
        for (volume in volumeApi.listVolumes().volumes) {
            val name = volume.name.split("/")[0]
            if (name == container) {
                println("Deleting volume ${volume.name}")
                val volMountRequest = VolumeMountRequest(volume.name)
                volumeApi.unmountVolume(volMountRequest)
                val volRequest = VolumeRequest(volume.name)
                try {
                    docker.removeVolume(volume.name, force)
                } catch (e: CommandException) {
                    /**
                     Docker will sometimes fail to launch a container after the
                     volume has been created. The container does not exist, but
                     docker thinks the volume is attached to a container and does
                     not allow it to be removed. Falling back on the VolumeApi
                     fixes this condition.
                     */
                    volumeApi.removeVolume(volRequest)
                }
            }
        }
        repositoriesApi.deleteRepository(container)
        println("$container removed")
    }
}