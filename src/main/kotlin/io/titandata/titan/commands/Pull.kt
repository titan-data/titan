/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Pull : CliktCommand(help = "Pull a new data state from remote") {
    private val dependencies: Dependencies by requireObject()
    private val commit by option("-c", "--commit", help="Commit GUID to pull from, defaults to latest")
    private val remote: String? by option("-r", "--remote", help="Name of the remote provider, defaults to origin")
    private val metadataOnly: Boolean by option("-u", "--update-only", help="Update tags only, do not pull data").flag()
    private val tags by option("-t", "--tag", help="Filter commits to select commit to pull").multiple()
    private val repository by argument()

    override fun run() {
        val provider = dependencies.providers.byRepository(repository)
        provider.pull(repository, commit, remote, tags, metadataOnly)
    }
}

val pullModule = Kodein.Module("pull") {
    bind<CliktCommand>().inSet() with provider {
        Pull()
    }
}
