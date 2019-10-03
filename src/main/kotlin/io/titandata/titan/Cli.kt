/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan

import io.titandata.titan.commands.*
import io.titandata.titan.providers.ProviderFactory
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import io.titandata.titan.exceptions.CommandException
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.setBinding
import kotlin.system.exitProcess

object Cli {
    class Titan: CliktCommand(help = "Titan CLI") {

        override fun run() {
            val providerFactory = ProviderFactory()
            val provider = providerFactory.getFactory("Local")
            context.obj = Dependencies(provider)
            if (context.invokedSubcommand?.commandName != "install") {
                provider.checkInstall()
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val version = Cli::class.java.getResource("/VERSION").readText()
        val kodein = Kodein {
            bind() from setBinding<CliktCommand>()
            import(installModule)
            import(runModule)
            import(cloneModule)
            import(migrateModule)
            import(cpModule)
            import(startModule)
            import(stopModule)
            import(commitModule)
            import(deleteModule)
            import(checkoutModule)
            import(listModule)
            import(logModule)
            import(pullModule)
            import(pushModule)
            import(abortModule)
            import(remoteModule)
            import(statusModule)
            import(removeModule)
            import(uninstallModule)
            import(upgradeModule)
        }
        val commands: Set<CliktCommand> by kodein.instance()
        try {
            Titan().subcommands(commands).versionOption(version).main(args)
        } catch (e: CommandException) {
            println(e.message)
            println(e.output)
            exitProcess(e.exitCode)
        } catch (e: Throwable) {
            println(e.message)
            exitProcess(1)
        }
    }
}
