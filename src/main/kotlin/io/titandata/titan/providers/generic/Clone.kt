/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.generic

import io.titandata.client.apis.RemotesApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.models.Commit
import io.titandata.models.Repository
import io.titandata.serialization.RemoteUtil
import io.titandata.titan.clients.Docker
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.providers.Metadata
import io.titandata.titan.utils.CommandExecutor
import kotlin.system.exitProcess

class Clone(
    private val remoteAdd: (container: String, uri: String, remoteName: String?, params: Map<String, String>) -> Unit,
    private val pull: (container: String, commit: String?, remoteName: String?, tags: List<String>, metadataOnly: Boolean) -> Unit,
    private val checkout: (container: String, hash: String?, tags: List<String>) -> Unit,
    private val run: (container: String, repository: String?, environments: List<String>, arguments: List<String>, disablePortMapping: Boolean, createRepo: Boolean) -> Unit,
    private val remove: (container: String, force: Boolean) -> Unit,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
    private val remotesApi: RemotesApi = RemotesApi(),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
    private val remoteUtil: RemoteUtil = RemoteUtil()
) {
    fun clone(uri: String, container: String?, guid: String?, params: Map<String, String>, arguments: List<String>, disablePortMapping: Boolean) {
        val repoName = when (container) {
            null -> uri.split("/").last().substringBefore('#')
            else -> container
        }
        val commitId = when {
            guid.isNullOrEmpty() && uri.contains('#') -> uri.split("#").last()
            else -> guid
        }
        val repository = Repository(repoName, emptyMap())
        try {
            repositoriesApi.createRepository(repository)
            remoteAdd(repoName, uri.substringBefore('#'), null, params)
            val remote = remotesApi.getRemote(repoName, "origin")
            var commit = Commit("id", emptyMap())
            if (commitId.isNullOrEmpty()) {
                val remoteCommits = remotesApi.listRemoteCommits(repoName, remote.name, remoteUtil.getParameters(remote))
                commit = remoteCommits.first()
            } else {
                commit = remotesApi.getRemoteCommit(repoName, remote.name, commitId, remoteUtil.getParameters(remote))
            }
            val metadata = Metadata.load(commit.properties)
            try {
                docker.inspectImage(metadata.image.digest)
            } catch (e: CommandException) {
                try {
                    docker.pull(metadata.image.digest)
                } catch (e: CommandException) {
                    throw CommandException(
                            "Unable to find image ${metadata.image.digest} for ${metadata.image.image}",
                            e.exitCode,
                            e.output
                    )
                }
                docker.pull(metadata.image.digest)
            }
            run(metadata.image.digest, repoName, metadata.environment, arguments, disablePortMapping, false)
            pull(repoName, commit.id, null, listOf(), false)
            checkout(repoName, commit.id, listOf())
        } catch (e: CommandException) {
            println(e.message)
            println(e.output)
            remove(repository.name, true)
            exitProcess(1)
        }
    }
}
