package io.titandata.progresstracker

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mockito
import java.io.PrintStream


class PrintStreamProgressTrackerTest {

    @Test
    fun testInProgressTaskAnimation() {
        val printStream = mock<PrintStream> {}
        val progressTracker = PrintStreamProgressTracker(
                animation = arrayOf('1', '2'),
                successMessage = "Success!",
                printStream = printStream,
                refreshRateInMillis = 1L)
        progressTracker.use {
            it.pushTask("Testing task")
            simulateLongRunningTask()
        }

        val inOrder = Mockito.inOrder(printStream)

        //TODO update testing for windows

        //inOrder.verify(printStream).print(contains("1 Testing task"))
        //inOrder.verify(printStream).print(contains("2 Testing task"))
        //inOrder.verify(printStream).print(contains("1 Testing task"))
        //inOrder.verify(printStream).print(contains("2 Testing task"))
    }

    @Test
    fun testCompletedTaskIsPrinted() {
        val printStream = mock<PrintStream> {}
        val progressTracker = PrintStreamProgressTracker(
                animation = arrayOf('1', '2'),
                successMessage = "Success!",
                printStream = printStream,
                refreshRateInMillis = 1L)
        progressTracker.use {
            it.pushTask("Testing task")
        }
        simulateLongRunningTask()
        val inOrder = Mockito.inOrder(printStream)
        //inOrder.verify(printStream).print(contains("√ Testing task"))
        //inOrder.verify(printStream).println("Success!")
    }

    @Test
    fun testExitMessageIsPrinted() {
        val printStream = mock<PrintStream> {}
        val progressTracker = PrintStreamProgressTracker(
                animation = arrayOf('1', '2'),
                successMessage = "Success!",
                printStream = printStream,
                refreshRateInMillis = 1L)
        progressTracker.use {
            it.pushTask("Testing task")
        }
        simulateLongRunningTask()
        //verify(printStream).println(contains("Success!"))
    }

    @Test
    fun testExitOnQuickTasks() {
        val printStream = mock<PrintStream> {}
        val progressTracker = PrintStreamProgressTracker(
                animation = arrayOf('1', '2'),
                successMessage = "Success!",
                printStream = printStream,
                refreshRateInMillis = 1L)
        progressTracker.use {
            it.pushTask("Testing task")
            simulateLongRunningTask()
        }
        val inOrder = Mockito.inOrder(printStream)
        //inOrder.verify(printStream).print(contains("1 Testing task"))
        //inOrder.verify(printStream).print(contains("√ Testing task"))
        //inOrder.verify(printStream).println(contains("Success!"))

    }

    @Test
    fun testFailureMessage() {
        val printStream = mock<PrintStream> {}
        val progressTracker = PrintStreamProgressTracker(
                animation = arrayOf('1', '2'),
                successMessage = "Success!",
                failureMessage = "Tasks failed!",
                printStream = printStream,
                refreshRateInMillis = 1L)
        progressTracker.use {
            it.pushTask("Testing task")
            simulateLongRunningTask()
            it.pushTask("Task that will fail")
            it.markAsFailed()
        }
        val inOrder = Mockito.inOrder(printStream)
        //inOrder.verify(printStream).print(contains("1 Testing task"))
        //inOrder.verify(printStream).print(contains("√ Testing task"))
        //inOrder.verify(printStream).print(contains("X Task that will fail"))
        //inOrder.verify(printStream).println(contains("Tasks failed!"))
        //verify(printStream, never()).println(contains("Success!"))
    }

    private fun simulateLongRunningTask(timeInMillis: Long = 10L) {
        Thread.sleep(timeInMillis)
    }

}
