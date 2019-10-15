/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.titan.providers.Container
import io.titandata.client.apis.RepositoriesApi
import java.text.DecimalFormat
import kotlin.math.log10

class Status (
    private val getContainersStatus: () -> List<Container>,
    private val repositoriesApi: RepositoriesApi = RepositoriesApi()
){
    private val n = System.lineSeparator()

    /**
     * https://stackoverflow.com/a/5599842
     */
    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("Bi", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun status(container: String) {
        val status = repositoriesApi.getRepositoryStatus(container)
        for(cont in getContainersStatus()) {
            if(container == cont.name) {
                System.out.printf("%15s %s${n}", "Status: ", cont.status)
            }
        }
        System.out.printf("%15s %s${n}", "Logical Size: ", readableFileSize(status.logicalSize))
        System.out.printf("%15s %s${n}", "Actual Size: ", readableFileSize(status.actualSize))
        System.out.printf("%15s %s${n}", "Last Commit: ", status.lastCommit)
        println()
        System.out.printf("%-30s  %-10s  %s${n}", "Volume", "Logical", "Actual")
        for (volume in status.volumeStatus) {
            System.out.printf("%-30s  %-10s  %s${n}", volume.properties["path"], readableFileSize(volume.logicalSize), readableFileSize(volume.actualSize))
        }
    }
}