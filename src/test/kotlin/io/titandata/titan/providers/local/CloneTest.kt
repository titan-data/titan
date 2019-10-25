package io.titandata.titan.providers.local

import org.junit.Test
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat

class CloneTest {
    private fun remoteAdd(container:String, uri: String, remoteName: String?) {}
    private fun pull(container: String, commit: String?, remoteName: String?, tags: List<String>, metadataOnly: Boolean) {}
    private fun checkout(container: String, hash: String) {}
    private fun run(arguments: List<String>, createRepo:Boolean) {}
    private fun remove(container: String, force: Boolean) {}
    private val command = Clone(::remoteAdd, ::pull, ::checkout, ::run, ::remove)

    @Test
    fun `can instantiate`(){
        assertThat(command, instanceOf(Clone::class.java))
    }

}