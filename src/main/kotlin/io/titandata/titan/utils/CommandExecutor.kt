/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.utils

import io.titandata.titan.exceptions.CommandException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * ORIGINAL FILE FROM TITAN-SERVER
 *
 * Handle invocation of external commands. This is a wrapper around the native interfaces that
 * handles all of the stdin/stdout errors, and will throw an exception if the command returns
 * a non-zero exit status. It is also a convenient to mock out any dependencies on the external
 * system.
 */
class CommandExecutor(val timeout: Long = 10) {

    /**
     * Execute the given command. Throws an exception if the
     */
    fun exec(args: List<String>, debug: Boolean = false): String {
        val builder = ProcessBuilder()
        if (debug) println(args)
        builder.command(args)
        val process = builder.start()
        try {
            process.waitFor(timeout, TimeUnit.MINUTES)
            val output = process.inputStream.bufferedReader().readText()
            if (process.isAlive) {
                throw IOException("Timed out waiting for command: $args")
            }
            if (debug) println("Exit Value: ${process.exitValue()}")
            if (process.exitValue() != 0) {
                val errOutput = process.errorStream.bufferedReader().readText()
                if (debug) println(errOutput)
                throw CommandException("Command failed: ${args.stringify()}",
                        exitCode = process.exitValue(),
                        output = errOutput)
            }
            return output
        } finally {
            process.destroy()
        }
    }

    companion object {
        fun List<String>.stringify(): String {
            var retString = ""
            for (item in this) {
                retString += "$item "
            }
            return retString
        }
    }
}
