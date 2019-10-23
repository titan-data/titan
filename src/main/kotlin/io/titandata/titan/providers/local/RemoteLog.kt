/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.RemotesApi
import io.titandata.client.infrastructure.ClientException
import io.titandata.serialization.RemoteUtil
import io.titandata.remote.nop.NopRemote
import io.titandata.models.Remote

class RemoteLog (
        private val exit: (message: String, code: Int) -> Unit,
        private val remotesApi: RemotesApi = RemotesApi(),
        private val remoteUtil: RemoteUtil = RemoteUtil()
) {
    private val n = System.lineSeparator()
    
    private fun getRemotes(container: String, remoteName: String?): Array<Remote> {
        return try {
            when (remoteName.isNullOrBlank()) {
                true -> remotesApi.listRemotes(container)
                else -> arrayOf(remotesApi.getRemote(container, remoteName))
            }
        } catch (e: ClientException) {
            arrayOf(NopRemote( name="NOP"))
        }
    }

    fun remoteLog(container:String, remoteName: String?, tags: List<String>) {
        val remotes = getRemotes(container, remoteName)
        if(remotes.isEmpty()) {
            exit("remote is not set, run 'remote add' first", 1)
        }

        var first = true
        loop@ for (remote in remotes) {
            if (remote.provider == "nop") {
                break@loop
            }
            try {
                if (!first) {
                    println("")
                } else {
                    first = false
                }
                val commits = remotesApi.listRemoteCommits(container, remote.name, remoteUtil.getParameters(remote), tags)
                for (commit in commits) {
                    println("Remote: ${remote.name}")
                    println("Commit ${commit.id}")
                    if (commit.properties.containsKey("author")) {
                        println("Author: ${commit.properties["author"]}")
                    }
                    if (commit.properties.containsKey("user")) {
                        println("User: ${commit.properties["user"]}")
                    }
                    if (commit.properties.containsKey("email")) {
                        println("Email: ${commit.properties["email"]}")
                    }
                    println("Date:   ${commit.properties["timestamp"]}")
                    if (commit.properties.containsKey("tags")) {
                        val tags = commit.properties.get("tags") as Map<String, String>
                        if (!tags.isEmpty()) {
                            print("Tags:")
                            for ((key, value) in tags) {
                                print(" ")
                                if (value != "") {
                                    print("$key=$value")
                                } else {
                                    print(key)
                                }
                            }
                            println("")
                        }
                    }
                    if (commit.properties["message"] != "") {
                        println("${n}${commit.properties["message"]}")
                    }
                }
            } catch (e: ClientException) {
                println("${remote.name} has not been initialized.")
            }
        }
    }
}