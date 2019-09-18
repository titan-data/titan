package io.titandata.titan.providers.local

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class InstallTest {
    private val command = Install("0.3.0", "delphix")

    @Test
    fun `can instantiate`(){
        Assert.assertThat(command, CoreMatchers.instanceOf(Install::class.java))
    }
}