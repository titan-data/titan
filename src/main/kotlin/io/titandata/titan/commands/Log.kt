/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Log : CliktCommand(help = "List commits for a repository") {
    private val dependencies: Dependencies by requireObject()
    private val repository by argument()
    override fun run() {
        val provider = dependencies.provider
        provider.log(repository)
    }
}

val logModule = Kodein.Module("log") {
    bind<CliktCommand>().inSet() with provider {
        Log()
    }
}
