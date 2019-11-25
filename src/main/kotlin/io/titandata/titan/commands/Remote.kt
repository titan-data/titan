/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import io.titandata.titan.Dependencies
import io.titandata.titan.exceptions.InvalidArgumentException
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Remote : CliktCommand(help = "Add, log, ls and rm remotes") {
    override fun run() {}
}

class RemoteAdd : CliktCommand(help = "Set remote destination for a repository", name = "add") {
    private val dependencies: Dependencies by requireObject()
    private val remote: String? by option("-r", "--remote", help = "Name of the remote provider, defaults to origin")
    private val parameters by option("-p", "--parameters", help = "Provider specific parameters. key=value format.").multiple()
    private val uri: String by argument()
    private val repository: String by argument()

    override fun run() {
        val provider = dependencies.provider
        val params = mutableMapOf<String, String>()
        for (param in parameters) {
            val split = param.split("=")
            if (split.count() != 2) {
                throw InvalidArgumentException(message = "Parameters must be in key=value format.", exitCode = 1, output = param)
            }
            params[split[0]] = split[1]
        }
        provider.remoteAdd(repository, uri, remote, params)
    }
}

class RemoteLog : CliktCommand(help = "Display log on remote", name = "log") {
    private val dependencies: Dependencies by requireObject()
    private val repository: String by argument()
    private val remote: String? by option("-r", "--remote", help = "Name of the remote provider, defaults to origin")
    private val tags by option("-t", "--tag", help = "Tag to set").multiple()

    override fun run() {
        val provider = dependencies.provider
        provider.remoteLog(repository, remote, tags)
    }
}

class RemoteList : CliktCommand(help = "List remotes for a repository", name = "ls") {
    private val dependencies: Dependencies by requireObject()
    private val repository: String by argument()

    override fun run() {
        val provider = dependencies.provider
        provider.remoteList(repository)
    }
}

class RemoteRemove : CliktCommand(help = "Remove remote from a repository", name = "rm") {
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
