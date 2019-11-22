/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.generic

import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.models.Commit
import io.titandata.titan.providers.Metadata
import io.titandata.titan.providers.Metadata.Companion.toMap
import java.util.*

class Commit (
    private val user: String,
    private val email: String,
    private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
    private val commitsApi: CommitsApi = CommitsApi(),
    private val uuid: String = UUID.randomUUID().toString().replace("-","")
) {
    fun commit(container: String, message: String, tags: List<String>) {
        val metadata = Metadata.load(repositoriesApi.getRepository(container).properties)
        val repoStatus = repositoriesApi.getRepositoryStatus(container)
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
        metadata.user = user
        metadata.email = email
        metadata.message = message
        metadata.tags = tagMetadata
        if (sourceCommit != null) {
            metadata.source = sourceCommit
        }
        val commit = Commit(uuid, metadata.toMap())
        val response = commitsApi.createCommit(repository, commit)
        val hash = response.id
        println("Commit $hash")
    }

}