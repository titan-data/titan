/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.titan.Version
import io.titandata.titan.clients.Docker
import io.titandata.titan.utils.CommandExecutor
import io.titandata.titan.utils.ProgressTracker

class Install (
    private val titanServerVersion: String,
    private val dockerRegistryUrl: String,
    private val verbose: Boolean = false,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
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
                docker.rm("titan-server", true)
            }
        }
        if (docker.titanLaunchIsAvailable()) {
            track( "Removing stale titan-launch container") {
                docker.rm("titan-launch", true)
            }
        }
        track("Starting titan server docker containers") {
            docker.launchTitanServers()
        }

        docker.fetchLogs("titan-launch")
        var finished = false
        var output = false
        loop@ while(!finished) {
            for (item in docker.logs) {
                if (!item.value) {
                    val line = item.key
                    if (verbose && output && !line.contains("TITAN", false)) {
                        println(line)
                    }
                    if (line.contains("TITAN START", false)) {
                        println(line.replace("TITAN START", "").removeRange(0..20))
                        output = true
                    }
                    if (line.contains("TITAN END", false)) {
                        output = false
                    }
                    if (line.contains("TITAN FINISHED")) {
                        finished = true
                        break@loop
                    }
                    docker.logs[line] = true
                }
            }
            Thread.sleep(2000)
            docker.fetchLogs("titan-launch")
        }
        println("Titan cli successfully installed, happy data versioning :)")
    }
}
