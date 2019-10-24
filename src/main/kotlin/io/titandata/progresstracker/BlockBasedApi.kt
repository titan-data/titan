package io.titandata.progresstracker


object BlockBasedApi {

    inline fun <T> trackTask(taskName: String, body: () -> T): T {
        io.titandata.progresstracker.ApplicationProgressTracker.progressTracker.pushTask(taskName)
        return body()
    }

    inline fun <T> trackLastTask(taskName: String, body: () -> T): T {
        io.titandata.progresstracker.ApplicationProgressTracker.progressTracker.pushTask(taskName)
        val output = body()
        io.titandata.progresstracker.ApplicationProgressTracker.progressTracker.end()
        return output
    }

}
