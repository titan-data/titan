package io.titandata.titan.providers.local

import io.titandata.titan.providers.Container
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class UpgradeTest {
    private fun start(container: String) {}
    private fun stop(container: String) {}
    private fun exit(message: String, code: Int) {}
    private fun getContainersStatus(): List<Container> {
        return listOf<Container>()
    }
    private val command = Upgrade(::start,::stop,::exit,::getContainersStatus)

    @Test
    fun `can instantiate`(){
        Assert.assertThat(command, CoreMatchers.instanceOf(Upgrade::class.java))
    }
  
}