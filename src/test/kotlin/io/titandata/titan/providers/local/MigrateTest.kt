package io.titandata.titan.providers.local

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class MigrateTest {
    private fun exit(message: String, code: Int) {}
    private fun commit(container: String, message: String) {}
    private val command = Migrate(::exit,::commit)

    @Test
    fun `can instantiate`(){
        Assert.assertThat(command, CoreMatchers.instanceOf(Migrate::class.java))
    }
}
