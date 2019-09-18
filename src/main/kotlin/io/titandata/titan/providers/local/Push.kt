/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.RemotesApi
import io.titandata.client.apis.OperationsApi
import io.titandata.client.infrastructure.ClientException
import io.titandata.models.*
import io.titandata.models.Commit
import io.titandata.serialization.RemoteUtil

class Push (
    private val exit: (message: String, code: Int) -> Unit,
    private val commitsApi: CommitsApi = CommitsApi(),
    private val remotesApi: RemotesApi = RemotesApi(),
    private val operationsApi: OperationsApi = OperationsApi(),
    private val remoteUtil: RemoteUtil = RemoteUtil()
) {
    fun push(container: String, guid: String?, remoteName: String?) {
        val name = remoteName ?: "origin"
        val remotes = remotesApi.listRemotes(container)
        if(remotes.isEmpty()) {
            exit("remote is not set, run 'remote add' first", 1)
        }

        val commits = commitsApi.listCommits(container)
        if (commits.isEmpty()) {
            exit("container has no history, run 'commit' to first commit state",1)
        }
        var commit: Commit = io.titandata.models.Commit("id", emptyMap())
        if (!guid.isNullOrEmpty()) {
            try{
                commit =  commitsApi.getCommit(container, guid!!)
            } catch (e: ClientException) {
                exit(e.message!!, 1)
            }
        } else {
            commit = commits.last()
        }
        val remote = remotesApi.getRemote(container, name)
        var operation = Operation("id", Operation.Type.PUSH, Operation.State.RUNNING, remote.name, commit.id)
        try {
            operation = operationsApi.push(container, remote.name, commit.id, remoteUtil.getParameters(remote))
        } catch (e: ClientException) {
            exit(e.message!!,1)
        }
        println("${operation.type} ${operation.commitId} to ${operation.remote} ${operation.state}")
        var padLen = 0
        while (operation.state == Operation.State.RUNNING){
            val statuses = operationsApi.getProgress(container, operation.id)
            for (status in statuses) {
                if (status.type != ProgressEntry.Type.PROGRESS) {
                    println(status.message)
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
        println("${operation.type} ${operation.commitId} to ${operation.remote} ${operation.state}")
        // TODO get appropriate source hash for clone !! Confirm beta issue
    }
}