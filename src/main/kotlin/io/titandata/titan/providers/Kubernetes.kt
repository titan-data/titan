/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers

import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.OperationsApi
import io.titandata.client.apis.RemotesApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.apis.VolumesApi
import io.titandata.serialization.RemoteUtil
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
import io.titandata.titan.providers.generic.RuntimeStatus
import io.titandata.titan.providers.generic.Status
import io.titandata.titan.providers.generic.Tag
import io.titandata.titan.providers.generic.Upgrade
import io.titandata.titan.providers.kubernetes.CheckInstall
import io.titandata.titan.providers.kubernetes.Checkout
import io.titandata.titan.providers.kubernetes.Install
import io.titandata.titan.providers.kubernetes.Remove
import io.titandata.titan.providers.kubernetes.Run
import io.titandata.titan.providers.kubernetes.Start
import io.titandata.titan.providers.kubernetes.Stop
import io.titandata.titan.providers.kubernetes.Uninstall
import io.titandata.titan.utils.CommandExecutor
import io.titandata.titan.utils.HttpHandler
import kotlin.system.exitProcess

class Kubernetes : Provider {
    private val titanServerVersion = "0.6.6"
    private val dockerRegistryUrl = "titandata"

    private val httpHandler = HttpHandler()
    private val commandExecutor = CommandExecutor()
    private val docker = Docker(commandExecutor, Identity)
    private val kubernetes = io.titandata.titan.clients.Kubernetes()
    private val repositoriesApi = RepositoriesApi("http://localhost:$Port")
    private val operationsApi = OperationsApi("http://localhost:$Port")
    private val remotesApi = RemotesApi("http://localhost:$Port")
    private val commitsApi = CommitsApi("http://localhost:$Port")
    private val volumesApi = VolumesApi("http://localhost:$Port")

    private val n = System.lineSeparator()

    companion object {
        val Identity = "titan-k8s"
        val Port = 5002
    }

    private fun exit(message: String, code: Int = 1) {
        if (message != "") {
            println(message)
        }
        exitProcess(code)
    }
    private fun getRuntimeStatus(): List<RuntimeStatus> {
        val returnList = mutableListOf<RuntimeStatus>()
        val repositories = repositoriesApi.listRepositories()
        for (repo in repositories) {
            val (status) = kubernetes.getStatefulSetStatus(repo.name)
            returnList.add(RuntimeStatus(repo.name, status))
        }
        return returnList
    }

    override fun checkInstall() {
        val checkInstallCommand = CheckInstall(::exit, commandExecutor, docker)
        return checkInstallCommand.checkInstall()
    }

    override fun pull(
        container: String,
        commit: String?,
        remoteName: String?,
        tags: List<String>,
        metadataOnly: Boolean
    ) {
        val pullCommand = Pull(::exit, remotesApi, operationsApi)
        return pullCommand.pull(container, commit, remoteName, tags, metadataOnly)
    }

    override fun push(
        container: String,
        commit: String?,
        remoteName: String?,
        tags: List<String>,
        metadataOnly: Boolean
    ) {
        val pushCommand = Push(::exit, commitsApi, remotesApi, operationsApi, RemoteUtil(), repositoriesApi)
        return pushCommand.push(container, commit, remoteName, tags, metadataOnly)
    }

    override fun commit(container: String, message: String, tags: List<String>) {
        try {
            val user = commandExecutor.exec(listOf("git", "config", "user.name")).trim()
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
        val statusCommand = Status(::getRuntimeStatus, repositoriesApi, volumesApi)
        return statusCommand.status(container)
    }

    override fun remoteAdd(container: String, uri: String, remoteName: String?, params: Map<String, String>) {
        val remoteAddCommand = RemoteAdd(::exit, repositoriesApi, remotesApi)
        return remoteAddCommand.remoteAdd(container, uri, remoteName, params)
    }

    override fun remoteLog(container: String, remoteName: String?, tags: List<String>) {
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

    override fun run(image: String, repository: String?, environments: List<String>, arguments: List<String>, disablePortMapping: Boolean) {
        val runCommand = Run(::exit, commandExecutor, docker, kubernetes, repositoriesApi, volumesApi)
        return runCommand.run(image, repository, environments, arguments, disablePortMapping)
    }

    override fun uninstall(force: Boolean) {
        val uninstallCommand = Uninstall(titanServerVersion, ::exit, ::remove, commandExecutor, docker, repositoriesApi)
        return uninstallCommand.uninstall(force)
    }

    override fun upgrade(force: Boolean, version: String, finalize: Boolean, path: String?) {
        val upgradeCommand = Upgrade(::start, ::stop, ::exit, ::getRuntimeStatus, commandExecutor, httpHandler)
        return upgradeCommand.upgrade(force, version, finalize, path)
    }

    override fun checkout(container: String, guid: String?, tags: List<String>) {
        val checkoutCommand = Checkout(kubernetes, commitsApi, repositoriesApi, volumesApi)
        return checkoutCommand.checkout(container, guid, tags)
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
        System.out.printf("%-20s  %s$n", "REPOSITORY", "STATUS")
        for (container in getRuntimeStatus()) {
            System.out.printf("%-20s  %s$n", container.name, container.status)
        }
    }

    override fun log(container: String, tags: List<String>) {
        val logCommand = Log(commitsApi)
        return logCommand.log(container, tags)
    }

    override fun stop(container: String) {
        val stopCommand = Stop(kubernetes, repositoriesApi)
        return stopCommand.stop(container)
    }

    override fun start(container: String) {
        val startCommand = Start(kubernetes, repositoriesApi)
        return startCommand.start(container)
    }

    override fun remove(container: String, force: Boolean) {
        val removeCommand = Remove(kubernetes, repositoriesApi, volumesApi)
        return removeCommand.remove(container, force)
    }

    override fun cp(container: String, driver: String, source: String, path: String) {
        throw NotImplementedError("cp is not supported in kuberentes context")
    }

    override fun clone(uri: String, container: String?, commit: String?, params: Map<String, String>, arguments: List<String>, disablePortMapping: Boolean) {
        TODO("not implemented")
    }
}
