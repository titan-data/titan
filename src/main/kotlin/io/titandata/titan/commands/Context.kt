/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import io.titandata.titan.Dependencies
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Context : CliktCommand(help = "Manage titan contexts") {
    override fun run() {}
}

class ContextInstall : CliktCommand(help = "Install a new context", name = "install") {
    private val dependencies: Dependencies by requireObject()
    private val remote: String? by option("-t", "--type", help = "Context type (local or kubernetes), defaults to local")
    private val name: String? by option("-n", "--name", help = "Context name, defaults to context type")
    private val parameters by option("-p", "--parameters", help = "Context specific parameters. key=value format.").multiple()

    override fun run() {
        TODO("install context")
    }
}

class ContextUninstall : CliktCommand(help = "Uninstall a context", name = "uninstall") {
    private val dependencies: Dependencies by requireObject()
    private val name: String? by option("-n", "--name", help = "Name of context, optional if only one context exists")

    override fun run() {
        TODO("uninstall context")
    }
}

class ContextList : CliktCommand(help = "List available contexts", name = "ls") {
    private val dependencies: Dependencies by requireObject()

    override fun run() {
        for (providerEntry in dependencies.providers.list()) {
            println(providerEntry.key)
        }
    }
}

val contextModule = Kodein.Module("context") {
    bind<CliktCommand>().inSet() with provider {
        Context().subcommands(
                ContextInstall(),
                ContextUninstall(),
                ContextList()
        )
    }
}
