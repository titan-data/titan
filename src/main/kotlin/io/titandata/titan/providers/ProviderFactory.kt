package io.titandata.titan.providers

class ProviderFactory {
    fun getFactory(name: String): Provider {
        return when(name) {
            "local" -> Local()
            "kubernetes" -> Kubernetes()
            else -> Mock()
        }
    }
}