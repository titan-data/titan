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
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

private val n = System.lineSeparator()

class Run : CliktCommand(
        help = "Create repository and start container",
        epilog = "Containers associated with a repository can be launched using context specific run arguments and passed verbatim using `--` as the flag.${n}${n}Docker example: `titan run --disable-port-mapping postgres -- -p 2345:5432`"
) {
    private val dependencies: Dependencies by requireObject()
    private val disablePortMapping by option("-P", "--disable-port-mapping", help = "Disable default port mapping from container to localhost.").flag(default = false)
    private val environments by option("-e", "--env", help = "Container specific environment variables.").multiple()
    private val image by argument()
    private val repository by option("-n", "--name", help = "Optional new name for repository.")
    private val arguments by argument().multiple()
    override fun run() {
        val provider = dependencies.provider
        provider.run(image, repository, environments, arguments, disablePortMapping)
    }
}

val runModule = Kodein.Module("run") {
    bind<CliktCommand>().inSet() with provider {
        Run()
    }
}
