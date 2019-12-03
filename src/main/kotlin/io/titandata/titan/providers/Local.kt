/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers

import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.ContextApi
import io.titandata.client.apis.OperationsApi
import io.titandata.client.apis.RemotesApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.apis.VolumesApi
import io.titandata.serialization.RemoteUtil
import io.titandata.titan.clients.Docker
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.providers.generic.Abort
import io.titandata.titan.providers.generic.Clone
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
import io.titandata.titan.providers.local.CheckInstall
import io.titandata.titan.providers.local.Checkout
import io.titandata.titan.providers.local.Cp
import io.titandata.titan.providers.local.Install
import io.titandata.titan.providers.local.Migrate
import io.titandata.titan.providers.local.Remove
import io.titandata.titan.providers.local.Run
import io.titandata.titan.providers.local.Start
import io.titandata.titan.providers.local.Stop
import io.titandata.titan.providers.local.Uninstall
import io.titandata.titan.utils.CommandExecutor
import io.titandata.titan.utils.HttpHandler
import kotlin.system.exitProcess

class Local(val contextName: String = "local", val host: String = "localhost", val port: Int = 5001) : Provider {
    private val titanServerVersion = "0.7.0"
    private val dockerRegistryUrl = "titandata"
    private val uri = "http://$host:$port"

    private val httpHandler = HttpHandler()
    private val commandExecutor = CommandExecutor()
    private val docker = Docker(commandExecutor, contextName, port)
    private val repositoriesApi = RepositoriesApi(uri)
    private val operationsApi = OperationsApi(uri)
    private val remotesApi = RemotesApi(uri)
    private val commitsApi = CommitsApi(uri)
    private val volumesApi = VolumesApi(uri)
    private val contextApi = ContextApi(uri)
    private val n = System.lineSeparator()

    private fun exit(message: String, code: Int = 1) {
        if (message != "") {
            println(message)
        }
        exitProcess(code)
    }

    private fun getContainersStatus(): List<RuntimeStatus> {
        val returnList = mutableListOf<RuntimeStatus>()
        val repositories = repositoriesApi.listRepositories()
        for (repo in repositories) {
            val container = repo.name
            var status = "detached"
            try {
                val containerInfo = docker.inspectContainer(container)!!
                status = containerInfo.getJSONObject("State").getString("Status")
            } catch (e: CommandException) {}
            returnList.add(RuntimeStatus(container, status))
        }
        return returnList
    }

    override fun getType(): String {
        return "local"
    }

    override fun getProperties(): Map<String, String> {
        return contextApi.getContext().properties
    }

    override fun repositoryExists(repository: String): Boolean {
        try {
            repositoriesApi.getRepository(repository)
        } catch (t: Throwable) {
            return false
        }
        return true
    }

    override fun upgrade(force: Boolean, version: String, finalize: Boolean, path: String?) {
        val upgradeCommand = Upgrade(::start, ::stop, ::exit, ::getContainersStatus, commandExecutor, httpHandler)
        return upgradeCommand.upgrade(force, version, finalize, path)
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

    override fun checkInstall() {
        val checkInstallCommand = CheckInstall(::exit, commandExecutor, docker)
        return checkInstallCommand.checkInstall()
    }

    override fun install(properties: Map<String, String>, verbose: Boolean) {
        val regVal = properties.get("registry") ?: dockerRegistryUrl
        val installCommand = Install(titanServerVersion, regVal, verbose, commandExecutor, docker)
        installCommand.install()
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

    override fun abort(container: String) {
        val abortCommand = Abort(::exit, operationsApi)
        return abortCommand.abort(container)
    }

    override fun status(container: String) {
        val statusCommand = Status(::getContainersStatus, repositoriesApi, volumesApi)
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
        val migrateCommand = Migrate(::exit, ::commit, commandExecutor, docker, repositoriesApi, volumesApi)
        return migrateCommand.migrate(container, name)
    }

    override fun run(image: String, repository: String?, environments: List<String>, arguments: List<String>, disablePortMapping: Boolean) {
        val runCommand = Run(::exit, commandExecutor, docker, repositoriesApi)
        return runCommand.run(image, repository, environments, arguments, disablePortMapping)
    }

    override fun list(context: String) {
        for (container in getContainersStatus()) {
            System.out.printf("%-10s %-20s  %s$n", context, container.name, container.status)
        }
    }

    override fun uninstall(force: Boolean) {
        val uninstallCommand = Uninstall(titanServerVersion, ::exit, ::remove, commandExecutor, docker, repositoriesApi)
        return uninstallCommand.uninstall(force)
    }

    override fun checkout(container: String, guid: String?, tags: List<String>) {
        val checkoutCommand = Checkout(commandExecutor, docker, commitsApi, repositoriesApi)
        return checkoutCommand.checkout(container, guid, tags)
    }

    override fun log(container: String, tags: List<String>) {
        val logCommand = Log(commitsApi)
        return logCommand.log(container, tags)
    }

    override fun stop(container: String) {
        val stopCommand = Stop(commandExecutor, docker)
        return stopCommand.stop(container)
    }

    override fun start(container: String) {
        val startCommand = Start(commandExecutor, docker)
        return startCommand.start(container)
    }

    override fun remove(container: String, force: Boolean) {
        val removeCommand = Remove(::exit, commandExecutor, docker, repositoriesApi, volumesApi)
        return removeCommand.remove(container, force)
    }

    override fun cp(container: String, driver: String, source: String, path: String) {
        val cpCommand = Cp(::exit, ::start, ::stop, commandExecutor, docker, volumesApi)
        return cpCommand.cp(container, driver, source, path)
    }

    override fun clone(uri: String, container: String?, commit: String?, params: Map<String, String>, arguments: List<String>, disablePortMapping: Boolean) {
        val runCommand = Run(::exit, commandExecutor, docker, repositoriesApi)
        val cloneCommand = Clone(::remoteAdd, ::pull, ::checkout, runCommand::run, ::remove, commandExecutor, docker, remotesApi, repositoriesApi)
        return cloneCommand.clone(uri, container, commit, params, arguments, disablePortMapping)
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
}
