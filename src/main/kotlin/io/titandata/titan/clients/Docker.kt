/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.clients

import io.titandata.titan.Version
import io.titandata.titan.Version.Companion.compare
import io.titandata.titan.utils.CommandExecutor
import kotlin.random.Random
import org.json.JSONArray
import org.json.JSONObject
import org.kohsuke.randname.RandomNameGenerator

class Docker(private val executor: CommandExecutor, val identity: String = "docker", val port: Int = 5001) {

    val logs = mutableMapOf<String, Boolean>()

    private val titanLaunchArgs = mutableListOf(
        "--privileged",
        "--pid=host",
        "--network=host",
        "-d",
        "--restart", "always",
        "--name=titan-$identity-launch",
        "-v", "/var/lib:/var/lib",
        "-v", "/run/docker:/run/docker",
        "-v", "/lib:/var/lib/titan-$identity/system",
        "-v", "titan-$identity-data:/var/lib/titan-$identity/data",
        "-v", "/var/run/docker.sock:/var/run/docker.sock"
    )

    private val titanLaunchKubernetesArgs = mutableListOf(
            "-d",
            "--restart", "always",
            "--name=titan-$identity-server",
            "-v", "${System.getProperty("user.home")}/.kube:/root/.kube",
            "-v", "titan-$identity-data:/var/lib/$identity",
            "-e", "TITAN_CONTEXT=kubernetes-csi",
            "-e", "TITAN_IDENTITY=titan-$identity",
            "-p", "$port:5001"
    )

    fun version(): String {
        return executor.exec(listOf("docker", "-v")).trim()
    }

    fun titanLatestIsDownloaded(titanServerVersion: Version): Boolean {
        val images = executor.exec(listOf("docker", "images", "titan", "--format", "\"{{.Tag}}\""))
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

    fun containerExists(container: String): Boolean {
        val result = executor.exec(listOf("docker", "ps", "-a", "-f", "name=$container", "--format", "\"{{.Names}}\""))
        return result.isNotEmpty()
    }

    fun containerIsRunning(container: String): Boolean {
        val result = executor.exec(listOf("docker", "ps", "-f", "name=$container", "--format", "\"{{.Names}}\""))
        return result.isNotEmpty()
    }

    fun containerIsStopped(container: String): Boolean {
        val result = executor.exec(listOf("docker", "ps", "-f", "status=exited", "|", "grep", "\"$container\""))
        return result.isNotEmpty()
    }

    fun titanLaunchIsAvailable(): Boolean {
        return containerIsRunning("titan-$identity-launch")
    }

    fun titanLaunchIsStopped(): Boolean {
        return containerIsStopped("titan-$identity-launch")
    }

    fun titanServerIsAvailable(): Boolean {
        return containerIsRunning("titan-$identity-server")
    }

    fun titanServerIsStopped(): Boolean {
        return containerIsStopped("titan-$identity-server")
    }

    fun launchTitanServers(): String {
        titanLaunchArgs.add("-e")
        titanLaunchArgs.add("TITAN_PORT=$port")
        titanLaunchArgs.add("-e")
        titanLaunchArgs.add("TITAN_IMAGE=titan:latest")
        titanLaunchArgs.add("-e")
        titanLaunchArgs.add("TITAN_IDENTITY=titan-$identity")
        return run("titan:latest", "/bin/bash /titan/launch", titanLaunchArgs)
    }

    fun teardownTitanServers(): String {
        titanLaunchArgs.removeAt(titanLaunchArgs.indexOf("-d"))
        titanLaunchArgs.removeAt(titanLaunchArgs.indexOf("--restart"))
        titanLaunchArgs.removeAt(titanLaunchArgs.indexOf("always"))
        titanLaunchArgs.removeAt(titanLaunchArgs.indexOf("--name=titan-$identity-launch"))
        titanLaunchArgs.add("-e")
        titanLaunchArgs.add("TITAN_IDENTITY=titan-$identity")
        titanLaunchArgs.add("--rm")
        return run("titan:latest", "/bin/bash /titan/teardown", titanLaunchArgs)
    }

    fun launchTitanKubernetesServers(titanServerVersion: String): String {
        var config = System.getenv("TITAN_CONFIG") ?: ""
        if (!config.contains("titanImage")) {
            if (config != "") {
                config += ","
            }
            config += "titanImage=titandata/titan:$titanServerVersion"
        }
        val launchArgs = titanLaunchKubernetesArgs.toMutableList()
        launchArgs.add("-e")
        launchArgs.add("TITAN_CONFIG=$config")
        return run("titan:latest", "/bin/bash /titan/run", launchArgs)
    }

    fun pull(image: String): String {
        return executor.exec(listOf("docker", "pull", image)).trim()
    }

    fun tag(source: String, target: String): String {
        return executor.exec(listOf("docker", "tag", source, target)).trim()
    }

    fun rm(container: String, force: Boolean): String {
        var argList = mutableListOf<String>(
                "docker", "rm", "-f", container
        )
        if (!force) {
            argList.remove("-f")
        }
        return executor.exec(argList).trim()
    }

    fun rmStopped(container: String): String {
        val containerId = executor.exec(listOf("docker", "ps", "-a", "-f", "name=^/$container\$", "--format", "{{.ID}}")).trim()
        return executor.exec(listOf("docker", "container", "rm", containerId))
    }

    fun removeTitanImages(version: String): String {
        val imageId = executor.exec(listOf("docker", "images", "titan:$version", "--format", "{{.ID}}")).trim()
        return executor.exec(listOf("docker", "rmi", imageId, "-f"))
    }

    fun run(image: String, entry: String, arguments: List<String>): String {
        val mutArgs = mutableListOf("docker", "run")
        mutArgs.addAll(arguments)
        mutArgs.addAll(image.toList())
        if (entry.isNotEmpty()) mutArgs.addAll(entry.toList())
        return executor.exec(mutArgs)
    }

    fun inspectContainer(container: String): JSONObject? {
        val results = executor.exec(listOf("docker", "inspect", "--type", "container", container))
        return JSONArray(results).optJSONObject(0)
    }

    fun fetchLogs(container: String) {
        val lines = executor.exec(listOf("docker", "logs", container)).lines()
        for (line in lines) {
            if (!this.logs.containsKey(line) && !line.isNullOrEmpty()) {
                this.logs[line] = false
            }
        }
    }

    fun inspectImage(image: String): JSONObject? {
        val results = executor.exec(listOf("docker", "inspect", "--type", "image", image))
        return JSONArray(results).optJSONObject(0)
    }

    fun createVolume(name: String, path: String, driver: String = "titan-$identity"): String {
        return executor.exec(listOf("docker", "volume", "create", "-d", driver, "-o", "path=$path", name))
    }

    fun removeVolume(name: String, force: Boolean = false): String {
        var argList = mutableListOf<String>(
                "docker", "volume", "rm", "-f", name
        )
        if (!force) {
            argList.remove("-f")
        }
        return executor.exec(argList)
    }

    fun cp(source: String, target: String): String {
        return executor.exec(listOf("docker", "cp", "-a", "$source/.", "titan-$identity-server:/var/lib/titan/mnt/$target"))
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
            val rawArguments = this.removePrefix("[").removeSuffix("]").toList(", ")
            val returnArgs = mutableListOf<String>()
            for (arg in rawArguments) {
                if (arg != "--mount" && !arg.contains("type=volume")) {
                    returnArgs.add(arg)
                }
            }
            return returnArgs
        }

        fun List<String>.fetchName(): String {
            return when {
                this.contains("--name") -> this[(this.indexOf("--name") + 1)]
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
