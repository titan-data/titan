/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Upgrade : CliktCommand(help = "Upgrade titan CLI and infrastructure") {
    private val dependencies: Dependencies by requireObject()
    private val force by option("-f", "--force", help="Stop running containers").flag(default = false)
    private val version = Upgrade::class.java.getResource("/VERSION").readText()
    private val finalize by option("--finalize").flag(default = false)
    private val path by option("-p", "--path", help="Full installation path of Titan").default("")
    override fun run() {
        // TODO This command is not really provider-specific
        val provider = dependencies.providers.defaultProvider

        provider.upgrade(force, version, finalize, path)
    }
}

val upgradeModule = Kodein.Module("upgrade") {
    bind<CliktCommand>().inSet() with provider {
        Upgrade()
    }
}
