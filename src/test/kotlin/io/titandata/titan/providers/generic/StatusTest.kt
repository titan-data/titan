package io.titandata.titan.providers.generic

import io.titandata.titan.providers.Container
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class StatusTest {
    private fun getContainersStatus(): List<Container> {
        return listOf<Container>()
    }
    private val command = Status(::getContainersStatus)

    @Test
    fun `can instantiate`(){
        Assert.assertThat(command, CoreMatchers.instanceOf(Status::class.java))
    }

}