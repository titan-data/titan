/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.generic

import io.titandata.client.apis.RemotesApi

class RemoteRemove (
    private val remotesApi: RemotesApi = RemotesApi()
) {
    fun remove(container: String, remote: String) {
        remotesApi.deleteRemote(container, remote)
        println("Removed $remote from $container")
    }
}