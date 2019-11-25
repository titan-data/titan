/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import io.titandata.titan.Dependencies
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Stop : CliktCommand(help = "Stop a running container for a repository") {
    private val dependencies: Dependencies by requireObject()
    private val repository by argument()
    override fun run() {
        val provider = dependencies.providers.byRepository(repository)
        provider.stop(repository)
    }
}

val stopModule = Kodein.Module("stop") {
    bind<CliktCommand>().inSet() with provider {
        Stop()
    }
}
