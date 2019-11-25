/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.utils.CommandExecutor

class Checkout(
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
    private val commitsApi: CommitsApi = CommitsApi(),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi()
) {
    fun checkout(container: String, guid: String?, tags: List<String>) {
        val sourceCommit = if (guid == null) {
            if (tags.isNotEmpty()) {
                val commits = commitsApi.listCommits(container, tags)
                if (commits.size == 0) {
                    throw IllegalStateException("no matching commits found")
                }
                commits.first().id
            } else {
                val status = repositoriesApi.getRepositoryStatus(container)
                if (status.sourceCommit == null) {
                    throw IllegalStateException("no commits present, run 'titan commit' first")
                }
                status.sourceCommit!!
            }
        } else {
            if (tags.isNotEmpty()) {
                throw IllegalArgumentException("tags and commit cannot both be specified")
            }
            guid
        }

        println("Stopping container $container")
        docker.stop(container)
        println("Checkout $sourceCommit")
        commitsApi.checkoutCommit(container, sourceCommit)
        println("Starting container $container")
        docker.start(container)
        println("$sourceCommit checked out")
    }
}
