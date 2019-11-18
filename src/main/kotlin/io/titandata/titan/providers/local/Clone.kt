/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.RemotesApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.models.Commit
import io.titandata.titan.clients.Docker
import io.titandata.titan.clients.Docker.Companion.runtimeToArguments
import io.titandata.models.Repository
import io.titandata.titan.utils.CommandExecutor
import io.titandata.serialization.RemoteUtil
import io.titandata.titan.exceptions.CommandException
import kotlin.system.exitProcess

class Clone (
    private val remoteAdd: (container:String, uri: String, remoteName: String?, params: Map<String, String>) -> Unit,
    private val pull: (container: String, commit: String?, remoteName: String?, tags: List<String>, metadataOnly: Boolean) -> Unit,
    private val checkout: (container: String, hash: String?, tags: List<String>) -> Unit,
    private val run: (arguments: List<String>, createRepo: Boolean) -> Unit,
    private val remove: (container: String, force: Boolean) -> Unit,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
    private val remotesApi: RemotesApi = RemotesApi(),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
    private val remoteUtil: RemoteUtil = RemoteUtil()
) {
    fun clone(uri: String, container: String?, guid: String?, params: Map<String, String>) {
        val repoName = when(container){
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
            try {
                docker.inspectImage(commit.properties["container"] as String)
            } catch (e: CommandException) {
                try{
                    docker.pull(commit.properties["container"] as String)
                } catch (e: CommandException) {
                    throw CommandException(
                            "Unable to find image ${commit.properties["container"]} for ${commit.properties["repoTags"]}",
                            e.exitCode,
                            e.output
                    )
                }
                docker.pull(commit.properties["container"] as String)
            }
            val runtime = commit.properties["runtime"] as String
            val arguments = runtime.runtimeToArguments().toMutableList()
            arguments[arguments.indexOf("--name") + 1] = repoName
            arguments.add(commit.properties["container"] as String)
            run(arguments, false)
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