package io.titandata.progresstracker


object StringBasedApi {

    fun String.trackTask(taskName: String = ""): String {
        if (taskName.isNotEmpty())
            io.titandata.progresstracker.ApplicationProgressTracker.progressTracker.pushTask(taskName)
        else
            io.titandata.progresstracker.ApplicationProgressTracker.progressTracker.pushTask(this)
        return this
    }

    fun String.endTask(): String {
        io.titandata.progresstracker.ApplicationProgressTracker.progressTracker.end()
        return this
    }

    fun String.markAsFailed(): String {
        io.titandata.progresstracker.ApplicationProgressTracker.progressTracker.markAsFailed()
        return this
    }

}
