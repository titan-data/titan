package io.titandata.titan.providers

import io.titandata.titan.providers.Mock as MockProvider
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import org.junit.Test

class MockTest {

    private val mockProvider = MockProvider()

    @Test
    fun `can pull`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.pull("container", "commit", null, listOf(), false)
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Pulling from remote")
    }

    @Test
    fun `can push`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.push("container", "commit", null, listOf(), false)
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Pushing to remote")
    }

    @Test
    fun `can commit`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.commit("container", "message", listOf())
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Committing new state")
    }

    @Test
    fun `can install`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.install(emptyMap(), false)
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Installing infrastructure")
    }

    @Test
    fun `can abort`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.abort("container")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Aborting current operation")
    }

    @Test
    fun `can status`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.status("container")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Display current status")
    }

    @Test
    fun `can remoteAdd`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.remoteAdd("container", "uri", "remoteName", emptyMap())
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Add remote")
    }

    @Test
    fun `can remoteLog`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.remoteLog("container", null, listOf())
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Display remote log")
    }

    @Test
    fun `can migrate`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.migrate("container", "name")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Migrating container to name controlled environment")
    }

    @Test
    fun `can run`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.run("Container", "Repo", emptyList(), emptyList(), true)
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Running controlled image")
    }

    @Test
    fun `can uninstall`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.uninstall(true, true)
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Tearing down containers")
    }

    @Test
    fun `can upgrade`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.upgrade(true, "0.0.1", false, "")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Upgrading to 0.0.1")
    }

    @Test
    fun `can checkout`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.checkout("container", "hash", listOf())
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Checking out data set hash")
    }

    @Test
    fun `can list`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.list("mock")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "List containers")
    }

    @Test
    fun `can log`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.log("container", listOf())
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Log for container")
    }

    @Test
    fun `can stop`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.stop("container")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Stopping container")
    }

    @Test
    fun `can start`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.start("container")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Starting container")
    }

    @Test
    fun `can remove`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.remove("container", true)
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Removing container")
    }

    @Test
    fun `can cp`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.cp("container", "driver", "source", "path")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "copying data into container with driver from source")
    }

    @Test
    fun `can clone`() {
        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        mockProvider.clone("http://user:pass@path", "container", null, emptyMap(), emptyList(), false, emptyList())
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "cloning container from http://user:pass@path")
    }
}
