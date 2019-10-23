package io.titandata.titan.providers

class Mock: Provider {
    override fun checkInstall() {
        println("Mock Provider Installed")
    }

    override fun pull(container: String, commit: String?, remoteName: String?) {
        println("Pulling from remote")
    }

    override fun push(container: String, commit: String?, remoteName: String?) {
        println("Pushing to remote")
    }

    override fun commit(container: String, message: String, tags: List<String>) {
        println("Committing new state")
    }

    override fun install(registry: String?) {
        println("Initializing new repository")
    }

    override fun abort(container: String) {
        println("Aborting current operation")
    }

    override fun status(container: String) {
        println("Display current status")
    }

    override fun remoteAdd(container: String, uri: String, remoteName: String?) {
        println("Add remote")
    }

    override fun remoteLog(container: String, remoteName: String?, tags: List<String>) {
        println("Display remote log")
    }

    override fun remoteList(container: String) {
        println("Display list of remotes")
    }

    override fun remoteRemove(container: String, remote: String) {
        println("Removing $remote for $container")
    }

    override fun migrate(container: String, name: String) {
        println("Migrating $container to $name controlled environment")
    }

    override fun run(arguments: List<String>) {
        println("Running controlled container")
    }

    override fun uninstall(force: Boolean) {
        println("Tearing down containers")
    }

    override fun upgrade(force: Boolean, version: String, finalize: Boolean, path: String?) {
        println("Upgrading to $version")
    }

    override fun checkout(container: String, guid: String) {
        println("Checking out data set $guid")
    }

    override fun list() {
        println("List containers")
    }

    override fun log(container: String, logs: List<String>) {
        println("Log for $container")
    }

    override fun stop(container: String) {
        println("Stopping $container")
    }

    override fun start(container: String) {
        println("Starting $container")
    }

    override fun remove(container: String, force: Boolean) {
        println("Removing $container")
    }

    override fun cp(container: String, driver: String, source: String, path: String) {
        println("copying data into $container with $driver from $source")
    }

    override fun clone(uri: String, container: String?, commit: String?) {
        println("cloning $container from $uri")
    }

    override fun delete(repository: String, commit: String?, tags: List<String>) {
        println("deleting $commit from $repository")
    }
}
