/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import io.titandata.titan.Dependencies
import io.titandata.titan.exceptions.InvalidArgumentException
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Clone : CliktCommand(help = "Clone a remote repository to local repository") {
    private val dependencies: Dependencies by requireObject()
    private val uri by argument()
    private val repository by option("-n", "--name", help = "Optional new name for repository.")
    private val commit by option("-c", "--commit", help = "Commit GUID to pull from, defaults to latest")
    private val parameters by option("-p", "--parameters", help = "Provider specific parameters. key=value format.").multiple()
    private val disablePortMapping by option("-P", "--disable-port-mapping", help = "Disable default port mapping from container to localhost.").flag(default = false)
    private val arguments by argument().multiple()

    override fun run() {
        val provider = dependencies.providers.byRepositoryName(repository)
        val params = mutableMapOf<String, String>()
        for (param in parameters) {
            val split = param.split("=")
            if (split.count() != 2) {
                throw InvalidArgumentException(message = "Parameters must be in key=value format.", exitCode = 1, output = param)
            }
            params[split[0]] = split[1]
        }
        provider.clone(uri, repository, commit, params, arguments, disablePortMapping)
    }
}

val cloneModule = Kodein.Module("clone") {
    bind<CliktCommand>().inSet() with provider {
        Clone()
    }
}
