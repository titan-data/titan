/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

class Abort : CliktCommand(help = "Abort current push or pull operation") {
    private val dependencies: Dependencies by requireObject()
    private val repository : String by argument()
    override fun run() {
        val provider = dependencies.provider
        provider.abort(repository)
    }
}

val abortModule = Kodein.Module("abort") {
    bind<CliktCommand>().inSet() with provider {
        Abort()
    }
}
