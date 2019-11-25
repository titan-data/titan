/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan

data class Version(
    val major: Int = 0,
    val minor: Int = 0,
    val micro: Int = 0,
    val preRelease: String? = null
) {
    companion object {
        @JvmStatic
        fun fromString(version: String): Version {
            val preRelease = if (version.contains("-")) {
                version.split("-")[1]
            } else {
                null
            }
            val split = if (version.contains("-")) {
                version.split("-")[0].split(".")
            } else {
                version.split(".")
            }
            return Version(
                    split[0].toInt(),
                    split[1].toInt(),
                    split[2].toInt(),
                    preRelease
            )
        }

        fun Version.compare(to: Version): Int {
            if (this.major > to.major) return 1
            if (this.major < to.major) return -1
            if (this.minor > to.minor) return 1
            if (this.minor < to.minor) return -1
            if (this.micro > to.micro) return 1
            if (this.micro < to.micro) return -1
            // TODO compare preRelease tag
            return 0
        }
    }
}
