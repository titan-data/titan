package io.titandata.titan

import org.junit.Test
import io.titandata.titan.providers.Provider
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import io.titandata.titan.providers.ProviderFactory

class DependenciesTest {
    val provider = ProviderFactory().getFactory("mock")
    val dependencies = Dependencies(provider)

    @Test
    fun `can get provider`(){
        assertThat(dependencies.provider, instanceOf(Provider::class.java))
    }
}