/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.OperationsApi
import io.titandata.client.apis.RemotesApi
import io.titandata.client.infrastructure.ClientException
import io.titandata.models.*
import io.titandata.serialization.RemoteUtil
import io.titandata.titan.utils.OperationMonitor

class Pull (
        private val exit: (message: String, code: Int) -> Unit,
        private val remotesApi: RemotesApi = RemotesApi(),
        private val operationsApi: OperationsApi = OperationsApi(),
        private val remoteUtil: RemoteUtil = RemoteUtil()
) {

    fun pull(container: String, guid: String?, remoteName: String?, tags: List<String>, metadataOnly: Boolean) {
        val name = remoteName ?: "origin"
        val remotes = remotesApi.listRemotes(container)
        if(remotes.isEmpty()) {
            exit("remote is not set, run 'remote add' first", 1)
        }

        val remote = remotesApi.getRemote(container, name)
        var commit = io.titandata.models.Commit("id", emptyMap())
        if (guid != null) {
            if (!tags.isEmpty()) {
                exit("tags cannot be specified when commit is also specified", 1)
            }
            commit = remotesApi.getRemoteCommit(container, remote.name, guid, remoteUtil.getParameters(remote))
        } else {
            val remoteCommits = remotesApi.listRemoteCommits(container, remote.name, remoteUtil.getParameters(remote),
                    tags)
            if (remoteCommits.isEmpty()) {
                exit("no matching commits found in remote, unable to pull latest", 1)
            }
            commit = remoteCommits.first()
        }
        if(commit.id == "id") {
            exit("remote commit not found", 1)
        }
        var operation = operationsApi.pull(container, remote.name, commit.id, remoteUtil.getParameters(remote),
                metadataOnly)
        if (!OperationMonitor(container, operation).monitor()) {
            exit("", 1)
        }
    }
}