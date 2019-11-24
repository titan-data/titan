/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.kubernetes

import io.titandata.client.apis.RepositoriesApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.clients.Kubernetes
import io.titandata.titan.utils.CommandExecutor

class Start (
        private val kubernetes : Kubernetes = Kubernetes(),
        private val repositoriesApi : RepositoriesApi = RepositoriesApi()
) {
    fun start(repoName: String) {
        val repo = repositoriesApi.getRepository(repoName)
        println("Updating deployment")
        kubernetes.startStatefulSet(repoName)
        println("Waiting for deployment to be ready")
        kubernetes.waitForStatefulSet(repoName)
        if (repo.properties["disablePortMapping"] != true) {
            println("Starting port forwarding")
            kubernetes.startPortForwarding(repoName)
        }
    }
}