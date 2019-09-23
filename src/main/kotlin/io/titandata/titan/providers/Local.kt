/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers

import io.titandata.client.apis.RepositoriesApi
import kotlin.system.exitProcess
import io.titandata.titan.clients.Docker
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.utils.CommandExecutor
import io.titandata.titan.utils.HttpHandler
import io.titandata.titan.providers.local.*

data class Container (
    val name: String,
    val status: String
)

class Local: Provider {
    private val titanServerVersion = "0.4.1"
    private val dockerRegistryUrl = "titandata"

    private val httpHandler = HttpHandler()
    private val commandExecutor = CommandExecutor()
    private val docker = Docker(commandExecutor)
    private val repositoriesApi = RepositoriesApi()

    private val n = System.lineSeparator()

    private fun exit(message:String, code: Int = 1) {
        println(message)
        exitProcess(code)
    }

    private fun getContainersStatus(): List<Container> {
        val returnList = mutableListOf<Container>()
        val repositories = repositoriesApi.listRepositories()
        for (repo in repositories) {
            val container = repo.name
            val containerInfo = docker.inspectContainer(container)
            var status = "unknown"
            if (containerInfo != null) {
                status = containerInfo.getJSONObject("State").getString("Status")
            }
            returnList.add(Container(container, status))
        }
        return returnList
    }

    override fun upgrade(force: Boolean, version: String, finalize: Boolean, path: String?) {
        val upgradeCommand = Upgrade(::start, ::stop, ::exit, ::getContainersStatus, commandExecutor, httpHandler)
        return upgradeCommand.upgrade(force, version, finalize, path)
    }

    override fun pull(container: String, commit: String?, remoteName: String?) {
        val pullCommand = Pull(::exit)
        return pullCommand.pull(container, commit, remoteName)
    }

    override fun push(container: String, commit: String?, remoteName: String?) {
        val pushCommand = Push(::exit)
        return pushCommand.push(container, commit, remoteName)
    }

    override fun checkInstall() {
        val checkInstallCommand = CheckInstall(::exit, commandExecutor, docker)
        return checkInstallCommand.checkInstall()
    }

    override fun install() {
        val installCommand = Install(titanServerVersion, dockerRegistryUrl, commandExecutor, docker)
        return installCommand.install()
    }

    override fun commit(container: String, message: String) {
        try {
            val user= commandExecutor.exec(listOf("git", "config", "user.name")).trim()
            val email = commandExecutor.exec(listOf("git", "config", "user.email")).trim()
            val commitCommand = Commit(user, email)
            return commitCommand.commit(container, message)
        } catch (e: CommandException) {
            exit("Git not configured.")
        }
    }

    override fun abort(container: String) {
        val abortCommand = Abort(::exit)
        return abortCommand.abort(container)
    }

    override fun status(container: String) {
        val statusCommand = Status(::getContainersStatus)
        return statusCommand.status(container)
    }

    override fun remoteAdd(container:String, uri: String, remoteName: String?) {
        val remoteAddCommand = RemoteAdd(::exit)
        return remoteAddCommand.remoteAdd(container, uri, remoteName)
    }

    override fun remoteLog(container:String, remoteName: String?) {
        val remoteLogCommand = RemoteLog(::exit)
        return remoteLogCommand.remoteLog(container, remoteName)
    }

    override fun remoteList(container: String) {
        val remoteListCommand = RemoteList()
        return remoteListCommand.list(container)
    }

    override fun remoteRemove(container: String, remote: String) {
        val remoteRemoveCommand = RemoteRemove()
        return remoteRemoveCommand.remove(container, remote)
    }

    override fun migrate(container: String, name: String) {
        val migrateCommand = Migrate(::exit, ::commit,  commandExecutor, docker)
        return migrateCommand.migrate(container, name)
    }

    override fun run(arguments: List<String>) {
        val runCommand = Run(::exit,  commandExecutor, docker)
        return runCommand.run(arguments)
    }

    override fun list() {
        System.out.printf("%-20s  %s${n}", "CONTAINER", "STATUS")
        for (container in getContainersStatus()) {
            System.out.printf("%-20s  %s${n}", container.name, container.status)
        }
    }

    override fun uninstall(force: Boolean) {
        val uninstallCommand = Uninstall(::exit, ::remove,  commandExecutor, docker)
        return uninstallCommand.uninstall(force)
    }

    override fun checkout(container: String, guid: String) {
        val checkoutCommand = Checkout(commandExecutor, docker)
        return checkoutCommand.checkout(container, guid)
    }

    override fun log(container: String) {
        val logCommand = Log()
        return logCommand.log(container)
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
        val removeCommand = Remove(::exit, commandExecutor, docker)
        return removeCommand.remove(container, force)
    }

    override fun cp(container: String, driver: String, source: String, path: String) {
       val cpCommand = Cp(::exit, ::start, ::stop, commandExecutor, docker)
        return cpCommand.cp(container, driver, source, path)
    }

    override fun clone(uri: String, container: String?) {
        val runCommand = Run(::exit,  commandExecutor, docker)
        val cloneCommand = Clone(::remoteAdd, ::pull, ::checkout, runCommand::run, commandExecutor, docker)
        return cloneCommand.clone(uri, container)
    }
}
