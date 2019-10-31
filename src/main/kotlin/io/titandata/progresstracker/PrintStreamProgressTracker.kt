package io.titandata.progresstracker

import java.io.PrintStream
import kotlin.concurrent.thread


private const val DARK_GREEN = "\u001B[0;32m"
private const val DARK_RED = "\u001B[0;31m"
private const val BRIGHT_YELLOW = "\u001B[1;33m"

private const val DEFAULT_COLOR = "\u001B[0m"


class PrintStreamProgressTracker(
    private val successMessage: String,
    private val failureMessage: String = "Failed",
    private val tasksPrefix: String = "",
    private val printStream: PrintStream = System.out,
    private val animation: Array<Char> = arrayOf('-', '\\', '|', '/', '-', '\\', '|', '/'),
    private val refreshRateInMillis: Long = 250L): ProgressTracker
{
    private val tasks: MutableList<String> = ArrayList(2)
    private var clearTasks: Boolean = false
    private var isMonitorStarted: Boolean = false
    private val monitor: Thread = createMonitor()
    private var failedTask: String = ""
    private var hasFailed = false

    private val n = System.lineSeparator()

    override fun pushTask(taskName: String) {
        tasks.add(taskName)
        startMonitorIfNeeded()
    }

    override fun end() {
        clearTasks = true
        monitor.interrupt()
    }

    override fun close() {
        end()
    }

    override fun markAsFailed() {
        failedTask = tasks.last()
        hasFailed = true
        monitor.interrupt()
    }

    private fun startMonitorIfNeeded() {
        if (!isMonitorStarted) {
            isMonitorStarted = true
            monitor.start()
        }
    }

    private fun createMonitor(): Thread {
        return thread(start = false) {
            while(tasks.isNotEmpty()) {
                for(frame in animation) {
                    printCurrentTask(frame)
                    if(shouldBreak()) {
                        break
                    }
                }
            }
            printExitMessage()
        }
    }

    private fun printCurrentTask(frame: Char) {
        if (isCurrentTaskDone()) {
            printDone(tasks.removeAt(0))
        } else {
            printInProgress(frame, tasks[0])
        }
    }

    private fun printInProgress(frame: Char, taskName: String) {
        printStream.print("\r$tasksPrefix$BRIGHT_YELLOW$frame $taskName$DEFAULT_COLOR")
    }

    private fun printDone(taskName: String) {
        val taskIsSuccess = failedTask != taskName
        if (taskIsSuccess)
            printStream.print("$tasksPrefix$DARK_GREEN√ $taskName$DEFAULT_COLOR${n}")
        else
            printStream.print("$tasksPrefix${DARK_RED}X $taskName$DEFAULT_COLOR${n}")
    }

    private fun waitMillis(millis: Long): Boolean {
        return try {
            if(!clearTasks)
                Thread.sleep(millis)
            true
        } catch (ex:InterruptedException) {
            clearTasks = true
            false
        }
    }

    private fun shouldBreak(): Boolean {
        return !waitMillis(refreshRateInMillis) || tasks.isEmpty()
    }

    private fun isCurrentTaskDone(): Boolean {
        return tasks.size >= 2 || (tasks.size == 1 && clearTasks)
    }

    private fun printExitMessage() {
        if(!hasFailed)
            printStream.println(successMessage)
        else
            printStream.println(failureMessage)
    }

}
