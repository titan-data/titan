/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Tag : CliktCommand(
        help = "Tag objects in titan"
) {
    private val dependencies: Dependencies by requireObject()
    private val repository by argument()
    private val commit by option("-c", "--commit", help="Commit GUID to tag").required()
    private val tags by option("-t", "--tag", help="Tags to add").multiple()
    override fun run() {
        val provider = dependencies.provider
        provider.tag(repository, commit, tags)
    }
}

val tagModule = Kodein.Module("tag") {
    bind<CliktCommand>().inSet() with provider {
        Tag()
    }
}
