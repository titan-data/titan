/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import io.titandata.titan.Dependencies
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class List : CliktCommand(help = "List repositories", name = "ls") {
    private val dependencies: Dependencies by requireObject()
    private val n = System.lineSeparator()

    override fun run() {
        // Check that we have at least one context installed
        dependencies.providers.default()
        System.out.printf("%-20s  %s$n", "REPOSITORY", "STATUS")
        for (provider in dependencies.providers.list()) {
            provider.list()
        }
    }
}

val listModule = Kodein.Module("ls") {
    bind<CliktCommand>().inSet() with provider {
        List()
    }
}
