/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.titan.clients.Docker
import io.titandata.titan.utils.CommandExecutor

class CheckInstall (
    private val exit: (message: String, code: Int) -> Unit,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor)
) {

    fun checkInstall() {
        try {
            docker.version()
        } catch (e: Exception) {
            exit("Docker not found, install docker and run 'install' to configured required infrastructure", 1)
        }
        if (!docker.titanIsDownloaded()) {
            exit("Titan is not configured, run 'install' to configure required infrastructure", 1)
        }
        if (!docker.titanLaunchIsAvailable()) {
            exit("Titan is not configured, run 'install' to configure required infrastructure", 1)
        }
    }
}