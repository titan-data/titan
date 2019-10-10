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

class Clone (
    private val remoteAdd: (container:String, uri: String, remoteName: String?) -> Unit,
    private val pull: (container: String, commit: String?, remoteName: String?) -> Unit,
    private val checkout: (container: String, hash: String) -> Unit,
    private val run: (arguments: List<String>, createRepo: Boolean) -> Unit,
    private val remove: (container: String, force: Boolean) -> Unit,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
    private val remotesApi: RemotesApi = RemotesApi(),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
    private val remoteUtil: RemoteUtil = RemoteUtil()
) {
    fun clone(uri: String, container: String?, guid: String?) {
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
            remoteAdd(repoName, uri.substringBefore('#'), null)
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
                docker.pull(commit.properties["container"] as String)
            }
            val runtime = commit.properties["runtime"] as String
            val arguments = runtime.runtimeToArguments().toMutableList()
            arguments[arguments.indexOf("--name") + 1] = repoName
            arguments.add(commit.properties["container"] as String)
            run(arguments, false)
            pull(repoName, commit.id, null)
            checkout(repoName, commit.id)
        } catch (e: CommandException) {
            println("Clone failed.")
            println(e.message)
            println(e.output)
            remove(repository.name, true)
        }
    }
}