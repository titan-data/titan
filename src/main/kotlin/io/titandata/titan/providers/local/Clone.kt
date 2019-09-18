/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.RemotesApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.clients.Docker.Companion.runtimeToArguments
import io.titandata.models.Repository
import io.titandata.titan.utils.CommandExecutor
import io.titandata.serialization.RemoteUtil

class Clone (
    private val remoteAdd: (container:String, uri: String, remoteName: String?) -> Unit,
    private val pull: (container: String, commit: String?, remoteName: String?) -> Unit,
    private val checkout: (container: String, hash: String) -> Unit,
    private val run: (arguments: List<String>, createRepo: Boolean) -> Unit,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
    private val remotesApi: RemotesApi = RemotesApi(),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
    private val remoteUtil: RemoteUtil = RemoteUtil()
) {
    fun clone(uri: String, container: String?) {
        val repoName = when(container){
            null -> uri.split("/").last()
            else -> container
        }
        val repository = Repository(repoName, emptyMap())
        repositoriesApi.createRepository(repository)
        remoteAdd(repoName, uri, null)
        val remote = remotesApi.getRemote(repoName, "origin")
        val remoteCommits = remotesApi.listRemoteCommits(repoName, remote.name, remoteUtil.getParameters(remote))
        val commit = remoteCommits.first()
        docker.pull(commit.properties["container"] as String)
        val runtime = commit.properties["runtime"] as String
        val arguments = runtime.runtimeToArguments().toMutableList()
        arguments[arguments.indexOf("--name") + 1] = repoName
        arguments.add(commit.properties["container"] as String)
        run(arguments, false)
        pull(repoName, commit.id, null)
        checkout(repoName, commit.id)
    }
}