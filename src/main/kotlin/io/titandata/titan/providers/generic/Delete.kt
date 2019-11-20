/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.generic

import io.titandata.client.apis.CommitsApi

class Delete (
    private val commitsApi: CommitsApi = CommitsApi()
) {
    fun deleteCommit(repository: String, commit: String) {
        commitsApi.deleteCommit(repository, commit)
        println("$commit deleted")
    }

    fun deleteTags(repository: String, commit: String, tags: List<String>) {
        val commit = commitsApi.getCommit(repository, commit)
        val commitTags = (commit.properties.get("tags") as Map<String, String>).toMutableMap()

        for (t in tags) {
            if (t.contains("=")) {
                val key = t.substringBefore("=")
                val value = t.substringAfter("=")
                if (commitTags.containsKey(key) && commitTags[key] == value) {
                    commitTags.remove(key)
                }
            } else {
                commitTags.remove(t)
            }
        }

        val metadata = commit.properties.toMutableMap()
        metadata["tags"] = commitTags

        commitsApi.updateCommit(repository, io.titandata.models.Commit(id=commit.id, properties=metadata))
    }
}