package io.titandata.progresstracker

import java.io.Closeable

interface ProgressTracker: Closeable, AutoCloseable {
    fun pushTask(taskName: String)
    fun markAsFailed()
    fun end()
}