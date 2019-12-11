/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.titandata.titan.Dependencies
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Install : CliktCommand(help = "Install titan infrastructure") {
    private val dependencies: Dependencies by requireObject()
    private val registry by option("-r", "--registry", help = "Registry URL for titan docker image, defaults to titandata")
    private val verbose by option("-V", "--verbose", help = "Verbose output of Titan Server installation steps.").flag(default = false)
    override fun run() {
        val props = mutableMapOf<String, String>()
        if (registry != null) {
            props["registry"] = registry!!
        }
        val provider = dependencies.providers.create("docker", "docker")
        provider.install(props, verbose)
        dependencies.providers.addProvider(provider)
    }
}

val installModule = Kodein.Module("install") {
    bind<CliktCommand>().inSet() with provider {
        Install()
    }
}
