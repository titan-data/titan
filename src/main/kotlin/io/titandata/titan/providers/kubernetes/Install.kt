/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.kubernetes

import io.titandata.titan.Version
import io.titandata.titan.clients.Docker
import io.titandata.titan.providers.Kubernetes
import io.titandata.titan.utils.CommandExecutor
import io.titandata.titan.utils.ProgressTracker

class Install (
        private val titanServerVersion: String,
        private val dockerRegistryUrl: String,
        private val verbose: Boolean = false,
        private val commandExecutor: CommandExecutor = CommandExecutor(),
        private val docker: Docker = Docker(commandExecutor, Kubernetes.Identity),
        private val track: (title: String, function: () -> Any) -> Unit = ProgressTracker()::track
) {
    fun install() {
        println("Initializing titan infrastructure ...")
        track("Checking docker installation") { docker.version() }
        if (!docker.titanLatestIsDownloaded(Version.fromString(titanServerVersion))) {
            track("Pulling titan docker image (may take a while)") {
                docker.pull("${dockerRegistryUrl}/titan:$titanServerVersion")
            }
            docker.tag("${dockerRegistryUrl}/titan:$titanServerVersion", "titan:$titanServerVersion")
            docker.tag("${dockerRegistryUrl}/titan:$titanServerVersion", "titan")
        }
        if (docker.titanServerIsAvailable()) {
            track("Removing stale titan-server container") {
                docker.rm("${docker.identity}-server", true)
            }
        }
        track("Starting titan server docker containers") {
            docker.launchTitanKubernetesServers()
        }

        println("Titan cli successfully installed, happy data versioning :)")
    }
}
