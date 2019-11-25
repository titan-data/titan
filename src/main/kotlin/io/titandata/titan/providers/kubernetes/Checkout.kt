/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.kubernetes

import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.apis.VolumesApi
import io.titandata.titan.clients.Kubernetes

class Checkout(
    private val kubernetes: Kubernetes = Kubernetes(),
    private val commitsApi: CommitsApi = CommitsApi(),
    private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
    private val volumesApi: VolumesApi = VolumesApi()
) {
    fun checkout(repoName: String, guid: String?, tags: List<String>) {
        val sourceCommit = if (guid == null) {
            if (tags.isNotEmpty()) {
                val commits = commitsApi.listCommits(repoName, tags)
                if (commits.size == 0) {
                    throw IllegalStateException("no matching commits found")
                }
                commits.first().id
            } else {
                val status = repositoriesApi.getRepositoryStatus(repoName)
                if (status.sourceCommit == null) {
                    throw IllegalStateException("no commits present, run 'titan commit' first")
                }
                status.sourceCommit!!
            }
        } else {
            if (tags.isNotEmpty()) {
                throw IllegalArgumentException("tags and commit cannot both be specified")
            }
            guid
        }

        val repo = repositoriesApi.getRepository(repoName)

        val status = commitsApi.getCommitStatus(repoName, sourceCommit)
        if (!status.ready) {
            println("Waiting for commit to be ready")
            while (true) {
                val commitStatus = commitsApi.getCommitStatus(repoName, sourceCommit)
                if (commitStatus.ready) {
                    break
                }
                Thread.sleep(1000L)
            }
        }

        println("Checkout $sourceCommit")
        commitsApi.checkoutCommit(repoName, sourceCommit)

        println("Stopping port forwarding")
        kubernetes.stopPortFowarding(repoName)

        println("Updating deployment")
        kubernetes.updateStatefulSetVolumes(repoName, volumesApi.listVolumes(repoName).toList())

        println("Waiting for deployment to be ready")
        kubernetes.waitForStatefulSet(repoName)

        println("Starting port forwarding")
        kubernetes.startPortForwarding(repoName)
    }
}
