package io.titandata.progresstracker


object StringAndBlockBasedApi {

    inline fun <T> String.trackTask(body: () -> T) {
        io.titandata.progresstracker.ApplicationProgressTracker.progressTracker.pushTask(this)
        body()
    }

    inline fun <T> String.trackLastTask(body: () -> T) {
        io.titandata.progresstracker.ApplicationProgressTracker.progressTracker.pushTask(this)
        body()
        io.titandata.progresstracker.ApplicationProgressTracker.progressTracker.end()
    }

}

