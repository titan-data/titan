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

class Uninstall : CliktCommand(help = "Uninstall titan infrastructure") {
    private val force by option("-f", "--force", help = "Destroy all repositories").flag(default = false)
    private val dependencies: Dependencies by requireObject()

    override fun run() {
        for (providerEntry in dependencies.providers.list()) {
            providerEntry.value.uninstall(force)
        }
    }
}

val uninstallModule = Kodein.Module("uninstall") {
    bind<CliktCommand>().inSet() with provider {
        Uninstall()
    }
}
