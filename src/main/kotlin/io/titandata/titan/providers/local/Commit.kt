/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

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
    fun commit(container: String, message: String, tags: List<String>) {
        val repoMetadata = repositoriesApi.getRepository(container).properties
        val tagMetadata = mutableMapOf<String, String>()
        for (t in tags) {
            val (key, value) = if (t.contains("=")) {
                Pair(t.substringBefore("="), t.substringAfter("="))
            } else {
                Pair(t, "")
            }
            tagMetadata[key] = value
        }

        val metadata = mapOf(
                "user" to user,
                "email" to email,
                "message" to message!!,
                "container" to repoMetadata["container"]!!,
                "image" to repoMetadata["image"]!!,
                "tag" to repoMetadata["tag"]!!,
                "digest" to repoMetadata["digest"]!!,
                "runtime" to repoMetadata["runtime"]!!,
                "tags" to tagMetadata
        )
        val commit = Commit(uuid, metadata)
        val response = commitsApi.createCommit(container, commit)
        val hash = response.id
        println("Commit $hash")
    }

}