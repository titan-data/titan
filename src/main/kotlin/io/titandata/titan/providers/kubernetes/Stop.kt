/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.kubernetes

import io.titandata.client.apis.RepositoriesApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.clients.Kubernetes
import io.titandata.titan.utils.CommandExecutor

class Stop (
        private val kubernetes : Kubernetes = Kubernetes(),
        private val repositoriesApi : RepositoriesApi = RepositoriesApi()
) {
    fun stop(repoName: String) {
        val repo = repositoriesApi.getRepository(repoName)
        if (repo.properties["disablePortMapping"] != true) {
            println("Stopping port forwarding")
            kubernetes.stopPortFowarding(repoName)
        }
        println("Updating deployment")
        kubernetes.stopStatefulSet(repoName)
        println("Waiting for deployment to stop")
        kubernetes.waitForStatefulSet(repoName)
        println("Stopped $repoName")
    }
}