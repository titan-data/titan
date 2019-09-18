package io.titandata.titan.providers.local

import io.titandata.client.apis.CommitsApi
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class LogTest {

    @Test
    fun `can instantiate`(){
        val command = Log()
        Assert.assertThat(command, CoreMatchers.instanceOf(Log::class.java))
    }

    @Test
    fun `can get log`(){
        val commitsApi: CommitsApi = mock()
        val commitObj = io.titandata.models.Commit("uuid", mapOf("message" to "message", "author" to "unknown", "container" to "container", "runtime" to "runtime", "timestamp" to "timestamp"))
        val commitObj2 = io.titandata.models.Commit("uuid2", mapOf("message" to "", "author" to "unknown", "container" to "container", "runtime" to "runtime", "timestamp" to "timestamp"))
        val commitArray = arrayOf(commitObj,commitObj2)
        doReturn(commitArray).whenever(commitsApi).listCommits("container")
        val command = Log(commitsApi)

        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        command.log("container")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "commit uuid\nAuthor: unknown\nDate: timestamp\n\nmessage\n\ncommit uuid2\nAuthor: unknown\nDate: timestamp")
    }
}