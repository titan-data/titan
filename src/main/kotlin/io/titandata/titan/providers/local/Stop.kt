/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.titan.clients.Docker
import io.titandata.titan.utils.CommandExecutor

class Stop (
        private val commandExecutor: CommandExecutor = CommandExecutor(),
        private val docker: Docker = Docker(commandExecutor)
) {
    fun stop(container: String) {
        docker.stop(container)
        println("$container stopped")
    }
}