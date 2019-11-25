package io.titandata.titan.providers.local

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class CpTest {
    private fun exit(message: String, code: Int) {}
    private fun start(container: String) {}
    private fun stop(container: String) {}
    private val command = Cp(::exit, ::start, ::stop)

    @Test
    fun `can instantiate`() {
        Assert.assertThat(command, CoreMatchers.instanceOf(Cp::class.java))
    }
}
