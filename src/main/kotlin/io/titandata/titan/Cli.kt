/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan

import io.titandata.titan.commands.*
import io.titandata.titan.providers.ProviderFactory
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import io.titandata.client.infrastructure.ApiClient
import io.titandata.titan.exceptions.CommandException
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.log4j.BasicConfigurator
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.setBinding
import kotlin.system.exitProcess

object Cli {
    class Titan: CliktCommand(help = "Titan CLI") {

        override fun run() {
            val providerFactory = ProviderFactory()
            val type = System.getenv("TITAN_CONTEXT") ?: "local"
            val provider = providerFactory.getFactory(type)
            context.obj = Dependencies(provider)
            if (context.invokedSubcommand?.commandName != "install") {
                provider.checkInstall()
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val titanDebug = System.getenv("TITAN_DEBUG")
        if (titanDebug != null) {
            val logging = HttpLoggingInterceptor()
            if (titanDebug.equals("trace", true)) {
                logging.level = HttpLoggingInterceptor.Level.BODY
            } else {
                logging.level = HttpLoggingInterceptor.Level.BASIC
            }
            ApiClient.builder.addInterceptor(logging)
        }

        BasicConfigurator.configure();

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
            import(tagModule)
        }
        val commands: Set<CliktCommand> by kodein.instance()
        try {
            Titan().subcommands(commands).versionOption(version).main(args)
        } catch (e: CommandException) {
            println(e.message)
            println(e.output)
            if (titanDebug != null) {
                e.printStackTrace()
            }
            exitProcess(e.exitCode)
        } catch (e: Throwable) {
            println(e.message)
            if (titanDebug != null) {
                e.printStackTrace()
            }
            exitProcess(1)
        }
    }
}
