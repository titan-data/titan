/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.kubernetes

import io.titandata.titan.clients.Docker
import io.titandata.titan.clients.Kubernetes
import io.titandata.titan.utils.CommandExecutor

class Stop (
        private val kubernetes : Kubernetes = Kubernetes()
) {
    fun stop(repo: String) {
        println("Stopping port forwarding")
        kubernetes.stopPortFowarding(repo)
        println("Updating deployment")
        kubernetes.stopStatefulSet(repo)
        println("Waiting for deployment to be ready")
        kubernetes.waitForStatefulSet(repo)
        println("Stopped $repo")
    }
}