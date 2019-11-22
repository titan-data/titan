/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.kubernetes

import io.titandata.titan.clients.Docker
import io.titandata.titan.clients.Kubernetes
import io.titandata.titan.utils.CommandExecutor

class Start (
        private val kubernetes : Kubernetes = Kubernetes()
) {
    fun start(repo: String) {
        println("Updating deployment")
        kubernetes.startStatefulSet(repo)
        println("Waiting for deployment to be ready")
        kubernetes.waitForStatefulSet(repo)
        println("Starting port forwarding")
        kubernetes.startPortForwarding(repo)
    }
}