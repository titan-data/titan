package io.titandata.titan.providers

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class MetadataTest {
    private val oldMetadata: Map<String, Any> = mapOf(
        "container" to "mongo@sha256:7d9fe4ae781849afc70a29a0e1e4a37a2236c93a75786c576f34258e5983efbe",
        "image" to "mongo",
        "tag" to "latest",
        "digest" to "mongo@sha256:7d9fe4ae781849afc70a29a0e1e4a37a2236c93a75786c576f34258e5983efbe",
        "runtime" to "[-d, --label, io.titandata.titan, --mount, type=volume,src=mongo/v0,dst=/data/configdb,volume-driver=titan, --mount, type=volume,src=mongo/v1,dst=/data/db,volume-driver=titan, --name, mongo, -p, 27017:27017, --env, DEBUG=TRUE]"
    )

    private val v2Metadata: Map<String, Any> = mapOf(
        "v2" to mapOf(
            "image" to mapOf(
                "image" to "mongo",
                "tag" to "latest",
                "digest" to "mongo@sha256:7d9fe4ae781849afc70a29a0e1e4a37a2236c93a75786c576f34258e5983efbe"
            ),
            "environment" to listOf<String>(
                "DEBUG=TRUE"
            ),
            "ports" to listOf<Map<String, String>>(
                mapOf("protocol" to "tcp", "port" to "27017")
            ),
            "volumes" to listOf<Map<String, String>>(
                mapOf("name" to "v0", "path" to "/data/configdb"),
                mapOf("name" to "v1", "path" to "/data/db")
            )
        )
    )

    private val v1Loaded = Metadata.load(oldMetadata)
    private val v2Loaded = Metadata.load(v2Metadata)

    @Test
    fun `v1 can get tags`() {
        assertNull(v1Loaded.tags)
    }

    @Test
    fun `v1 can get timestamp`() {
        assertNull(v1Loaded.timestamp)
    }

    @Test
    fun `v1 can get image`() {
        assertEquals("mongo", v1Loaded.image.image)
        assertEquals("latest", v1Loaded.image.tag)
        assertEquals("mongo@sha256:7d9fe4ae781849afc70a29a0e1e4a37a2236c93a75786c576f34258e5983efbe", v1Loaded.image.digest)
    }

    @Test
    fun `v1 can get envs`() {
        assertEquals("DEBUG=TRUE", v1Loaded.environment[0])
    }

    @Test
    fun `v1 can get ports`() {
        assertEquals("tcp", v1Loaded.ports[0].protocol)
        assertEquals("27017", v1Loaded.ports[0].port)
    }

    @Test
    fun `v1 can get vols`() {
        assertEquals("v0", v1Loaded.volumes[0].name)
        assertEquals("/data/configdb", v1Loaded.volumes[0].path)
        assertEquals("v1", v1Loaded.volumes[1].name)
        assertEquals("/data/db", v1Loaded.volumes[1].path)
    }

    @Test
    fun `v2 can get tags`() {
        assertNull(v2Loaded.tags)
    }

    @Test
    fun `v2 can get timestamp`() {
        assertNull(v2Loaded.timestamp)
    }

    @Test
    fun `v2 can get image`() {
        assertEquals("mongo", v2Loaded.image.image)
        assertEquals("latest", v2Loaded.image.tag)
        assertEquals("mongo@sha256:7d9fe4ae781849afc70a29a0e1e4a37a2236c93a75786c576f34258e5983efbe", v2Loaded.image.digest)
    }

    @Test
    fun `v2 can get envs`() {
        assertEquals("DEBUG=TRUE", v2Loaded.environment[0])
    }

    @Test
    fun `v2 can get ports`() {
        assertEquals("tcp", v2Loaded.ports[0].protocol)
        assertEquals("27017", v2Loaded.ports[0].port)
    }

    @Test
    fun `v2 can get vols`() {
        assertEquals("v0", v2Loaded.volumes[0].name)
        assertEquals("/data/configdb", v2Loaded.volumes[0].path)
        assertEquals("v1", v2Loaded.volumes[1].name)
        assertEquals("/data/db", v2Loaded.volumes[1].path)
    }
}
