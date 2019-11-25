/*
 * Copyright The Titan Project Contributors.
 */

package io.titandata.titan.exceptions

import java.io.InvalidObjectException

class InvalidArgumentException(message: String, val exitCode: Int, val output: String) : InvalidObjectException(message)
