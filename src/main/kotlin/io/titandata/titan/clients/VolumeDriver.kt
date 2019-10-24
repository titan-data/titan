/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.clients

import io.titandata.titan.utils.HttpHandler
import io.titandata.titan.utils.HttpHandler.Companion.asString
import org.json.JSONObject

class VolumeDriver(private val httpHandler: HttpHandler, private val volumeDriverUrl: String = "http://localhost:5000"){

    fun list(): JSONObject {
        val response = httpHandler.post("$volumeDriverUrl/VolumeDriver.List", emptyMap()).asString()
        return JSONObject(response)
    }

    fun mount(volumeName: String): JSONObject {
        val response = httpHandler.post("$volumeDriverUrl/VolumeDriver.Mount", mapOf("Name" to volumeName)).asString()
        return JSONObject(response)
    }

    fun unmount(volumeName: String): JSONObject {
        val response = httpHandler.post("$volumeDriverUrl/VolumeDriver.Unmount", mapOf("Name" to volumeName)).asString()
        return JSONObject(response)
    }
}