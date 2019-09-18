/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.clients

import io.titandata.titan.utils.HttpHandler
import org.junit.Test
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import kotlin.test.assertEquals

class VolumeDriverTest {
    private val mockServer = MockWebServer()
    private val listResponse = MockResponse()
            .setBody("{\"Volumes\":[{\"Mountpoint\":\"/var/lib/titan/mnt/jerry/v0\",\"Name\":\"jerry/v0\"}],\"Err\":\"\"}")
            .setResponseCode(200)
    private val mountResponse = MockResponse()
            .setBody("{\"Err\":\"\",\"Mountpoint\":\"/var/lib/titan/mnt/elaine/v1\"}")
            .setResponseCode(200)
    private val unmountResponse = MockResponse()
            .setBody("{\"Err\":\"\"}")
            .setResponseCode(200)
    private val volumeDriver = VolumeDriver(HttpHandler(), mockServer.url("/").toString())

    @Test
    fun `can get volume driver list`(){
        mockServer.enqueue(listResponse)
        val list = volumeDriver.list()
        val array = list.getJSONArray("Volumes")[0] as JSONObject
        assertEquals("/var/lib/titan/mnt/jerry/v0", array.getString("Mountpoint"))
    }

    @Test
    fun `can mount new volume`(){
        mockServer.enqueue(mountResponse)
        val response = volumeDriver.mount("volumeName")
        assertEquals("/var/lib/titan/mnt/elaine/v1", response.getString("Mountpoint"))
    }

    @Test
    fun `can unmount volume`(){
        mockServer.enqueue(unmountResponse)
        val response = volumeDriver.unmount("volumeName")
        assertEquals("", response.getString("Err"))
    }

}