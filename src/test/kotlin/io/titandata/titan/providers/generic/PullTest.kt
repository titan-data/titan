package io.titandata.titan.providers.generic

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class PullTest {
    //TODO Update test for titan-server 0.3.1 release with URI and Multiple Remotes
    private fun exit(message: String, code: Int) {}
    private val command = Pull(::exit)

    @Test
    fun `can instantiate`(){
        Assert.assertThat(command, CoreMatchers.instanceOf(Pull::class.java))
    }
}