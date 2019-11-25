package io.titandata.titan

import io.titandata.titan.Version.Companion.compare
import kotlin.test.assertEquals
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Test

class VersionTest {
    private val version = Version(1, 0, 0, "rc-1")

    @Test
    fun `can load version`() {
        assertThat(version, instanceOf(Version::class.java))
    }

    @Test
    fun `can get major value`() {
        assertEquals(1, version.major)
    }

    @Test
    fun `can get minor value`() {
        assertEquals(0, version.minor)
    }

    @Test
    fun `can get micro value`() {
        assertEquals(0, version.micro)
    }

    @Test
    fun `can get pre-release value`() {
        assertEquals("rc-1", version.preRelease)
    }

    @Test
    fun `can load from string`() {
        val version = Version.fromString("2.1.3")
        assertThat(version, instanceOf(Version::class.java))
        assertEquals(2, version.major)
        assertEquals(1, version.minor)
        assertEquals(3, version.micro)
    }

    @Test
    fun `can load pre-release from string`() {
        val version = Version.fromString("2.1.3-rc1")
        assertThat(version, instanceOf(Version::class.java))
        assertEquals(2, version.major)
        assertEquals(1, version.minor)
        assertEquals(3, version.micro)
        assertEquals("rc1", version.preRelease)
    }

    @Test
    fun `versions are the same`() {
        val current = Version(1, 0, 0)
        val latest = Version(1, 0, 0)
        assertEquals(0, current.compare(latest))
    }

    @Test
    fun `current is behind latest major`() {
        val current = Version(0, 0, 1)
        val latest = Version(1, 0, 0)
        assertEquals(-1, current.compare(latest))
    }

    @Test
    fun `current is ahead of latest major`() {
        val current = Version(1, 0, 0)
        val latest = Version(0, 0, 0)
        assertEquals(1, current.compare(latest))
    }

    @Test
    fun `current is behind latest minor`() {
        val current = Version(0, 0, 1)
        val latest = Version(0, 1, 0)
        assertEquals(-1, current.compare(latest))
    }

    @Test
    fun `current is ahead of latest minor`() {
        val current = Version(0, 1, 0)
        val latest = Version(0, 0, 0)
        assertEquals(1, current.compare(latest))
    }

    @Test
    fun `current is behind latest micro`() {
        val current = Version(0, 0, 0)
        val latest = Version(0, 0, 1)
        assertEquals(-1, current.compare(latest))
    }

    @Test
    fun `current is ahead of latest micro`() {
        val current = Version(0, 0, 1)
        val latest = Version(0, 0, 0)
        assertEquals(1, current.compare(latest))
    }
}
