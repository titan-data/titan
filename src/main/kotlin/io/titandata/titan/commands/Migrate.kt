/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.commands

import io.titandata.titan.Dependencies
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.inSet
import org.kodein.di.generic.provider

private val n = System.lineSeparator()

class Migrate : CliktCommand(
        help = "Migrate an existing docker database container to titan repository",
        epilog = "Container becomes the new name of the docker container.${n}${n}Example: `titan migrate -s oldPostgres titanPostgres`"
) {
    private val dependencies: Dependencies by requireObject()
    private val repository by argument()
    private val source by option("-s", "--source", help="Required. Source docker database container").required()
    override fun run() {
        val provider = dependencies.provider
        provider.migrate(source, repository)
    }
}

val migrateModule = Kodein.Module("migrate") {
    bind<CliktCommand>().inSet() with provider {
        Migrate()
    }
}
