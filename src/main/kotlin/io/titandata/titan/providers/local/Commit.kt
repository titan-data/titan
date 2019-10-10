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
    fun commit(container: String, message: String) {
        val repoMetadata = repositoriesApi.getRepository(container).properties
        val metadata = mapOf(
                "user" to user,
                "email" to email,
                "message" to message!!,
                "container" to repoMetadata["container"]!!,
                "runtime" to repoMetadata["runtime"]!!
        )
        val commit = Commit(uuid, metadata)
        val response = commitsApi.createCommit(container, commit)
        val hash = response.id
        println("Commit $hash")
    }
}