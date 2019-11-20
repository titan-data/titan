/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.generic

import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.models.Commit
import java.util.*

class Commit (
    private val user: String,
    private val email: String,
    private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
    private val commitsApi: CommitsApi = CommitsApi(),
    private val uuid: String = UUID.randomUUID().toString().replace("-","")
) {
    fun commit(repository: String, message: String, tags: List<String>) {
        val repoMetadata = repositoriesApi.getRepository(repository).properties
        val repoStatus = repositoriesApi.getRepositoryStatus(repository)
        val sourceCommit = repoStatus.sourceCommit
        val tagMetadata = mutableMapOf<String, String>()
        for (tag in tags) {
            val (key, value) = if (tag.contains("=")) {
                Pair(tag.substringBefore("="), tag.substringAfter("="))
            } else {
                Pair(tag, "")
            }
            tagMetadata[key] = value
        }

        val metadata = mutableMapOf(
                "user" to user,
                "email" to email,
                "message" to message,
                "container" to repoMetadata["container"]!!,
                "image" to repoMetadata["image"]!!,
                "tag" to repoMetadata["tag"]!!,
                "digest" to repoMetadata["digest"]!!,
                "runtime" to repoMetadata["runtime"]!!,
                "tags" to tagMetadata
        )
        if (sourceCommit != null) {
                metadata.put("source", sourceCommit)
        }
        val commit = Commit(uuid, metadata)
        val response = commitsApi.createCommit(repository, commit)
        val hash = response.id
        println("Commit $hash")
    }

}