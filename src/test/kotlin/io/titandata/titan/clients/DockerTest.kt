/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */

package io.titandata.titan.clients

import io.titandata.titan.utils.CommandExecutor
import io.titandata.titan.clients.Docker.Companion.toList
import io.titandata.titan.clients.Docker.Companion.fetchName
import io.titandata.titan.clients.Docker.Companion.hasDetach
import io.titandata.titan.clients.Docker.Companion.runtimeToArguments
import org.junit.Test
import com.nhaarman.mockitokotlin2.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DockerTest {
    private val executor = mock<CommandExecutor> {
        on {exec(listOf("docker", "-v")) } doReturn "Docker version 18.09.2, build 6247962"
        on {exec(listOf("docker", "images", "titan", "--format", "\"{{.Repository}}\""))} doReturn "titan"
        on {exec(listOf("docker", "ps", "-f", "name=titan-launch", "--format", "\"{{.Names}}\""))} doReturn "titan-launch"
        on {exec(listOf("docker", "ps", "-f", "name=titan-server", "--format", "\"{{.Names}}\""))} doReturn "titan-server"
        on {exec(listOf("docker", "pull", "titan"))} doReturn """Using default tag: latest
latest: Pulling from titan
Digest: sha256:bc6f593df26c0631a1ce3a06afdd5a4a8fda703b071fb18805091e2372c68201
Status: Downloaded newer image for titan:latest
        """
        on {exec(listOf("docker","tag","source","target"))} doReturn ""
        on {exec(listOf("docker","stop","container"))} doReturn "container"
        on {exec(listOf("docker","start","container"))} doReturn "container"
        on {exec(listOf("docker", "inspect",  "--type", "container", "container"))} doReturn """[
    {
        "Id": "48a93f6ac01c054dc8eb02313dc28450b48e4368cf29d8a8366200368f0e7789",
    }
]"""
        on {exec(listOf("docker", "inspect",  "--type", "image", "image"))} doReturn """[
    {
        "Id": "48a93f6ac01c054dc8eb02313dc28450b48e4368cf29d8a8366200368f0e7789",
    }
]"""
        on {exec(listOf("docker", "volume", "create", "-d", "titan", "-o", "path=path", "volume"))} doReturn "volume"
        on {exec(listOf("docker", "volume", "rm", "volume"))} doReturn "volume"
        on {exec(listOf("docker", "cp", "-a", "source/.", "titan-server:/var/lib/titan/mnt/target"))} doReturn ""
        on {exec(listOf("docker", "rm", "-f", "container"))} doReturn ""
        on {exec(listOf("docker", "rm", "container"))} doReturn ""
        on {exec(listOf("docker", "run", "--name", "name", "image", "entry.sh"))} doReturn "entry output string"
        on {exec(listOf("docker", "run", "--name", "name", "image"))} doReturn "no entry output string"
        on {exec(listOf("docker", "run", "--privileged","--pid=host","--network=host","-d","--restart","always","--name=titan-launch","-v", "/var/lib:/var/lib","-v", "/run/docker:/run/docker","-v", "/lib:/var/lib/titan/system","-v", "titan-data:/var/lib/titan/data","-v", "/var/run/docker.sock:/var/run/docker.sock", "-e", "TITAN_IMAGE=titan:latest", "-e", "TITAN_IDENTITY=titan", "titan:latest", "/bin/bash", "/titan/launch"))} doReturn ""
    }
    private val docker = Docker(executor)

    @Test
    fun `can get version`() {
        assertEquals("Docker version 18.09.2, build 6247962", docker.version())
    }

    @Test
    fun `can check if titan images are downloaded`() {
        assertTrue(docker.titanIsDownloaded())
    }

    @Test
    fun `can check if titan images are not downloaded`() {
        val falseExecutor = mock<CommandExecutor> {
            on {exec(listOf("docker", "images", "titan", "--format", "\"{{.Repository}}\""))} doReturn ""
        }
        val falseDocker = Docker(falseExecutor)
        assertFalse(falseDocker.titanIsDownloaded())
    }

    @Test
    fun `can check if titan-launch is available`() {
        assertTrue(docker.titanLaunchIsAvailable())
    }

    @Test
    fun `can check if titan-launch is not available`() {
        val falseExecutor = mock<CommandExecutor> {
            on {exec(listOf("docker", "ps", "-f", "name=titan-launch", "--format", "\"{{.Names}}\""))} doReturn ""
        }
        val falseDocker = Docker(falseExecutor)
        assertFalse(falseDocker.titanLaunchIsAvailable())
    }

    @Test
    fun `can get string from list`() {
        val checkList = listOf("Test","String","From","List")
        assertEquals(checkList, "Test String From List".toList())
    }

    @Test
    fun `can check if titan is running`() {
        assertTrue(docker.titanServerIsAvailable())
    }

    @Test
    fun `can check if titan is not running`() {
        val falseExecutor = mock<CommandExecutor> {
            on {exec(listOf("docker", "ps", "-f", "name=titan-server", "--format", "\"{{.Names}}\""))} doReturn ""
        }
        val falseDocker = Docker(falseExecutor)
        assertFalse(falseDocker.titanServerIsAvailable())
    }

    @Test
    fun `can pull titan`() {
        assertTrue(docker.pull("titan").contains("sha256:bc6f593df26c0631a1ce3a06afdd5a4a8fda703b071fb18805091e2372c68201"))
    }

    @Test
    fun `can tag images`() {
        assertEquals("", docker.tag("source", "target"))
    }

    @Test
    fun `can stop container`(){
        assertEquals("container", docker.stop("container"))
    }

    @Test
    fun `can start container`(){
        assertEquals("container", docker.start("container"))
    }

    @Test
    fun `can inspect container`(){
        val result = docker.inspectContainer("container")
        assertEquals("48a93f6ac01c054dc8eb02313dc28450b48e4368cf29d8a8366200368f0e7789", result!!.getString("Id"))
    }

    @Test
    fun `can inspect image`(){
        val result = docker.inspectImage("image")
        assertEquals("48a93f6ac01c054dc8eb02313dc28450b48e4368cf29d8a8366200368f0e7789", result!!.getString("Id"))
    }

    @Test
    fun `can create volume`(){
        val result = docker.createVolume("volume", "path")
        assertEquals("volume", result)
    }

    @Test
    fun `can remove volume`(){
        val result = docker.removeVolume("volume")
        assertEquals("volume", result)
    }

    @Test
    fun `can copy files to volume`(){
        val result = docker.cp("source", "target")
        assertEquals("", result)
    }

    @Test
    fun `can force rm container`(){
        val result = docker.rm("container", true)
        assertEquals("", result)
    }

    @Test
    fun `can rm container`(){
        val result = docker.rm("container", false)
        assertEquals("", result)
    }

    @Test
    fun `can run container with entry`(){
        val result = docker.run("image", "entry.sh", listOf("--name","name"))
        assertEquals("entry output string", result)
    }

    @Test
    fun `can run container without entry`(){
        val result = docker.run("image", "", listOf("--name","name"))
        assertEquals("no entry output string", result)
    }

    @Test
    fun `can launch titan servers`() {
        val result = docker.launchTitanServers()
        assertEquals("", result)
    }

    @Test
    fun `can get name from metadata`(){
        val arguments = listOf("--name","nameValue")
        assertEquals("nameValue", arguments.fetchName())
    }

    @Test
    fun `can get generated name from metadata`(){
        val arguments = emptyList<String>()
        val generatedName = arguments.fetchName()
        assertTrue(generatedName is String)
        assertTrue(generatedName.contains("_"))
    }

    @Test
    fun `run arguments has detach mode`(){
        val arguments = listOf("-d", "container", "image")
        assertTrue(arguments.hasDetach())
        val newArguments = listOf("--detach", "container", "image")
        assertTrue(newArguments.hasDetach())
    }

    @Test
    fun `fun arguments do not have detach mode`(){
        val arguments = listOf("container", "image")
        assertFalse(arguments.hasDetach())
    }

    @Test
    fun `can get arguments from runtime string`(){
        val runtime = "[--label, io.titandata.titan, --mount, type=volume,src=jerrytest/v0,dst=/var/lib/postgresql/data,volume-driver=titan, --name, jerrytest, -d, -p, 5432:5432, -e, POSTGRES_USER=postgres, -e, POSTGRES_PASSWORD=mysecretpassword, postgres:10]"
        val arguments = runtime.runtimeToArguments()
        assertFalse(arguments.contains("--mount"))
        val expected = listOf("--label", "io.titandata.titan", "--name", "jerrytest", "-d", "-p", "5432:5432", "-e", "POSTGRES_USER=postgres", "-e", "POSTGRES_PASSWORD=mysecretpassword", "postgres:10")
        assertEquals(expected, arguments)
        val runtimeWithoutMount = "[--label, io.titandata.titan, --name, jerrytest, -d, -p, 5432:5432, -e, POSTGRES_USER=postgres, -e, POSTGRES_PASSWORD=mysecretpassword, postgres:10]"
        val argumentsWithoutMount = runtimeWithoutMount.runtimeToArguments()
        assertEquals(expected, argumentsWithoutMount)
    }

    @Test
    fun `check output from methods`() {
        //val checkDocker  = Docker(CommandExecutor())
        //println(checkDocker.removeVolume("jerry/v2"))

        assertTrue(true)
    }

}