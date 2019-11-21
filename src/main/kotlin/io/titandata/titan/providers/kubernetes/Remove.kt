/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.kubernetes

import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.apis.VolumesApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.clients.Kubernetes
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.utils.CommandExecutor
import java.lang.Exception

class Remove (
        private val kubernetes: Kubernetes = Kubernetes(),
        private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
        private val volumeApi: VolumesApi = VolumesApi()
) {
    fun remove(repo: String, force: Boolean) {
        // TODO check running  & force
        kubernetes.deleteStatefulSpec(repo)
        for (volume in volumeApi.listVolumes(repo)) {
            volumeApi.deleteVolume(repo, volume.name)
        }
        repositoriesApi.deleteRepository(repo)
        println("$repo removed")
    }
}