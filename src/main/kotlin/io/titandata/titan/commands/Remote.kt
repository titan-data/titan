/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Remote : CliktCommand(help = "Add, log, ls and rm remotes") {
    override fun run() {}
}

class RemoteAdd : CliktCommand(help = "Set remote destination for a repository", name = "add") {
    private val dependencies: Dependencies by requireObject()
    private val remote: String? by option("-r", "--remote", help="Name of the remote provider, defaults to origin")
    private val uri: String by argument()
    private val repository: String by argument()

    override fun run() {
        val provider = dependencies.provider
        provider.remoteAdd(repository, uri, remote)
    }
}

class RemoteLog : CliktCommand(help = "Display log on remote", name = "log") {
    private val dependencies: Dependencies by requireObject()
    private val repository: String by argument()
    private val remote: String? by option("-r", "--remote", help="Name of the remote provider, defaults to origin")

    override fun run() {
        val provider = dependencies.provider
        provider.remoteLog(repository, remote)
    }
}

class RemoteList: CliktCommand(help = "List remotes for a repository", name = "ls") {
    private val dependencies: Dependencies by requireObject()
    private val repository: String by argument()

    override fun run() {
        val provider = dependencies.provider
        provider.remoteList(repository)
    }
}

class RemoteRemove: CliktCommand(help = "Remove remote from a repository", name = "rm") {
    private val dependencies: Dependencies by requireObject()
    private val repository: String by argument()
    private val remote: String by argument()

    override fun run() {
        val provider = dependencies.provider
        provider.remoteRemove(repository, remote)
    }
}

val remoteModule = Kodein.Module("remote") {
    bind<CliktCommand>().inSet() with provider {
        Remote().subcommands(
                RemoteAdd(),
                RemoteLog(),
                RemoteList(),
                RemoteRemove()
        )
    }
}
