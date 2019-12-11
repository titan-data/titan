package io.titandata.titan.providers

class Mock : Provider {
    override fun getType(): String {
        return "mock"
    }

    override fun getProperties(): Map<String, String> {
        return emptyMap()
    }

    override fun repositoryExists(repository: String): Boolean {
        return true
    }

    override fun getName(): String {
        return "mock"
    }

    override fun getPort(): Int {
        return 0
    }

    override fun pull(container: String, commit: String?, remoteName: String?, tags: List<String>, metadataOnly: Boolean) {
        println("Pulling from remote")
    }

    override fun push(container: String, commit: String?, remoteName: String?, tags: List<String>, metadataOnly: Boolean) {
        println("Pushing to remote")
    }

    override fun commit(container: String, message: String, tags: List<String>) {
        println("Committing new state")
    }

    override fun install(properties: Map<String, String>, verbose: Boolean) {
        println("Installing infrastructure")
    }

    override fun abort(container: String) {
        println("Aborting current operation")
    }

    override fun status(container: String) {
        println("Display current status")
    }

    override fun remoteAdd(container: String, uri: String, remoteName: String?, params: Map<String, String>) {
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

    override fun run(image: String, repository: String?, environments: List<String>, arguments: List<String>, disablePortMapping: Boolean) {
        println("Running controlled image")
    }

    override fun uninstall(force: Boolean, removeImages: Boolean) {
        println("Tearing down containers")
    }

    override fun upgrade(force: Boolean, version: String, finalize: Boolean, path: String?) {
        println("Upgrading to $version")
    }

    override fun checkout(container: String, guid: String?, tags: List<String>) {
        println("Checking out data set $guid")
    }

    override fun list(context: String) {
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

    override fun clone(uri: String, container: String?, commit: String?, params: Map<String, String>, arguments: List<String>, disablePortMapping: Boolean) {
        println("cloning $container from $uri")
    }

    override fun delete(repository: String, commit: String?, tags: List<String>) {
        println("deleting $commit from $repository")
    }

    override fun tag(repository: String, commit: String, tags: List<String>) {
        println("tagging $commit in $repository")
    }
}
