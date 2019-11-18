/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers

import io.titandata.client.apis.RepositoriesApi
import kotlin.system.exitProcess
import io.titandata.titan.clients.Docker
import io.titandata.titan.exceptions.CommandException
import io.titandata.titan.utils.CommandExecutor
import io.titandata.titan.providers.kubernetes.*

class Kubernetes: Provider {
    private val titanServerVersion = "0.6.3"
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

    override fun pull(container: String, commit: String?, remoteName: String?, tags: List<String>, metadataOnly: Boolean) {
        TODO("not implemented")
    }

    override fun push(container: String, commit: String?, remoteName: String?, tags: List<String>, metadataOnly: Boolean) {
        TODO("not implemented")
    }

    override fun commit(container: String, message: String, tags: List<String>) {
        TODO("not implemented")
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
        TODO("not implemented")
    }

    override fun status(container: String) {
        TODO("not implemented")
    }

    override fun remoteAdd(container: String, uri: String, remoteName: String?, params: Map<String, String>) {
        TODO("not implemented")
    }

    override fun remoteLog(container: String, remoteName: String?, tags: List<String>) {
        TODO("not implemented")
    }

    override fun remoteList(container: String) {
        TODO("not implemented")
    }

    override fun remoteRemove(container: String, remote: String) {
        TODO("not implemented")
    }

    override fun migrate(container: String, name: String) {
        TODO("not implemented")
    }

    override fun run(arguments: List<String>) {
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
        TODO("not implemented")
    }

    override fun tag(repository: String, commit: String, tags: List<String>) {
        TODO("not implemented")
    }

    override fun list() {
        System.out.printf("%-20s  %s${n}", "REPOSITORY", "STATUS")
        for (container in getContainersStatus()) {
            System.out.printf("%-20s  %s${n}", container.name, container.status)
        }
    }

    override fun log(container: String, tags: List<String>) {
        TODO("not implemented")
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
        TODO("not implemented")
    }

    override fun clone(uri: String, container: String?, commit: String?, params: Map<String, String>) {
        TODO("not implemented")
    }
}
