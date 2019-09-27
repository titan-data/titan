/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.option
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Install : CliktCommand(help = "Install titan infrastructure") {
    private val dependencies: Dependencies by requireObject()
    private val registry by option("-r","--registry", help = "Registry URL for titan docker image, defaults to titandata")
    override fun run() {
        val provider = dependencies.provider
        provider.install(registry)
    }
}

val installModule = Kodein.Module("install") {
    bind<CliktCommand>().inSet() with provider {
        Install()
    }
}
