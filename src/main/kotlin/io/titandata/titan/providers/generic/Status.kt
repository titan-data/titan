/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.generic

import io.titandata.titan.providers.Container
import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.apis.VolumesApi
import java.text.DecimalFormat
import kotlin.math.log10

class Status (
    private val getContainersStatus: () -> List<Container>,
    private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
    private val volumesApi: VolumesApi = VolumesApi()
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
        if (status.lastCommit != null) {
            System.out.printf("%20s %s${n}", "Last Commit: ", status.lastCommit)
        }
        if (status.sourceCommit != null) {
            System.out.printf("%20s %s${n}", "Source Commit: ", status.sourceCommit)
        }
        println()

        val volumes = volumesApi.listVolumes(container)
        System.out.printf("%-30s  %-12s  %s${n}", "Volume", "Uncompressed", "Compressed")
        for (volume in volumes) {
            val volumeStatus = volumesApi.getVolumeStatus(container, volume.name)
            System.out.printf("%-30s  %-12s  %s${n}", volume.properties["path"],
                    readableFileSize(volumeStatus.logicalSize), readableFileSize(volumeStatus.actualSize))
        }
    }
}