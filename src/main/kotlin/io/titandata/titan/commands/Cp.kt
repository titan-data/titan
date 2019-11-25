/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import io.titandata.titan.Dependencies
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Cp : CliktCommand(
        help = "Copy data into a repository",
        epilog = "If the container for the repository has only one volume, data will be copied there. If there are multiple volumes, the destination path must be specified."
) {
    private val dependencies: Dependencies by requireObject()
    private val repository by argument()
    private val source by option("-s", "--source", help = "Required. Source location of the files on the local machine").required()
    private val destination by option("-d", "--destination", help = "Destination of the files inside of the container").default("")
    override fun run() {
        val provider = dependencies.providers.byRepository(repository)
        provider.cp(repository, "local", source, destination)
    }
}

val cpModule = Kodein.Module("cp") {
    bind<CliktCommand>().inSet() with provider {
        Cp()
    }
}
