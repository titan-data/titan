/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */
package io.titandata.titan.utils

import java.io.Console
import kotlin.concurrent.thread
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle

class ProgressTracker {

    private fun isTTY(): Boolean {
        val console = System.console()
        return (console is Console)
    }

    private fun getStyle(): ProgressBarStyle {
        val term = System.getenv("TERM")
        // TODO get better list of switches
        return when {
            term.contains("xterm") -> ProgressBarStyle.COLORFUL_UNICODE_BLOCK
            else -> ProgressBarStyle.ASCII
        }
    }

    private fun trackTitleOnly(title: String, function: () -> Any) {
        println(title)
        function()
    }

    private fun trackWithBar(title: String, function: () -> Any) {
        val thread = thread(start = false) {
            function()
        }
        val pb = ProgressBarBuilder()
                .setTaskName(title)
                .setInitialMax(100)
                .setUpdateIntervalMillis(200)
                .setStyle(getStyle())
                .build()
        thread.start()
        while (thread.isAlive) {
            pb.step()
            Thread.sleep(200)
        }
        pb.stepTo(100).close()
    }

    fun track(title: String, function: () -> Any) {
        if (isTTY()) {
            trackWithBar(title, function)
        } else {
            trackTitleOnly(title, function)
        }
    }
}
