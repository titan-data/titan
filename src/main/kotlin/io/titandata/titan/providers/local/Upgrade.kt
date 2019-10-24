/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.titan.Version
import io.titandata.titan.Version.Companion.compare
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.providers.Container
import io.titandata.titan.utils.CommandExecutor
import io.titandata.titan.utils.HttpHandler
import io.titandata.titan.utils.HttpHandler.Companion.asBytes
import io.titandata.titan.utils.HttpHandler.Companion.asJsonObject
import io.titandata.titan.utils.HttpHandler.Companion.asJsonArray
import org.apache.commons.lang3.SystemUtils
import org.json.JSONObject
import java.io.File

class Upgrade(
    private val start: (container: String) -> Unit,
    private val stop: (container: String) -> Unit,
    private val exit: (message: String, code: Int) -> Unit,
    private val getContainersStatus: () -> List<Container>,
    private val executor: CommandExecutor = CommandExecutor(),
    private val httpHandler: HttpHandler = HttpHandler()
) {
    //TODO revert to HTTPS when Graal has fix the windows security bug
    private val titanVersionURL = "http://api.github.com/repos/titan-data/titan/releases/latest"
    private var assetUrl = ""

    private fun getLatestVersion(): String {
        var latestVersion = ""
        try {
            val response = httpHandler.get(titanVersionURL).asJsonObject()
            latestVersion = response.getString("tag_name")
            assetUrl = response.getString("assets_url")
        } catch(e: java.net.UnknownHostException) {
            exit("Upgrade server cannot be reached.", 1)
        }
        return latestVersion
    }

    private fun getBinaryURL(version: String): String {
        val assets = httpHandler.get(assetUrl).asJsonArray()
        var path = "titan-cli-$version-"
        path += when{
            SystemUtils.IS_OS_MAC -> "darwin_amd64.zip"
            SystemUtils.IS_OS_LINUX -> "linux_amd64.zip"
            SystemUtils.IS_OS_WINDOWS -> "todo" //TODO windows build
            else -> exit("unsupported operating system", 1)
        }
        var downloadUrl = ""
        for (item in assets) {
            val asset = item as JSONObject
            if(asset.getString("name") == path) {
                downloadUrl = asset.getString("browser_download_url")
            }
        }
        return downloadUrl
    }

    private fun getTitanBinaryPath(): String {
        return when {
            SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX -> {
                val fullpath = try {
                    executor.exec(listOf("which", "titan"))
                } catch (e: CommandException) {
                    exit("Cannot find Titan in path. Use -p to to specify the install path.", 1)
                }
                val symCheck = executor.exec(listOf("readlink", fullpath.toString().trim()))
                when {
                    !symCheck.isBlank() -> symCheck.dropLast(6).trim()
                    else -> fullpath.toString().dropLast(6).trim()
                }
            }
            SystemUtils.IS_OS_WINDOWS -> {
                "Not Yet Implemented"
            }
            else -> throw Exception("Operating system is unsupported.")
        }
    }

    private fun downloadToTemp(binaryPath: String) {
        when {
            SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX -> {
                try {
                    File("/tmp/titan-latest.zip").writeBytes(httpHandler.get(binaryPath).asBytes())
                } catch (e: java.net.UnknownHostException) {
                    exit("Upgrade server not found.", 1)
                }
            }
            SystemUtils.IS_OS_WINDOWS -> {
                println("Not Yet Implemented")
            }
            else -> throw Exception("Operating system is unsupported.")
        }
    }

    private fun extractTempToNew() {
        when {
            SystemUtils.IS_OS_MAC -> {
                executor.exec(listOf("unzip","-o","-d","/tmp/","/tmp/titan-latest.zip"))
            }
            SystemUtils.IS_OS_LINUX -> {
                executor.exec(listOf("tar","-xvf","/tmp/titan-latest.zip","-C","/tmp/"))
            }
            SystemUtils.IS_OS_WINDOWS -> {
                println("Not Yet Implemented")
            }
            else -> throw Exception("Operating system is unsupported.")
        }
    }

    private fun copyCurrentToOld(installPath: String) {
        when {
            SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX -> {
                executor.exec(listOf("cp", "$installPath/titan", "$installPath/titan_OLD"))
            }
            SystemUtils.IS_OS_WINDOWS -> {
                println("Not Yet Implemented")
            }
            else -> throw Exception("Operating system is unsupported.")
        }
    }

    private fun upgradeInfrastructure() {
        when {
            SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX -> {
                executor.exec(listOf("/tmp/titan","install"))
            }
            SystemUtils.IS_OS_WINDOWS -> {
                println("Not Yet Implemented")
            }
            else -> throw Exception("Operating system is unsupported.")
        }
    }

    private fun replaceTitanBinaries(installPath: String) {
        when {
            SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX -> {
                executor.exec(listOf("mv","/tmp/titan","$installPath/titan"))
                executor.exec(listOf("rm","$installPath/titan_OLD"))
                executor.exec(listOf("rm","/tmp/titan-latest.zip"))
            }
            SystemUtils.IS_OS_WINDOWS -> {
                println("Not Yet Implemented")
            }
            else -> throw Exception("Operating system is unsupported.")
        }
    }

    private fun finalizeUpgrade() {
        when {
            SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX -> {
                executor.exec(listOf("/tmp/titan","upgrade","--finalize"))
            }
            SystemUtils.IS_OS_WINDOWS -> {
                println("Not Yet Implemented")
            }
            else -> throw Exception("Operating system is unsupported.")
        }
    }

    fun upgrade(force: Boolean, version: String, finalize: Boolean, path: String?) {
        val restartList = mutableListOf<String>()
        if(!force){
            for (container in getContainersStatus()) {
                if (container.status == "running") {
                    exit("container ${container.name} is running, stop or use '-f' to force", 1)
                }
            }
        } else {
            for (container in getContainersStatus()) {
                if (container.status == "running") {
                    restartList.add(container.name)
                    stop(container.name)
                }
            }
        }

        val latestVersion = getLatestVersion()
        val local = Version.fromString(version)
        val latest = Version.fromString(latestVersion)
        val upgradeStatus = local.compare(latest)

        val latestPath = getBinaryURL(latestVersion)
        val installPath = if(!path.isNullOrEmpty()) path.toString() else getTitanBinaryPath()

        if (finalize) {
            println("Upgrading infrastructure")
            upgradeInfrastructure()

            println("Replacing Version")
            replaceTitanBinaries(installPath)

            for(container in restartList) {
                start(container)
            }

            exit("Titan is now $latestVersion", 0)
        }

        if (upgradeStatus == 0) exit("Titan is up to date.", 1)
        if (upgradeStatus == 1) exit("Titan is ahead of the latest version.", 1)

        println("Downloading Latest Version")
        downloadToTemp(latestPath)

        println("Extracting Archive")
        extractTempToNew()

        println("Coping current bin to OLD")
        copyCurrentToOld(installPath)

        finalizeUpgrade()
    }
}