/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.providers.kubernetes

import io.titandata.client.apis.CommitsApi
import io.titandata.client.apis.RepositoriesApi
import io.titandata.client.apis.VolumesApi
import io.titandata.titan.clients.Kubernetes

class Checkout (
        private val kubernetes: Kubernetes = Kubernetes(),
        private val commitsApi: CommitsApi = CommitsApi(),
        private val repositoriesApi: RepositoriesApi = RepositoriesApi(),
        private val volumesApi: VolumesApi = VolumesApi()
) {
    fun checkout(repo: String, guid: String?, tags: List<String>) {
        val sourceCommit = if (guid == null) {
            if (tags.isNotEmpty()) {
                val commits = commitsApi.listCommits(repo, tags)
                if (commits.size == 0) {
                    throw IllegalStateException("no matching commits found")
                }
                commits.first().id
            } else {
                val status = repositoriesApi.getRepositoryStatus(repo)
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

        val status = commitsApi.getCommitStatus(repo, sourceCommit)
        if (!status.ready) {
            println("Waiting for commit to be ready")
            while (true) {
                val commitStatus = commitsApi.getCommitStatus(repo, sourceCommit)
                if (commitStatus.ready) {
                    break
                }
                Thread.sleep(1000L)
            }
        }

        println("Checkout $sourceCommit")
        commitsApi.checkoutCommit(repo, sourceCommit)

        println("Stopping port forwarding")
        kubernetes.stopPortFowarding(repo)

        println("Updating deployment")
        kubernetes.updateStatefulSetVolumes(repo, volumesApi.listVolumes(repo).toList())

        println("Waiting for deployment to be ready")
        kubernetes.waitForStatefulSet(repo)

        println("Starting port forwarding")
        kubernetes.startPortForwarding(repo)
    }
}