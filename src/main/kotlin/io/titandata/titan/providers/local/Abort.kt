/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.local

import io.titandata.client.apis.OperationsApi
import io.titandata.client.infrastructure.ClientException
import io.titandata.models.Operation

class Abort (
    private val exit: (message: String, code: Int) -> Unit,
    private val operationsApi: OperationsApi = OperationsApi()
) {
    fun abort(container: String) {
        try {
            val operations = operationsApi.listOperations(container)
            var abortCount = 0
            for (operation in operations) {
                if (operation.state == Operation.State.RUNNING) {
                    println("aborting operation ${operation.id}")
                    operationsApi.deleteOperation(container, operation.id)
                    abortCount++
                }
            }
            if (abortCount == 0) {
                exit("no operation in progress", 0)
            }

        } catch (e: ClientException) {
            exit(e.message!!,1)
        }

    }
}