/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import io.titandata.client.infrastructure.ApiClient
import io.titandata.titan.commands.abortModule
import io.titandata.titan.commands.checkoutModule
import io.titandata.titan.commands.cloneModule
import io.titandata.titan.commands.commitModule
import io.titandata.titan.commands.cpModule
import io.titandata.titan.commands.deleteModule
import io.titandata.titan.commands.installModule
import io.titandata.titan.commands.listModule
import io.titandata.titan.commands.logModule
import io.titandata.titan.commands.migrateModule
import io.titandata.titan.commands.pullModule
import io.titandata.titan.commands.pushModule
import io.titandata.titan.commands.remoteModule
import io.titandata.titan.commands.removeModule
import io.titandata.titan.commands.runModule
import io.titandata.titan.commands.startModule
import io.titandata.titan.commands.statusModule
import io.titandata.titan.commands.stopModule
import io.titandata.titan.commands.tagModule
import io.titandata.titan.commands.uninstallModule
import io.titandata.titan.commands.upgradeModule
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.providers.ProviderFactory
import kotlin.system.exitProcess
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.log4j.BasicConfigurator
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.setBinding

object Cli {
    class Titan : CliktCommand(help = "Titan CLI") {

        override fun run() {
            context.obj = Dependencies(ProviderFactory())
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

        BasicConfigurator.configure()

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
