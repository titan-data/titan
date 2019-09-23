/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

private val n = System.lineSeparator()

class Run : CliktCommand(
        help = "Create repository and start container",
        epilog = "Containers that contain a repository are launched using docker run arguments and passed verbatim using `--` as the flag.${n}${n}Example: `titan run -- --name newRepo -d -p 5432:5432 postgres:10`"
) {
    private val dependencies: Dependencies by requireObject()
    private val arguments by argument().multiple()
    override fun run() {
        val provider = dependencies.provider
        provider.run(arguments)
    }
}

val runModule = Kodein.Module("run") {
    bind<CliktCommand>().inSet() with provider {
        Run()
    }
}
