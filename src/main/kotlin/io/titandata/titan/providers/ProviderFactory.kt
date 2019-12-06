package io.titandata.titan.providers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.GsonBuilder
import java.io.File

data class TitanProvider(
    val host: String = "localhost",
    val port: Int = 5001,
    val type: String = "docker",
    val default: Boolean = false
)

data class TitanConfig(
    val contexts: Map<String, TitanProvider> = emptyMap()
)

/**
 * The provider factor is responsible for managing multiple providers (contexts to the user). We keep track of
 * providers in the ~/.titan/config file, which is a YAML file that contains a list of contexts and their
 * configuration. Each provider corresponds to an instance of 'titan-server' running somewhere (currently only
 * the user's laptop). The config file keeps track of:
 *
 *      - The context name
 *      - The context type (kubernetes or local)
 *      - The host (always localhost)
 *      - The port to connect to (defaults to 5001)
 *      - Default indicator
 *
 * Additional configuration, such as the provider type and provider-specific configuration, is stored within
 * the titan-server instance and accessible through the getContext() client method. When a context is created, it
 * can be given a type ("docker" or "kubernetes") as well as context-specific configuration.
 *
 * Each repository is associated with a particular context, and can be referred to as "context/repo", or just
 * "repo" for convenience (if there is only one known context, or no conflicts exists).
 */
class ProviderFactory {

    private val configDir: String
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val config: TitanConfig
    private val providers: Map<String, Provider>

    init {
        val home = System.getProperty("user.home") ?: error("unable to determine home directory")
        configDir = "$home/.titan"
        config = readConfig()
        val result = mutableMapOf<String, Provider>()
        for (entry in config.contexts.entries) {
            result[entry.key] = when {
                entry.value.type == "docker" -> Local(entry.key, entry.value.host, entry.value.port)
                entry.value.type == "kubernetes" -> Kubernetes(entry.key, entry.value.host, entry.value.port)
                else -> error("unknown context type '${entry.value.type}")
            }
        }
        providers = result.toMap()
    }

    private fun readConfig(): TitanConfig {
        try {
            val file = File("$configDir/config")
            if (file.exists()) {
                val mapper = ObjectMapper(YAMLFactory())
                mapper.registerModule(KotlinModule())
                return mapper.readValue(File("$configDir/config"), TitanConfig::class.java)
            } else {
                return TitanConfig()
            }
        } catch (t: Throwable) {
            return TitanConfig()
        }
    }

    private fun writeConfig(config: TitanConfig) {
        val dir = File(configDir)
        if (!dir.exists()) {
            dir.mkdir()
        }
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.writeValue(File("$configDir/config"), config)
    }

    fun addProvider(name: String, type: String, port: Int) {
        val contexts = config.contexts.toMutableMap()
        contexts[name] = TitanProvider(port = port, type = type, default = contexts.isEmpty())
        writeConfig(config.copy(contexts = contexts))
    }

    fun removeProvider(name: String) {
        val contexts = config.contexts.toMutableMap()
        val current = contexts[name]
        contexts.remove(name)
        // If we delete the default provider, just pick one at random to be default
        if (current?.default == true) {
            contexts.keys.firstOrNull()?.let {
                contexts[it] = contexts[it]!!.copy(default = true)
            }
        }
        writeConfig(config.copy(contexts = contexts))
    }

    fun list(): Map<String, Provider> {
        return providers
    }

    fun byName(name: String): Provider {
        return providers.get(name) ?: error("no such context '$name'")
    }

    fun create(contextName: String, providerType: String, port: Int): Provider {
        return when (providerType) {
            "docker" -> Local(contextName, providerType, port)
            "kubernetes" -> Kubernetes(contextName, providerType, port)
            else -> error("unknown context type '$providerType'")
        }
    }

    fun byRepositoryName(repoName: String?): Pair<Provider, String?> {
        if (repoName == null || !repoName.contains("/")) {
            return default() to repoName
        } else {
            val providerName = repoName.substringBefore("/")
            val repo = repoName.substringAfter("/")
            val provider = providers.get(providerName) ?: error("unknown context '$providerName'")
            return provider to repo
        }
    }

    fun byRepository(repoName: String): Pair<Provider, String> {
        if (repoName.contains("/")) {
            val res = byRepositoryName(repoName)
            return res.first to res.second!!
        } else if (providers.isEmpty()) {
            error("No context is configured, run 'titan install' or 'titan context install' to configure titan")
        } else if (providers.size == 1) {
            return providers.values.first() to repoName
        } else {
            val matching = mutableListOf<Provider>()
            for (provider in providers.values) {
                if (provider.repositoryExists(repoName)) {
                    matching.add(provider)
                }
            }
            if (matching.size == 0) {
                error("no such repository '$repoName'")
            } else if (matching.size > 1) {
                error("more than one repository with matching")
            } else {
                return matching.first() to repoName
            }
        }
    }

    fun defaultName(): String {
        return if (providers.isEmpty()) {
            error("No context is configured, run 'titan install' or 'titan context install' to configure titan")
        } else if (providers.size == 1) {
            providers.entries.first().key
        } else {
            config.contexts.filter { it.value.default }.keys.firstOrNull()
                    ?: error("More than one context specified, but no default set")
        }
    }

    fun setDefault(name: String) {
        val contexts = config.contexts.toMutableMap()
        byName(name) // check it exists
        val oldDefault = defaultName()

        contexts[oldDefault] = contexts[oldDefault]!!.copy(default = false)
        contexts[name] = contexts[name]!!.copy(default = true)

        writeConfig(config.copy(contexts = contexts))
    }

    fun default(checkInstall: Boolean = true): Provider {
        val defaultName = defaultName()
        return providers[defaultName] ?: error("No such provider '$defaultName'")
    }
}
