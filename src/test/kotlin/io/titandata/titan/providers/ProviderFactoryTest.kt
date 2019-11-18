package io.titandata.titan.providers

import org.junit.Test
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import kotlin.test.assertEquals

class ProviderFactoryTest {
    val providerFactory = ProviderFactory()

    @Test
    fun `can get mock provider`(){
        val provider = providerFactory.getFactory("mock")
        //TODO test without reflection or add reflection to dev dependencies only
        //assertThat(provider, instanceOf(Provider::class.java))
        //assertEquals("io.titandata.titan.providers.Mock", provider::class.qualifiedName)
    }

    @Test
    fun `can get local provider`(){
        val provider = providerFactory.getFactory("local")
        //TODO test without reflection or add reflection to dev dependencies only
        //assertThat(provider, instanceOf(Provider::class.java))
        //assertEquals("io.titandata.titan.providers.Local", provider::class.qualifiedName)
    }
}