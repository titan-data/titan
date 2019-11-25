package io.titandata.titan.providers.local

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.titandata.titan.clients.Docker
import io.titandata.titan.utils.CommandExecutor
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Test

class StartTest {
    private val executor = mock<CommandExecutor> {
        on { exec(listOf("docker", "start", "container")) } doReturn ""
    }
    private val docker = Docker(executor)
    private val command = Start(executor, docker)

    @Test
    fun `can instantiate`() {
        assertThat(command, instanceOf(Start::class.java))
    }

    @Test
    fun `can start`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        command.start("container")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "container started")
    }
}
