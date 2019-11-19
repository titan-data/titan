/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers

import io.titandata.client.apis.RepositoriesApi
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

    private val commandExecutor = CommandExecutor()
    private val docker = Docker(commandExecutor, Identity)
    private val repositoriesApi = RepositoriesApi("http://localhost:${Port}")

    private val n = System.lineSeparator()

    companion object {
        val Identity = "titan-k8s"
        val Port = 5002
    }

    private fun exit(message:String, code: Int = 1) {
        if (message != "") {
            println(message)
        }
        exitProcess(code)
    }

    private fun getContainersStatus(): List<Container> {
        val returnList = mutableListOf<Container>()
        val repositories = repositoriesApi.listRepositories()
        for (repo in repositories) {
            val container = repo.name
            var status = "detached"
            try {
                val containerInfo = docker.inspectContainer(container)!!
                status = containerInfo.getJSONObject("State").getString("Status")
            } catch (e: CommandException) {}
            returnList.add(Container(container, status))
        }
        return returnList
    }

    override fun checkInstall() {
        val checkInstallCommand = CheckInstall(::exit, commandExecutor, docker)
        return checkInstallCommand.checkInstall()
    }

    override fun pull(container: String, commit: String?, remoteName: String?, tags: List<String>,
                      metadataOnly: Boolean) {
        val pullCommand = Pull(::exit)
        return pullCommand.pull(container, commit, remoteName, tags, metadataOnly)
    }

    override fun push(container: String, commit: String?, remoteName: String?, tags: List<String>,
                      metadataOnly: Boolean) {
        val pushCommand = Push(::exit)
        return pushCommand.push(container, commit, remoteName, tags, metadataOnly)
    }

    override fun commit(container: String, message: String, tags: List<String>) {
        try {
            val user= commandExecutor.exec(listOf("git", "config", "user.name")).trim()
            val email = commandExecutor.exec(listOf("git", "config", "user.email")).trim()
            val commitCommand = Commit(user, email)
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
        val abortCommand = Abort(::exit)
        return abortCommand.abort(container)
    }

    override fun status(container: String) {
        val statusCommand = Status(::getContainersStatus)
        return statusCommand.status(container)
    }

    override fun remoteAdd(container:String, uri: String, remoteName: String?, params: Map<String, String>) {
        val remoteAddCommand = RemoteAdd(::exit)
        return remoteAddCommand.remoteAdd(container, uri, remoteName, params)
    }

    override fun remoteLog(container:String, remoteName: String?, tags: List<String>) {
        val remoteLogCommand = RemoteLog(::exit)
        return remoteLogCommand.remoteLog(container, remoteName, tags)
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
        throw NotImplementedError("migrate is not supported in kubernetes context")
    }

    override fun run(repository: String?, environments: List<String>, parameters: List<String>, disablePortMapping: Boolean) {
        TODO("not implemented")
    }

    override fun uninstall(force: Boolean) {
        val uninstallCommand = Uninstall(titanServerVersion, ::exit, ::remove,  commandExecutor, docker)
        return uninstallCommand.uninstall(force)
    }

    override fun upgrade(force: Boolean, version: String, finalize: Boolean, path: String?) {
        TODO("not implemented")
    }

    override fun checkout(container: String, guid: String?, tags: List<String>) {
        TODO("not implemented")
    }

    override fun delete(repository: String, commit: String?, tags: List<String>) {
        val deleteCommand = Delete()
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
        val tagCommand = Tag()
        return tagCommand.tagCommit(repository, commit, tags)
    }

    override fun list() {
        System.out.printf("%-20s  %s${n}", "REPOSITORY", "STATUS")
        for (container in getContainersStatus()) {
            System.out.printf("%-20s  %s${n}", container.name, container.status)
        }
    }

    override fun log(container: String, tags: List<String>) {
        val logCommand = Log()
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
