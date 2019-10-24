package io.titandata.titan.providers.local

import io.titandata.client.apis.CommitsApi
import io.titandata.titan.clients.Docker
import io.titandata.titan.utils.CommandExecutor
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class CheckoutTest {
    private val executor = mock<CommandExecutor> {
        on {exec(listOf("docker","stop","container"))} doReturn "container"
        on {exec(listOf("docker","start","container"))} doReturn "container"
    }
    private val docker = Docker(executor)
    private val n = System.lineSeparator()

    @Test
    fun `can instantiate`(){
        val command = Checkout()
        assertThat(command, instanceOf(Checkout::class.java))
    }

    @Test
    fun `can checkout`(){
        val commitsApi: CommitsApi = mock()
        doNothing().whenever(commitsApi).checkoutCommit("container", "hash")
        val command = Checkout(executor, docker, commitsApi)
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        command.checkout("container", "hash")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Stopping container container${n}Checkout hash${n}Starting container container${n}hash checked out")
    }
}