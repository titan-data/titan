package io.titandata.titan.utils

import io.titandata.client.apis.OperationsApi
import io.titandata.client.infrastructure.ClientException
import io.titandata.models.Operation
import io.titandata.models.ProgressEntry

class OperationMonitor(val repo: String,
                       val operation : Operation,
                       private val operationsApi: OperationsApi = OperationsApi()) {

    fun isTerminal(state: ProgressEntry.Type) : Boolean {
        return (state == ProgressEntry.Type.FAILED || state == ProgressEntry.Type.ABORT ||
                state == ProgressEntry.Type.COMPLETE)
    }

    fun monitor() : Boolean {
        /*
         * The behavior of the API is such that we can keep reading progress entries, and we must "drain" all such
         * entries for the operation to be considered complete. Once we read the last entry, the operation will
         * be complete and we will no longer be able to query it for status.
         */
        var padLen = 0
        var aborted = false
        var state = ProgressEntry.Type.START
        while (!isTerminal(state)) {
            try {
                val entries = operationsApi.getProgress(repo, operation.id)

                if (entries.size > 0) {
                    state = entries.last().type
                }

                for (e in entries) {
                    if (e.type != ProgressEntry.Type.PROGRESS) {
                        if (!e.message.isNullOrEmpty()) {
                            println(e.message)
                        }
                        padLen = 0
                    } else {
                        val subMessage = e.message as String
                        if (subMessage.length > padLen) {
                            padLen = subMessage.length
                        }
                        System.out.printf("\r%s", subMessage.padEnd((padLen - subMessage.length) + 1, ' '))
                    }
                }

                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                /*
                 * We swallow interrupts and instead translate them to an abort call. The operation may have already
                 * completed, so we swallow any exception there. If the users sends multiple interrupts (e.g.
                 * mashing Ctrl-C), then we let them exit out in case there's something seriously broken on the
                 * server.
                 */
                if (aborted) {
                    throw e
                } else {
                    try {
                        operationsApi.deleteOperation(repo, operation.id)
                    } catch (e: ClientException) {
                        if (e.code != "NoSuchObjectException") {
                            throw e
                        }
                    }
                    aborted = true
                }
            }
        }

        val operationText = if (operation.type == Operation.Type.PULL) { "Pull" } else { "Push" }
        when (state) {
            ProgressEntry.Type.COMPLETE -> println("$operationText completed successfully")
            ProgressEntry.Type.FAILED -> println("$operationText failed")
            ProgressEntry.Type.ABORT -> println("$operationText aborted")
        }

        return state == ProgressEntry.Type.COMPLETE
    }
}