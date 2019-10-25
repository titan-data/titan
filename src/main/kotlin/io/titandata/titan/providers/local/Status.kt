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
                System.out.printf("%20s %s${n}", "Status: ", cont.status)
            }
        }
        System.out.printf("%20s %s${n}", "Uncompressed Size: ", readableFileSize(status.logicalSize))
        System.out.printf("%20s %s${n}", "Compressed Size: ", readableFileSize(status.actualSize))
        if (status.lastCommit != null) {
            System.out.printf("%20s %s${n}", "Last Commit: ", status.lastCommit)
        }
        if (status.sourceCommit != null) {
            System.out.printf("%20s %s${n}", "Source Commit: ", status.sourceCommit)
        }
        println()
        System.out.printf("%-30s  %-12s  %s${n}", "Volume", "Uncompressed", "Compressed")
        for (volume in status.volumeStatus) {
            System.out.printf("%-30s  %-12s  %s${n}", volume.properties["path"], readableFileSize(volume.logicalSize), readableFileSize(volume.actualSize))
        }
    }
}