package io.titandata.titan.providers.local

import org.junit.Test
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat

class CheckInstallTest {
    private fun exit(message: String, code: Int) {}
    private val command = CheckInstall(::exit)

    @Test
    fun `can instantiate`(){
        assertThat(command, instanceOf(CheckInstall::class.java))
    }

}