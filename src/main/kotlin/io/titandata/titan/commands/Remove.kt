/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Remove : CliktCommand(help = "Remove a repository", name = "rm") {
    private val force by option("-f", "--force", help="Stop container if running").flag(default=false)
    private val dependencies: Dependencies by requireObject()
    private val repository by argument()
    override fun run() {
        val provider = dependencies.providers.byRepository(repository)
        provider.remove(repository, force)
    }
}

val removeModule = Kodein.Module("rm") {
    bind<CliktCommand>().inSet() with provider {
        Remove()
    }
}
