package io.titandata.titan.providers

class ProviderFactory {

    private val provider : Provider

    init {
        val type = System.getenv("TITAN_CONTEXT") ?: "local"
        provider = when(type) {
            "local" -> Local()
            "kubernetes" -> Kubernetes()
            else -> Mock()
        }
    }

    fun list() : List<Provider> {
        return listOf(provider)
    }

    fun byRepositoryName(repoName: String?) : Provider {
        if (repoName == null) {
            return default()
        } else {
            return provider
        }
    }

    fun byRepository(repoName: String) : Provider {
        return provider
    }

    fun default(checkInstall : Boolean = true) : Provider {
        if (checkInstall) {
            provider.checkInstall()
        }
        return provider
    }
}