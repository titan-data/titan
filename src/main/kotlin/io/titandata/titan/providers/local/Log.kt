/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.CommitsApi

class Log (
    private val commitsApi: CommitsApi = CommitsApi()
) {
    private val n = System.lineSeparator()

    fun log(container: String, tags: List<String>) {
        var first = true
        for (commit in commitsApi.listCommits(container, tags)) {
            if (!first) {
                println("")
            } else {
                first = false
            }
            val metadata = commit.properties
            println("commit ${commit.id}")
            if (metadata.containsKey("author")) {
                println("Author: ${metadata["author"]}")
            }
            if (metadata.containsKey("user")) {
                println("User: ${metadata["user"]}")
            }
            if (metadata.containsKey("email")) {
                println("Email: ${metadata["email"]}")
            }
            println("Date: ${metadata["timestamp"]}")
            if (metadata.containsKey("tags")) {
                @Suppress("UNCHECKED_CAST")
                val tags = metadata.get("tags") as Map<String, String>
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
            if (metadata["message"] != "") {
                println("${n}${metadata["message"]}")
            }
        }
    }
}