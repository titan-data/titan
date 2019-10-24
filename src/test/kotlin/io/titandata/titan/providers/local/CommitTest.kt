package io.titandata.titan.providers.local

import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.models.Repository
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class CommitTest {

    @Test
    fun `can instantiate`(){
        val command = Commit("user", "email")
        assertThat(command, instanceOf(Commit::class.java))
    }
    /*
    @Test
    fun `can commit with system user`(){
        val reposApi: RepositoriesApi = mock()
        val commitsApi: CommitsApi = mock()
        val repo = Repository("container", mapOf("container" to "container", "runtime" to "runtime"))
        doReturn(repo).whenever(reposApi).getRepository("container")
        val commitObj = io.titandata.titan.models.Commit("uuid", mapOf("message" to "message", "author" to "systemuser", "container" to "container", "runtime" to "runtime"))
        doReturn(commitObj).whenever(commitsApi).createCommit("container", commitObj)
        val command = Commit(reposApi,commitsApi,"systemuser", "uuid")

        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        command.commit("container", "message")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Commit uuid")
    }

    @Test
    fun `can commit without user`(){
        val reposApi: RepositoriesApi = mock()
        val commitsApi: CommitsApi = mock()
        val repo = Repository("container", mapOf("container" to "container", "runtime" to "runtime"))
        doReturn(repo).whenever(reposApi).getRepository("container")
        val commitObj = io.titandata.titan.models.Commit("uuid", mapOf("message" to "message", "author" to "unknown", "container" to "container", "runtime" to "runtime"))
        doReturn(commitObj).whenever(commitsApi).createCommit("container", commitObj)
        val command = Commit(reposApi,commitsApi,null, "uuid")

        val byteStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteStream))
        command.commit("container", "message")
        byteStream.flush()
        val expected = String(byteStream.toByteArray()).trim()
        assertEquals(expected, "Commit uuid")
    }

     */
}