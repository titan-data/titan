/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.RepositoriesApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.utils.CommandExecutor
import io.titandata.titan.utils.ProgressTracker

class Uninstall (
    private val titanServerVersion: String,
    private val exit: (message: String, code: Int) -> Unit,
    private val remove: (container: String, force: Boolean) -> Unit,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
    private val track: (title: String, function: () -> Any) -> Unit = ProgressTracker()::track
) {
    fun uninstall(force: Boolean) {
        if (docker.titanServerIsAvailable()) {
            val repositories = repositoriesApi.listRepositories()
            for (repo in repositories) {
                if (!force) {
                    exit("repository ${repo.name} exists, remove first or use '-f'", 1)
                }
                track("Removing repository ${repo.name}" ) { remove(repo.name, true) }
            }
        }
        if (docker.titanServerIsAvailable()) docker.rm("titan-server", true)
        if (docker.titanLaunchIsAvailable()) docker.rm("titan-launch", true)
        track ("Tearing down Titan servers") {
            docker.teardownTitanServers()
        }
        track ("Removing titan-data Docker volume") {
            docker.removeVolume("titan-data")
        }
        track ("Removing Titan Docker image") {
            docker.removeTitanImages(titanServerVersion)
        }
        println("Uninstalled titan infrastructure")
    }
}