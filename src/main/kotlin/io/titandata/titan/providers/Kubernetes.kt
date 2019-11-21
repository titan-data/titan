/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers

import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.CoreV1Api
import io.kubernetes.client.util.Config
import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.OperationsApi
import io.titandata.client.apis.RemotesApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.apis.VolumesApi
import io.titandata.serialization.RemoteUtil
import kotlin.system.exitProcess
import io.titandata.titan.clients.Docker
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.providers.generic.Abort
import io.titandata.titan.providers.generic.Commit
import io.titandata.titan.providers.generic.Delete
import io.titandata.titan.providers.generic.Log
import io.titandata.titan.providers.generic.Pull
import io.titandata.titan.providers.generic.Push
import io.titandata.titan.providers.generic.RemoteAdd
import io.titandata.titan.providers.generic.RemoteList
import io.titandata.titan.providers.generic.RemoteLog
import io.titandata.titan.providers.generic.RemoteRemove
import io.titandata.titan.providers.generic.Status
import io.titandata.titan.providers.generic.Tag
import io.titandata.titan.utils.CommandExecutor
import io.titandata.titan.providers.kubernetes.*

class Kubernetes: Provider {
    private val titanServerVersion = "0.6.5"
    private val dockerRegistryUrl = "titandata"

    private var coreApi: CoreV1Api
    private val commandExecutor = CommandExecutor()
    private val docker = Docker(commandExecutor, Identity)
    private val kubernetes = io.titandata.titan.clients.Kubernetes()
    private val repositoriesApi = RepositoriesApi("http://localhost:${Port}")
    private val operationsApi = OperationsApi("http://localhost:${Port}")
    private val remotesApi = RemotesApi("http://localhost:${Port}")
    private val commitsApi = CommitsApi("http://localhost:${Port}")
    private val volumesApi = VolumesApi("http://localhost:${Port}")

    private val n = System.lineSeparator()

    companion object {
        val Identity = "titan-k8s"
        val Port = 5002
    }

    init {
        val client = Config.defaultClient()
        Configuration.setDefaultApiClient(client)
        coreApi = CoreV1Api()
    }

    private fun exit(message:String, code: Int = 1) {
        if (message != "") {
            println(message)
        }
        exitProcess(code)
    }

    override fun checkInstall() {
        val checkInstallCommand = CheckInstall(::exit, commandExecutor, docker)
        return checkInstallCommand.checkInstall()
    }

    override fun pull(container: String, commit: String?, remoteName: String?, tags: List<String>,
                      metadataOnly: Boolean) {
        val pullCommand = Pull(::exit, remotesApi, operationsApi)
        return pullCommand.pull(container, commit, remoteName, tags, metadataOnly)
    }

    override fun push(container: String, commit: String?, remoteName: String?, tags: List<String>,
                      metadataOnly: Boolean) {
        val pushCommand = Push(::exit, commitsApi, remotesApi, operationsApi, RemoteUtil(), repositoriesApi)
        return pushCommand.push(container, commit, remoteName, tags, metadataOnly)
    }

    override fun commit(container: String, message: String, tags: List<String>) {
        try {
            val user= commandExecutor.exec(listOf("git", "config", "user.name")).trim()
            val email = commandExecutor.exec(listOf("git", "config", "user.email")).trim()
            val commitCommand = Commit(user, email, repositoriesApi, commitsApi)
            return commitCommand.commit(container, message, tags)
        } catch (e: CommandException) {
            exit("Git not configured.")
        }
    }

    override fun install(registry: String?, verbose: Boolean) {
        val regVal = if (registry.isNullOrEmpty()) {
            dockerRegistryUrl
        } else {
            registry
        }
        val installCommand = Install(titanServerVersion, regVal, verbose, commandExecutor, docker)
        return installCommand.install()
    }

    override fun abort(container: String) {
        val abortCommand = Abort(::exit, operationsApi)
        return abortCommand.abort(container)
    }

    override fun status(container: String) {
        TODO("not implemented")
    }

    override fun remoteAdd(container:String, uri: String, remoteName: String?, params: Map<String, String>) {
        val remoteAddCommand = RemoteAdd(::exit, repositoriesApi, remotesApi)
        return remoteAddCommand.remoteAdd(container, uri, remoteName, params)
    }

    override fun remoteLog(container:String, remoteName: String?, tags: List<String>) {
        val remoteLogCommand = RemoteLog(::exit, remotesApi)
        return remoteLogCommand.remoteLog(container, remoteName, tags)
    }

    override fun remoteList(container: String) {
        val remoteListCommand = RemoteList(remotesApi)
        return remoteListCommand.list(container)
    }

    override fun remoteRemove(container: String, remote: String) {
        val remoteRemoveCommand = RemoteRemove(remotesApi)
        return remoteRemoveCommand.remove(container, remote)
    }

    override fun migrate(container: String, name: String) {
        throw NotImplementedError("migrate is not supported in kubernetes context")
    }

    override fun run(arguments: List<String>) {
        val runCommand = Run(::exit, commandExecutor, docker, kubernetes, repositoriesApi, volumesApi)
        return runCommand.run(arguments)
    }

    override fun uninstall(force: Boolean) {
        val uninstallCommand = Uninstall(titanServerVersion, ::exit, ::remove,  commandExecutor, docker, repositoriesApi)
        return uninstallCommand.uninstall(force)
    }

    override fun upgrade(force: Boolean, version: String, finalize: Boolean, path: String?) {
        TODO("not implemented")
    }

    override fun checkout(container: String, guid: String?, tags: List<String>) {
        TODO("not implemented")
    }

    override fun delete(repository: String, commit: String?, tags: List<String>) {
        val deleteCommand = Delete(commitsApi)
        if (!commit.isNullOrEmpty()) {
            if (!tags.isEmpty()) {
                return deleteCommand.deleteTags(repository, commit, tags)
            } else {
                return deleteCommand.deleteCommit(repository, commit)
            }
        }
        println("No object found to delete.")
    }

    override fun tag(repository: String, commit: String, tags: List<String>) {
        val tagCommand = Tag(commitsApi)
        return tagCommand.tagCommit(repository, commit, tags)
    }

    override fun list() {
        System.out.printf("%-20s  %s${n}", "REPOSITORY", "STATUS")
        for (repo in repositoriesApi.listRepositories()) {
            System.out.printf("%-20s${n}", repo.name)
        }
    }

    override fun log(container: String, tags: List<String>) {
        val logCommand = Log(commitsApi)
        return logCommand.log(container, tags)
    }

    override fun stop(container: String) {
        TODO("not implemented")
    }

    override fun start(container: String) {
        TODO("not implemented")
    }

    override fun remove(container: String, force: Boolean) {
        TODO("not implemented")
    }

    override fun cp(container: String, driver: String, source: String, path: String) {
        throw NotImplementedError("cp is not supported in kuberentes context")
    }

    override fun clone(uri: String, container: String?, commit: String?, params: Map<String, String>) {
        TODO("not implemented")
    }
}
