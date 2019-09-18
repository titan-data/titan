/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.clients

import io.titandata.titan.Version
import io.titandata.titan.Version.Companion.compare
import io.titandata.titan.utils.CommandExecutor
import org.json.JSONArray
import org.json.JSONObject
import org.kohsuke.randname.RandomNameGenerator
import kotlin.random.Random

class Docker(private val executor: CommandExecutor) {

    private val titanLaunchArgs = mutableListOf(
        "--privileged",
        "--pid=host",
        "--network=host",
        "-d",
        "--restart","always",
        "--name=titan-launch",
        "-v", "/var/lib:/var/lib",
        "-v", "/run/docker:/run/docker",
        "-v", "/lib:/var/lib/titan/system",
        "-v", "titan-data:/var/lib/titan/data",
        "-v", "/var/run/docker.sock:/var/run/docker.sock"
    )

    fun version(): String {
        return executor.exec(listOf("docker", "-v")).trim()
    }

    fun titanIsDownloaded(): Boolean {
        val images = executor.exec(listOf("docker", "images", "titan", "--format", "\"{{.Repository}}\""))
        return images.isNotEmpty()
    }

    fun titanLatestIsDownloaded(titanServerVersion: Version): Boolean {
        val images= executor.exec(listOf("docker", "images", "titan", "--format", "\"{{.Tag}}\""))
        val tags = images.split(System.lineSeparator())
        for (item in tags) {
            val tag = item.replace("\"", "")
            if (tag != "latest" && tag != "") {
                val local = Version.fromString(tag)
                if (local.compare(titanServerVersion) == 0) return true
            }
        }
        return false
    }

    fun containerIsRunning(container: String): Boolean {
        val result = executor.exec(listOf("docker", "ps", "-f", "name=$container", "--format", "\"{{.Names}}\""))
        return result.isNotEmpty()
    }

    fun containerIsStopped(container: String): Boolean {
        val result = executor.exec(listOf("docker", "ps", "-f", "status=exited", "|", "grep", "\"$container\""))
        return result.isNotEmpty()
    }

    fun titanLaunchIsAvailable():Boolean {
        return containerIsRunning("titan-launch")
    }

    fun titanLaunchIsStopped(): Boolean {
        return containerIsStopped("titan-launch")
    }

    fun titanServerIsAvailable(): Boolean {
        return containerIsRunning("titan-server")
    }

    fun titanServerIsStopped(): Boolean {
        return containerIsStopped("titan-server")
    }
    fun launchTitanServers(): String {
        titanLaunchArgs.add("-e")
        titanLaunchArgs.add("TITAN_IMAGE=titan:latest")
        return run("titan:latest", "/bin/bash /titan/launch", titanLaunchArgs)
    }

    fun teardownTitanServers(): String {
        titanLaunchArgs.removeAt(titanLaunchArgs.indexOf("-d"))
        titanLaunchArgs.removeAt(titanLaunchArgs.indexOf("--restart"))
        titanLaunchArgs.removeAt(titanLaunchArgs.indexOf("always"))
        titanLaunchArgs.removeAt(titanLaunchArgs.indexOf("--name=titan-launch"))
        titanLaunchArgs.add("--rm")
        return run("titan:latest", "/bin/bash /titan/teardown", titanLaunchArgs)
    }

    fun pull(image: String): String {
        return executor.exec(listOf("docker", "pull", image)).trim()
    }

    fun tag(source: String, target: String): String {
        return executor.exec(listOf("docker", "tag", source, target)).trim()
    }

    fun rm(container: String, force: Boolean): String {
        val forceFlag = if(force) "-f" else ""
        val args = mutableListOf("docker", "rm", forceFlag)
        args.addAll(container.toList())
        return executor.exec(args).trim()
    }

    fun rmStopped(container:String): String {
        val containerId = executor.exec(listOf("docker", "ps", "-f", "status=exited", "-f", "name=$container", "--format", "{{.ID}}")).trim()
        return executor.exec(listOf("docker", "container", "rm", containerId))
    }

    fun run(image: String, entry: String, arguments: List<String>): String {
        val mutArgs = mutableListOf("docker", "run")
        mutArgs.addAll(arguments)
        mutArgs.addAll(image.toList())
        if (entry.isNotEmpty()) mutArgs.addAll(entry.toList())
        return executor.exec(mutArgs)
    }

    fun inspectContainer(container: String): JSONObject? {
        val results = executor.exec(listOf("docker", "inspect",  "--type", "container", container))
        return JSONArray(results).optJSONObject(0)
    }

    fun inspectImage(image: String): JSONObject? {
        val results = executor.exec(listOf("docker", "inspect",  "--type", "image", image))
        return JSONArray(results).optJSONObject(0)
    }

    fun createVolume(name: String, path: String, driver: String = "titan"): String {
        return executor.exec(listOf("docker", "volume", "create", "-d", driver, "-o", "path=$path", name))
    }

    fun removeVolume(name: String): String {
        return executor.exec(listOf("docker", "volume", "rm", name))
    }

    fun cp(source: String, target: String): String {
        return executor.exec(listOf("docker", "cp", "-a", "$source/.", "titan-server:/var/lib/titan/mnt/$target"))
    }

    fun stop(container: String): String {
        return executor.exec(listOf("docker", "stop", container))
    }

    fun start(container: String): String {
        return executor.exec(listOf("docker", "start", container))
    }

    companion object {
        fun String.toList(delimiter: String = " "): List<String> {
            return this.split(delimiter)
        }

        fun String.runtimeToArguments(): List<String> {
            val arguments = this.removePrefix("[").removeSuffix("]").toList(", ").toMutableList()
            if (arguments.contains("--mount")) {
                arguments.removeAt((arguments.indexOf("--mount") + 1))
                arguments.removeAt(arguments.indexOf("--mount"))
            }
            return arguments
        }

        fun List<String>.fetchName(): String {
            return when {
                this.contains("--name") -> this[(this.indexOf("--name")+1)]
                else -> RandomNameGenerator(Random.nextInt()).next()
            }
        }

        fun List<String>.hasDetach(): Boolean {
            return when {
                this.contains("-d") -> true
                this.contains("--detach") -> true
                else -> false
            }
        }
    }
}