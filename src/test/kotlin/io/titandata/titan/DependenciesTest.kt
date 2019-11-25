package io.titandata.titan

import io.titandata.titan.providers.Provider
import io.titandata.titan.providers.ProviderFactory
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Test

class DependenciesTest {
    val provider = ProviderFactory().getFactory("mock")
    val dependencies = Dependencies(provider)

    @Test
    fun `can get provider`() {
        assertThat(dependencies.provider, instanceOf(Provider::class.java))
    }
}
