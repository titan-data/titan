/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers

import io.titandata.titan.clients.Docker.Companion.toList

data class Image(val image: String, val tag: String?, val digest: String)
data class Port(val protocol: String, val port: String)
data class Volume(val name: String, val path: String)

data class Metadata(
    val version: Version,
    var user: String?,
    var email: String?,
    var message: String?,
    var source: String?,
    var tags: Map<String, String>?,
    var timestamp: String?,
    val image: Image,
    val environment: List<String>,
    val ports: List<Port>,
    val volumes: List<Volume>
) {
    enum class Version(val value: String) {
        V2("v2"),
        V1("v1")
    }
    companion object{
        fun Metadata.toMap(): Map<String, Any> {
            val returnMap = mutableMapOf<String, Any>()
            if (!this.user.isNullOrEmpty()) {
                returnMap["user"] = this.user!!
            }
            if (!this.email.isNullOrEmpty()) {
                returnMap["email"] = this.email!!
            }
            if (!this.message.isNullOrEmpty()) {
                returnMap["message"] = this.message!!
            }
            if (!this.source.isNullOrEmpty()) {
                returnMap["source"] = this.source!!
            }
            if (!this.tags.isNullOrEmpty()) {
                returnMap["tags"] = this.tags!!
            }
            if (!this.timestamp.isNullOrEmpty()) {
                returnMap["timestamp"] = this.timestamp!!
            }
            if (this.version == Version.V2) {
                returnMap["v2"] = mapOf(
                    "image" to this.image,
                    "environment" to this.environment,
                    "ports" to this.ports,
                    "volumes" to this.volumes
                )
            }
            if (this.version == Version.V1) {
                returnMap["container"] = this.image.digest
                returnMap["image"] = this.image.image
                returnMap["tag"] = this.image.tag!!
                returnMap["digest"] = this.image.digest
            }
            return returnMap
        }
        fun load(metaMap: Map<String, Any>): Metadata {
            return when {
                metaMap.containsKey("v2") -> mapV2(metaMap)
                else -> mapV1(metaMap)
            }
        }
        private fun mapV2(metaMap: Map<String, Any>): Metadata {
            val user = when {
                metaMap.containsKey("user") -> metaMap["user"] as String
                else -> null
            }
            val email = when {
                metaMap.containsKey("email") -> metaMap["email"] as String
                else -> null
            }
            val message = when {
                metaMap.containsKey("message") -> metaMap["message"] as String
                else -> null
            }
            val source = when {
                metaMap.containsKey("source") -> metaMap["source"] as String
                else -> null
            }
            @Suppress("UNCHECKED_CAST")
            val tags = when {
                metaMap.containsKey("tags") -> metaMap["tags"] as Map<String, String>
                else -> null
            }
            val timestamp = when {
                metaMap.containsKey("timestamp") -> metaMap["timestamp"] as String
                else -> null
            }

            @Suppress("UNCHECKED_CAST")
            val meta = metaMap["v2"] as Map<String, Any>

            @Suppress("UNCHECKED_CAST")
            val imageMap = meta["image"] as Map<String, String>
            val image = Image(imageMap["image"]!!, imageMap["tag"]!!, imageMap["digest"]!!)

            @Suppress("UNCHECKED_CAST")
            val environment =  meta["environment"] as List<String>

            @Suppress("UNCHECKED_CAST")
            val mapPorts = meta["ports"] as List<Map<String, String>>
            val ports = mutableListOf<Port>()
            for (mapPort in mapPorts) {
                ports.add(
                        Port(mapPort["protocol"]!!, mapPort["port"]!!)

                )
            }
            @Suppress("UNCHECKED_CAST")
            val mapVols = meta["volumes"] as List<Map<String, String>>
            val volumes = mutableListOf<Volume>()
            for (mapVol in mapVols) {
                volumes.add(Volume(mapVol["name"]!!, mapVol["path"]!!))
            }
            return Metadata(Version.V2, user, email, message, source, tags, timestamp, image, environment, ports, volumes)
        }

        private fun mapV1(metaMap: Map<String, Any>): Metadata {
            val user = when {
                metaMap.containsKey("user") -> metaMap["user"] as String
                else -> null
            }
            val email = when {
                metaMap.containsKey("email") -> metaMap["email"] as String
                else -> null
            }
            val message = when {
                metaMap.containsKey("message") -> metaMap["message"] as String
                else -> null
            }
            val source = when {
                metaMap.containsKey("source") -> metaMap["source"] as String
                else -> null
            }
            val tags = when {
                metaMap.containsKey("tags") -> metaMap["tags"] as Map<String, String>
                else -> null
            }
            val timestamp = when {
                metaMap.containsKey("timestamp") -> metaMap["timestamp"] as String
                else -> null
            }
            val digest = metaMap["container"] as String
            val imageName = when {
                metaMap.containsKey("image") -> metaMap["image"] as String
                else -> digest.split("@")[0]
            }
            val imageTag = when {
                metaMap.containsKey("tag") -> metaMap["tag"] as String
                else -> null
            }
            val image =  Image(imageName, imageTag, digest)
            val runtimeString = metaMap["runtime"] as String
            val runtime = runtimeString.removePrefix("[").removeSuffix("]").toList(", ")

            val envs = mutableListOf<String>()
            val ports = mutableListOf<Port>()
            val volumes = mutableListOf<Volume>()

            for ((index, item) in runtime.withIndex()) {
                if (item == "--env" || item == "-e") {
                    envs.add(runtime[index + 1])
                }
                if (item == "-p") {
                    val port = when {
                        runtime[index + 1].contains(":") -> runtime[index + 1].split(":")[1]
                        else -> runtime[index + 1]
                    }
                    val addPort = Port(
                            "tcp",
                            port
                    )
                    ports.add(addPort)
                }
                if (item == "--mount") {
                    val vols = runtime[index + 1].split(",")
                    var name = ""
                    var path = ""
                    for (vol in vols){
                        val splitVol = vol.split("=")
                        when (splitVol[0]) {
                            "src" -> name = splitVol[1].split("/")[1]
                            "dst" -> path = splitVol[1]
                            else -> "no match"
                        }
                    }
                    volumes.add(Volume(name, path))
                }
            }
            return Metadata(Version.V1, user, email, message, source, tags, timestamp, image, envs, ports, volumes)
        }
    }
}