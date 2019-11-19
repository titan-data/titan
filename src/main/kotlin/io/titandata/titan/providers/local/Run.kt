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
        container: String,
        repository: String?,
        environments: List<String>,
        parameters: List<String>,
        disablePortMapping: Boolean,
        createRepo: Boolean = true
    ) {

        if(!repository.isNullOrEmpty() && repository.contains("/")) {
            exit("Repository name cannot contain a slash",1)
        }

        val containerName = when {
            repository.isNullOrEmpty() -> container
            else -> repository
        }
        val image = when{
            container.contains(":") -> container.split(":")[0]
            else -> container
        }
        val tag =  when {
            container.contains(":") -> container.split(":")[1]
            else -> "latest"
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

        val argList = mutableListOf("-d", "--label","io.titandata.titan")
        for ((index, path) in volumes.keys().withIndex()) {
            val volumeName = "$containerName/v$index"
            println("Creating docker volume $volumeName with path $path")
            docker.createVolume(volumeName, path)
            argList.add("--mount")
            argList.add("type=volume,src=$volumeName,dst=$path,volume-driver=titan")
        }
        argList.add("--name")
        argList.add(containerName)

        if (!disablePortMapping) {
            val exposedPorts = imageInfo.getJSONObject("Config").optJSONObject("ExposedPorts")
            for (rawPort in exposedPorts.keys()) {
                val port = rawPort.split("/")[0]
                argList.add("-p")
                argList.add("$port:$port")
            }
        }

        for (env in environments) {
            argList.add("--env")
            argList.add(env)
        }

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
    }
}