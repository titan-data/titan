/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.RepositoriesApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.clients.Docker.Companion.fetchName
import io.titandata.titan.clients.Docker.Companion.hasDetach
import io.titandata.models.Repository
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.utils.CommandExecutor
import org.json.JSONObject

class Run (
    private val exit: (message: String, code: Int) -> Unit,
    private val commandExecutor: CommandExecutor = CommandExecutor(),
    private val docker: Docker = Docker(commandExecutor),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi()
) {
    fun run(
        repository: String?,
        environments: List<String>,
        parameters: List<String>,
        disablePortMapping: Boolean,
        createRepo: Boolean = true
    ) {
        /*
        if (!arguments.hasDetach()) {
            exit("No detach (-d or --detach) argument found, only detached containers are supported",1)
        }
        val containerName = arguments.fetchName()
        if(containerName.contains("/")) {
            exit("Container name cannot contain a slash",1)
        }
        val imageArray = arguments.last().split(":")
        val image = imageArray[0]
        val tag = if (imageArray[1].isNullOrEmpty()) {
            "latest"
        } else {
            imageArray[1]
        }

        var imageInfo: JSONObject? = null
        try {
             imageInfo = docker.inspectImage("$image:$tag")
        } catch (e: CommandException) {
            docker.pull("$image:$tag")
            imageInfo = docker.inspectImage("$image:$tag")
        }

        if (imageInfo == null) {
            exit("Image information is not available",1)
        }
        val volumes = imageInfo!!.getJSONObject("Config").optJSONObject("Volumes")
        if (volumes == null) {
            exit("No volumes found for image $image",1)
        }
        println("Creating repository $containerName")
        val repo = Repository(containerName, emptyMap())
        if (createRepo) {
            repositoriesApi.createRepository(repo)
        }
        val argList = mutableListOf("--label","io.titandata.titan")
        for ((index, path) in volumes.keys().withIndex()) {
            val volumeName = "$containerName/v$index"
            println("Creating docker volume $volumeName with path $path")

            docker.createVolume(volumeName, path)

            argList.add("--mount")
            argList.add("type=volume,src=$volumeName,dst=$path,volume-driver=titan")
        }
        val argumentEdit= arguments.toMutableList()
        if (argumentEdit.contains("--name")) {
            argumentEdit.removeAt((argumentEdit.indexOf("--name") + 1))
            argumentEdit.removeAt(argumentEdit.indexOf("--name"))
        }
        if (argumentEdit.contains("$image:$tag")) {
            argumentEdit.removeAt(argumentEdit.indexOf("$image:$tag"))
        }
        argList.add("--name")
        argList.add(containerName)
        argList.addAll(argumentEdit)
        val repoDigest = imageInfo.optJSONArray("RepoDigests").optString(0)
        val dockerRunCommand = if(repoDigest.isNullOrEmpty()) {
            "$image:$tag"
        } else  {
            repoDigest
        }
        val metadata = mapOf(
                "container" to dockerRunCommand,
                "image" to image,
                "tag" to tag,
                "digest" to repoDigest,
                "runtime" to argList.toString()
        )

        val updateRepo = Repository(containerName, metadata)
        repositoriesApi.updateRepository(containerName, updateRepo)
        docker.run(dockerRunCommand, "", argList)
        println("Running controlled container $containerName")

         */
    }
}