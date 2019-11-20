/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.generic

import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.RemotesApi
import io.titandata.client.apis.OperationsApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.serialization.RemoteUtil
import io.titandata.titan.utils.OperationMonitor

class Push (
    private val exit: (message: String, code: Int) -> Unit,
    private val commitsApi: CommitsApi = CommitsApi(),
    private val remotesApi: RemotesApi = RemotesApi(),
    private val operationsApi: OperationsApi = OperationsApi(),
    private val remoteUtil: RemoteUtil = RemoteUtil(),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi()
) {

    fun push(container: String, guid: String?, remoteName: String?, tags: List<String>, metadataOnly: Boolean) {
        val name = remoteName ?: "origin"
        val remotes = remotesApi.listRemotes(container)
        if(remotes.isEmpty()) {
            exit("remote is not set, run 'remote add' first", 1)
        }

        val repoStatus = repositoriesApi.getRepositoryStatus(container)
        if(repoStatus.lastCommit.isNullOrEmpty()) {
            exit("container has no history, run 'commit' to first commit state",1)
        }
        val commit = if (guid != null) {
            if (!tags.isEmpty()) {
                exit("tags cannot be specified when commit is also specified", 1)
            }
            guid
        } else {
            if (tags.isEmpty()) {
                commitsApi.getCommit(container, repoStatus.lastCommit!!).id
            } else {
                val commits = commitsApi.listCommits(container, tags)
                if (commits.isEmpty()) {
                    exit("no matching commits found, unable to push latest", 1)
                }
                commits.first().id
            }
        }

        val remote = remotesApi.getRemote(container, name)
        var operation = operationsApi.push(container, remote.name, commit, remoteUtil.getParameters(remote),
                metadataOnly)
        if (!OperationMonitor(container, operation).monitor()) {
            exit("", 1)
        }
    }
}