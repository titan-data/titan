/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.CommitsApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.utils.CommandExecutor

class Checkout (
        private val commandExecutor: CommandExecutor = CommandExecutor(),
        private val docker: Docker = Docker(commandExecutor),
        private val commitsApi: CommitsApi = CommitsApi()
) {
    fun checkout(container: String, guid: String) {
        println("Stopping container $container")
        docker.stop(container)
        println("Checkout $guid")
        commitsApi.checkoutCommit(container, guid)
        println("Starting container $container")
        docker.start(container)
        println("$guid checked out")
    }
}