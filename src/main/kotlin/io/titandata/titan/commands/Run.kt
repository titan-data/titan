/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

private val n = System.lineSeparator()

class Run : CliktCommand(
        help = "Create repository and start container"
) {
    private val dependencies: Dependencies by requireObject()
    private val disablePortMapping by option("-P", "--disablePortMapping", help = "Disable port mapping from container to localhost.").flag(default = false)
    private val parameters by option("-p", "--parameters", help="Context specific parameters. key=value format.").multiple()
    private val environments by option("-e", "--env", help="Container specific environment variables. key=value format.").multiple()
    private val repository by argument().optional()
    override fun run() {
        val provider = dependencies.provider
        provider.run(repository, environments, parameters, disablePortMapping)
    }
}

val runModule = Kodein.Module("run") {
    bind<CliktCommand>().inSet() with provider {
        Run()
    }
}
