/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.utils

import org.junit.Test
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import io.titandata.titan.utils.HttpHandler.Companion.asString
import io.titandata.titan.utils.HttpHandler.Companion.asBytes
import io.titandata.titan.utils.HttpHandler.Companion.asJsonObject
import io.titandata.titan.utils.HttpHandler.Companion.asJsonArray
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.given
import java.io.IOException
import kotlin.test.assertFailsWith

class HttpHandlerTest {
    private val mockServer = MockWebServer()
    private val mockUrl = mockServer.url("/").toString()
    private val handler = HttpHandler()

    @Test
    fun `can get`() {
        val mockedResponse = MockResponse()
        mockedResponse.setResponseCode(200)
        mockedResponse.setBody("1.1.1.1")
        mockServer.enqueue(mockedResponse)
        val response = handler.get(mockUrl)
        assertThat(response, instanceOf(ResponseBody::class.java))
        assertEquals("1.1.1.1", response.string())
    }

    @Test
    fun `can post`() {
        val mockedResponse = MockResponse()
        mockedResponse.setResponseCode(200)
        mockedResponse.setBody("1.1.1.1")
        mockServer.enqueue(mockedResponse)
        val response = handler.post(mockUrl, mapOf("data" to "data"))
        assertThat(response, instanceOf(ResponseBody::class.java))
        assertEquals("1.1.1.1", response.string())
    }

    @Test
    fun `can handle exception`() {
        val mockedResponse = MockResponse()
        mockedResponse.setResponseCode(500)
        mockServer.enqueue(mockedResponse)
        assertFailsWith<IOException> {
            handler.get(mockUrl)
        }
    }

    @Test
    fun `can get asString`() {
        val mockedResponse = MockResponse()
        mockedResponse.setResponseCode(200)
        mockedResponse.setBody("1.1.1.1")
        mockServer.enqueue(mockedResponse)
        val response = handler.get(mockUrl).asString()
        assertTrue(response is String)
        assertEquals("1.1.1.1", response)
    }

    @Test
    fun `can get asBytes`() {
        val mockedResponse = MockResponse()
        mockedResponse.setResponseCode(200)
        mockedResponse.setBody("1.1.1.1")
        mockServer.enqueue(mockedResponse)
        val response = handler.get(mockUrl).asBytes()
        assertTrue(response is ByteArray)
        assertEquals(46, response[1])
    }

    @Test
    fun `can get asJsonObject`() {
        val mockedResponse = MockResponse()
        mockedResponse.setResponseCode(200)
        mockedResponse.setBody("{\"json\":\"data\"}")
        mockServer.enqueue(mockedResponse)
        val response = handler.get(mockUrl).asJsonObject()
        assertEquals("data", response.getString("json"))
    }

    @Test
    fun `can get asJsonArray`() {
        val mockedResponse = MockResponse()
        mockedResponse.setResponseCode(200)
        mockedResponse.setBody("[{\"json\":\"data\"}]")
        mockServer.enqueue(mockedResponse)
        val response = handler.get(mockUrl).asJsonArray()
        val firstItem = response.getJSONObject(0)
        assertEquals("data", firstItem.getString("json"))
    }
}