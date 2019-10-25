package io.titandata.titan.providers.local

import com.nhaarman.mockitokotlin2.any
import io.titandata.client.apis.CommitsApi
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers.anyObject
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class LogTest {

    private val n = System.lineSeparator()

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
        doReturn(commitArray).whenever(commitsApi).listCommits("container", listOf())
        val command = Log(commitsApi)

        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        command.log("container", listOf())
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "commit uuid${n}Author: unknown${n}Date: timestamp${n}${n}message${n}${n}commit uuid2${n}Author: unknown${n}Date: timestamp")
    }
}