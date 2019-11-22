package io.titandata.titan.providers.generic

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class UpgradeTest {
    private fun start(container: String) {}
    private fun stop(container: String) {}
    private fun exit(message: String, code: Int) {}
    private fun getContainersStatus(): List<RuntimeStatus> {
        return emptyList()
    }
    private val command = Upgrade(::start, ::stop, ::exit, ::getContainersStatus)

    @Test
    fun `can instantiate`(){
        Assert.assertThat(command, CoreMatchers.instanceOf(Upgrade::class.java))
    }
  
}