/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.VolumesApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.utils.CommandExecutor
import org.json.JSONObject

class Cp(
    private val exit: (message: String, code: Int) -> Unit,
    private val start: (container: String) -> Unit,
    private val stop: (container: String) -> Unit,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
    private val volumeApi: VolumesApi = VolumesApi()
) {
    fun cp(container: String, driver: String, source: String, path: String) {
        var mutablePath = path
        val containerInfo = docker.inspectContainer(container)
        if (containerInfo == null) {
            exit("Container information is not available", 1)
        }
        val running = containerInfo!!.getJSONObject("State").getBoolean("Running")
        if (running) {
            stop(container)
        }
        val mounts = containerInfo.getJSONObject("HostConfig").optJSONArray("Mounts")
        if (mounts.count() > 1 && mutablePath.isEmpty()) {
            exit("$container has more than 1 volume mount. --path is required.", 1)
        }
        if (mutablePath.isEmpty()) {
            val mount = mounts[0] as JSONObject
            mutablePath = mount.getString("Target")
        }
        for (item in mounts) {
            val mount = item as JSONObject
            if (mount.optString("Target") == mutablePath) {
                val volumeName = mount.optString("Source")
                println("Copying data to $volumeName")
                volumeApi.activateVolume(container, volumeName)
                /*
                TODO add multiple cp sources
                when(driver) {
                    else -> docker.cp(source.removeSuffix("/"), volumeName)

                }
                */
                docker.cp(source.removeSuffix("/"), volumeName)
                volumeApi.deactivateVolume(container, volumeName)
            }
        }
        if (running) {
            start(container)
        }
        println("$container running with data from $source")
    }
}
