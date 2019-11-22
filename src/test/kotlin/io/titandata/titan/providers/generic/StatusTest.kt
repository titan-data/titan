package io.titandata.titan.providers.generic

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class StatusTest {
    private fun getContainersStatus(): List<RuntimeStatus> {
        return emptyList()
    }
    private val command = Status(::getContainersStatus)

    @Test
    fun `can instantiate`(){
        Assert.assertThat(command, CoreMatchers.instanceOf(Status::class.java))
    }

}