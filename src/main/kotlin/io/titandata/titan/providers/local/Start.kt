/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.titan.clients.Docker
import io.titandata.titan.utils.CommandExecutor

class Start (
        private val commandExecutor: CommandExecutor = CommandExecutor(),
        private val docker: Docker = Docker(commandExecutor)
) {
    fun start(container: String) {
        docker.start(container)
        println("$container started")
    }
}