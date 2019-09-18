/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.RepositoriesApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.utils.CommandExecutor

class Uninstall (
    private val exit: (message: String, code: Int) -> Unit,
    private val remove: (container: String, force: Boolean) -> Unit,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi()
) {
    fun uninstall(force: Boolean) {
        if (docker.titanServerIsAvailable()) {
            val repositories = repositoriesApi.listRepositories()
            for (repo in repositories) {
                if (!force) {
                    exit("repository ${repo.name} exists, remove first or use '-f'", 1)
                }
                remove(repo.name, true)
            }
        }
        if (docker.titanServerIsAvailable()) docker.rm("titan-server", true)
        if (docker.titanLaunchIsAvailable()) docker.rm("titan-launch", true)
        docker.teardownTitanServers()
        try {
            docker.removeVolume("titan-modules")
        } catch (e: CommandException) {}
        try {
            docker.removeVolume("titan-data")
        } catch (e: CommandException) {}
        println("Uninstalled titan infrastructure")
    }
}