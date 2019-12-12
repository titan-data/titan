/*
 * Copyright The Titan Project Contributors.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import io.titandata.titan.Dependencies
import io.titandata.titan.exceptions.InvalidArgumentException
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Context : CliktCommand(help = "Manage titan contexts") {
    override fun run() {}
}

class ContextInstall : CliktCommand(help = "Install a new context", name = "install") {
    private val dependencies: Dependencies by requireObject()
    private val type: String by option("-t", "--type", help = "Context type (docker or kubernetes), defaults to docker").default("docker")
    private val name: String? by option("-n", "--name", help = "Context name, defaults to context type")
    private val parameters by option("-p", "--parameters", help = "Context specific parameters. key=value format").multiple()
    private val verbose by option("-v", "--verbose", help = "Verbose logging").flag(default = false)

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
        val provider = dependencies.providers.create(contextName, type)
        provider.install(params, verbose)
        dependencies.providers.addProvider(provider)
    }
}

class ContextUninstall : CliktCommand(help = "Uninstall a context", name = "uninstall") {
    private val force by option("-f", "--force", help = "Destroy all repositories").flag(default = false)
    private val dependencies: Dependencies by requireObject()
    private val contextName by argument()

    override fun run() {
        val provider = dependencies.providers.byName(contextName)
        provider.uninstall(force, false)
        dependencies.providers.removeProvider(contextName)
    }
}

class ContextList : CliktCommand(help = "List available contexts", name = "ls") {
    private val dependencies: Dependencies by requireObject()
    private val n = System.lineSeparator()

    override fun run() {
        System.out.printf("%-20s  %-12s$n", "NAME", "TYPE")
        val providers = dependencies.providers.list()
        if (!providers.isEmpty()) {
            val defaultName = dependencies.providers.defaultName()
            for (providerEntry in providers) {
                var context = providerEntry.key
                if (providerEntry.key == defaultName) {
                    context += " (*)"
                }
                val type = providerEntry.value.getType()
                System.out.printf("%-20s  %-12s$n", context, type)
            }
        }
    }
}
class ContextDefault : CliktCommand(help = "Get or set default context", name = "default") {
    private val dependencies: Dependencies by requireObject()
    private val contextName by argument().optional()

    override fun run() {
        if (contextName != null) {
            dependencies.providers.setDefault(contextName!!)
        } else {
            println(dependencies.providers.defaultName())
        }
    }
}

val contextModule = Kodein.Module("context") {
    bind<CliktCommand>().inSet() with provider {
        Context().subcommands(
                ContextInstall(),
                ContextUninstall(),
                ContextDefault(),
                ContextList()
        )
    }
}
