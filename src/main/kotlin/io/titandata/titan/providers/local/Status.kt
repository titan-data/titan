/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.titan.providers.Container

class Status (
    private val getContainersStatus: () -> List<Container>
){
    fun status(container: String) {
        for(cont in getContainersStatus()) {
            if(container == cont.name) {
                println("Status: ${cont.status}")
            }
        }
        println("*** SAMPLE STATUS ***")
        println("Size: 5.4GiB")
        println("Current Head: d3f4c1")
        println("Volumes:")
        println("   1.23GiB /var/lib/postgres/data")
        println("Operations: None")
        println("*** SAMPLE STATUS ***")
    }
}