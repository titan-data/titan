/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.progresstracker.PrintStreamProgressTracker
import io.titandata.progresstracker.ProgressTracker
import io.titandata.progresstracker.StringBasedApi.markAsFailed
import io.titandata.progresstracker.StringBasedApi.trackTask
import io.titandata.titan.Version
import io.titandata.titan.clients.Docker
import io.titandata.titan.utils.CommandExecutor
import kotlin.system.exitProcess

class Install (
    private val titanServerVersion: String,
    private val dockerRegistryUrl: String,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor)
) {

    private fun createProgressTracker(): ProgressTracker {
        io.titandata.progresstracker.ApplicationProgressTracker.progressTracker = PrintStreamProgressTracker(
                tasksPrefix = "\t",
                successMessage = "Titan cli successfully installed, happy data versioning :)",
                failureMessage = "Docker not found")
        return io.titandata.progresstracker.ApplicationProgressTracker.progressTracker
    }

    fun install() {
        createProgressTracker().use {
            println("Initializing titan infrastructure ...")
            try {
                "Checking docker installation".trackTask()
                docker.version()
            } catch (e: Exception) {
                "Checking docker installation".markAsFailed()
                exitProcess(2)
            }
            if(!docker.titanLatestIsDownloaded(Version.fromString(titanServerVersion))) {
                "Pulling titan docker image (may take a while)".trackTask()
                docker.pull("${dockerRegistryUrl}/titan:$titanServerVersion")
                "Tagging titan docker image".trackTask()
                docker.tag("${dockerRegistryUrl}/titan:$titanServerVersion", "titan:$titanServerVersion")
                "Tagging latest titan".trackTask()
                docker.tag("${dockerRegistryUrl}/titan:$titanServerVersion", "titan")
            }
            if (docker.titanServerIsAvailable()) {
                "Removing stale containers".trackTask()
                docker.rm("titan-server", true)
            }
            if (docker.titanLaunchIsAvailable()) {
                "Removing stale containers".trackTask()
                docker.rm("titan-launch", true)
            }
            "Starting titan server docker containers".trackTask()
            docker.launchTitanServers()
        }
    }
}
