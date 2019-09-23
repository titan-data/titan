/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.RemotesApi

class RemoteList (
    private val remotesApi: RemotesApi = RemotesApi()
) {
    private val n = System.lineSeparator()
    
    fun list(container: String) {
        val remotes = remotesApi.listRemotes(container)
        System.out.printf("%-20s  %s${n}", "REMOTE", "PROVIDER")
        for (remote in remotes) {
            System.out.printf("%-20s  %s${n}", remote.name, remote.provider)
        }
    }
}