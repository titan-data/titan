/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.option
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Clone : CliktCommand(help = "Clone a remote repository to local repository") {
    private val dependencies: Dependencies by requireObject()
    private val uri by argument()
    private val repository by argument().optional()
    private val commit by option("-c", "--commit", help="Commit GUID to pull from, defaults to latest")

    override fun run() {
        val provider = dependencies.provider
        provider.clone(uri, repository, commit)
    }
}

val cloneModule = Kodein.Module("clone") {
    bind<CliktCommand>().inSet() with provider {
        Clone()
    }
}
