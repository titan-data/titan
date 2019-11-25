package io.titandata.titan.providers.local

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class RemoveTest {
    private fun exit(message: String, code: Int) {}
    private val command = Remove(::exit)

    @Test
    fun `can instantiate`() {
        Assert.assertThat(command, CoreMatchers.instanceOf(Remove::class.java))
    }
}
