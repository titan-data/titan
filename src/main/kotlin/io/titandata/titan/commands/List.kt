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
    override fun run() {
        val provider = dependencies.provider
        provider.list()
    }
}

val listModule = Kodein.Module("ls") {
    bind<CliktCommand>().inSet() with provider {
        List()
    }
}
