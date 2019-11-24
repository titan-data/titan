/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Log : CliktCommand(help = "List commits for a repository") {
    private val dependencies: Dependencies by requireObject()
    private val repository by argument()
    private val tags by option("-t", "--tag", help="Tag to set").multiple()
    override fun run() {
        val provider = dependencies.providers.byRepository(repository)
        provider.log(repository, tags)
    }
}

val logModule = Kodein.Module("log") {
    bind<CliktCommand>().inSet() with provider {
        Log()
    }
}
