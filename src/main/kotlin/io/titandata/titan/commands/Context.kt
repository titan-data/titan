/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import io.titandata.titan.Dependencies
import io.titandata.titan.exceptions.InvalidArgumentException
import java.net.ServerSocket
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Context : CliktCommand(help = "Manage titan contexts") {
    override fun run() {}
}

class ContextInstall : CliktCommand(help = "Install a new context", name = "install") {
    private val dependencies: Dependencies by requireObject()
    private val type: String by option("-t", "--type", help = "Context type (local or kubernetes), defaults to local").default("local")
    private val name: String? by option("-n", "--name", help = "Context name, defaults to context type")
    private val parameters by option("-p", "--parameters", help = "Context specific parameters. key=value format.").multiple()
    private val verbose by option("-v", "--verbose", help = "Verbose logging").flag(default = false)

    private fun getAvailablePort(): Int {
        val socket = ServerSocket(0)
        socket.use {
            socket.reuseAddress = true
            return socket.localPort
        }
    }

    override fun run() {
        val contextName = name ?: type
        val params = mutableMapOf<String, String>()
        for (param in parameters) {
            val split = param.split("=")
            if (split.count() != 2) {
                throw InvalidArgumentException(message = "Parameters must be in key=value format.", exitCode = 1, output = param)
            }
            params[split[0]] = split[1]
        }
        val port = getAvailablePort()
        val provider = dependencies.providers.create(contextName, type, port)
        provider.install(params, verbose)
        dependencies.providers.addProvider(contextName, type, port)
    }
}

class ContextUninstall : CliktCommand(help = "Uninstall a context", name = "uninstall") {
    private val force by option("-f", "--force", help = "Destroy all repositories").flag(default = false)
    private val dependencies: Dependencies by requireObject()
    private val contextName by argument()

    override fun run() {
        val provider = dependencies.providers.byName(contextName)
        provider.uninstall(force)
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
