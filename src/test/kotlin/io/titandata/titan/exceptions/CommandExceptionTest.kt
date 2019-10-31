package io.titandata.titan.exceptions

import org.junit.Test
import kotlin.test.assertEquals
import java.io.IOException
import kotlin.test.assertFailsWith

class CommandExceptionTest {

    @Test
    fun `can handle exception`() {
        assertFailsWith<IOException> {
            throw CommandException("message", 1, "output")
        }
    }

    @Test
    fun `can get exception values`() {
        try {
            throw  CommandException("message", 1, "output")
        } catch (e: CommandException) {
            assertEquals("message", e.message)
            assertEquals(1, e.exitCode)
            assertEquals("output", e.output)
        }
    }

}