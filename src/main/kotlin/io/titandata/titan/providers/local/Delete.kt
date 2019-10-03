/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.CommitsApi

class Delete (
    private val commitsApi: CommitsApi = CommitsApi()
) {
    fun deleteCommit(repository: String, commit: String) {
        commitsApi.deleteCommit(repository, commit)
        println("$commit deleted")
    }
}