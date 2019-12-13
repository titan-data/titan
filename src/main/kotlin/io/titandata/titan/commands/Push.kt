/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import io.titandata.titan.Dependencies
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Push : CliktCommand(help = "Push data state to remote") {
    private val dependencies: Dependencies by requireObject()
    private val commit: String? by option("-c", "--commit", help = "Commit GUID to push, defaults to latest")
    private val remote: String? by option("-r", "--remote", help = "Name of the remote provider, defaults to origin")
    private val tags by option("-t", "--tag", help = "Filter commits to select commit to push").multiple()
    private val metadataOnly: Boolean by option("-u", "--update-only", help = "Update tags only, do not push data").flag()
    private val repository: String by argument()

    override fun run() {
        val (provider, repoName) = dependencies.providers.byRepository(repository)
        provider.push(repoName, commit, remote, tags, metadataOnly)
    }
}

val pushModule = Kodein.Module("push") {
    bind<CliktCommand>().inSet() with provider {
        Push()
    }
}
