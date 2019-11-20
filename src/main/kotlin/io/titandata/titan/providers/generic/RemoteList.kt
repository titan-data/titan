/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.generic

import io.titandata.client.apis.RemotesApi
import io.titandata.serialization.RemoteUtil

class RemoteList (
    private val remotesApi: RemotesApi = RemotesApi()
) {
    private val n = System.lineSeparator()
    
    fun list(container: String) {
        val remotes = remotesApi.listRemotes(container)
        System.out.printf("%-20s %-20s${n}", "REMOTE", "URI")
        for (remote in remotes) {
            System.out.printf("%-20s %-20s${n}", remote.name, RemoteUtil().toUri(remote).first)
        }
    }
}