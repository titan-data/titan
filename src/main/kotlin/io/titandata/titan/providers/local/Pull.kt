/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.OperationsApi
import io.titandata.client.apis.RemotesApi
import io.titandata.client.infrastructure.ClientException
import io.titandata.models.*
import io.titandata.serialization.RemoteUtil

class Pull (
        private val exit: (message: String, code: Int) -> Unit,
        private val remotesApi: RemotesApi = RemotesApi(),
        private val operationsApi: OperationsApi = OperationsApi(),
        private val remoteUtil: RemoteUtil = RemoteUtil()
) {

    fun pull(container: String, guid: String?, remoteName: String?) {
        val name = remoteName ?: "origin"
        val remotes = remotesApi.listRemotes(container)
        if(remotes.isEmpty()) {
            exit("remote is not set, run 'remote add' first", 1)
        }

        val remote = remotesApi.getRemote(container, name)
        var commit = io.titandata.models.Commit("id", emptyMap())
        try {
            commit = remotesApi.getRemoteCommit(container, remote.name, guid!!, remoteUtil.getParameters(remote))
        } catch (e: kotlin.KotlinNullPointerException) {
            val remoteCommits = remotesApi.listRemoteCommits(container, remote.name, remoteUtil.getParameters(remote))
            if (remoteCommits.isEmpty()) {
                exit("no commits found in remote, unable to pull latest", 1)
            }
            commit = remoteCommits.first()

        } catch (e: ClientException) {
            exit(e.message!!, 1)
        }
        if(commit.id == "id") {
            exit("remote commit not found", 1)
        }
        var operation = Operation("id", Operation.Type.PULL, Operation.State.RUNNING, remote.name, commit.id)
        try {
            operation = operationsApi.pull(container, remote.name, commit.id, remoteUtil.getParameters(remote))
        } catch (e: ClientException) {
            exit(e.message!!,1)
        }
        println("${operation.type} ${operation.commitId} from ${operation.remote} ${operation.state}")
        var padLen = 0
        while (operation.state == Operation.State.RUNNING){
            val statuses = operationsApi.getProgress(container, operation.id)
            for (status in statuses) {
                if (status.type != ProgressEntry.Type.PROGRESS) {
                    if (!status.message.isNullOrEmpty()) {
                        println(status.message)
                    }
                } else {
                    val subMessage = status.message as String
                    if (subMessage.length > padLen) {
                        padLen = subMessage.length
                    }
                    System.out.printf("\r%s", subMessage.padEnd((padLen - subMessage.length) + 1, ' '))
                }
            }
            Thread.sleep(2000)
            operation = operationsApi.getOperation(container, operation.id)
        }
        println("${operation.type} ${operation.commitId} from ${operation.remote} ${operation.state}")
    }
}