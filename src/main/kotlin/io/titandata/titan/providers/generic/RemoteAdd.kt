/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.generic

import io.titandata.client.apis.RemotesApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.infrastructure.ClientException
import io.titandata.models.Repository
import io.titandata.serialization.RemoteUtil

class RemoteAdd (
        private val exit: (message: String, code: Int) -> Unit,
        private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
        private val remotesApi: RemotesApi = RemotesApi(),
        private val remoteUtil: RemoteUtil = RemoteUtil()
) {
    fun remoteAdd(
            container:String,
            uri: String,
            remoteName: String?,
            params: Map<String, String>
    ) {
        val name = remoteName ?: "origin"
        try {
            remotesApi.getRemote(container, name)
            exit("remote $name already exists for $container", 1)
        } catch (e: ClientException) { }
        val remote = remoteUtil.parseUri(uri, name, params)
        remotesApi.createRemote(container, remote)
        val metadata = repositoriesApi.getRepository(container).properties.toMutableMap()
        metadata["remote"] = remoteName ?: container
        val repo = Repository(container, metadata)
        repositoriesApi.updateRepository(container,repo)
    }
}