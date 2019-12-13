/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import io.titandata.titan.Dependencies
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Checkout : CliktCommand(help = "Checkout a specific commit") {
    private val dependencies: Dependencies by requireObject()
    private val repository by argument()
    private val commit by option("-c", "--commit", help = "Commit to checkout")
    private val tags by option("-t", "--tag", help = "Tag to filter latest commit, if commit is not specified").multiple()
    override fun run() {
        val (provider, repoName) = dependencies.providers.byRepository(repository)
        provider.checkout(repoName, commit, tags)
    }
}

val checkoutModule = Kodein.Module("checkout") {
    bind<CliktCommand>().inSet() with provider {
        Checkout()
    }
}
