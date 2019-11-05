/*
 * Copyright (c) 2019 by Delphix. All rights reserved.
 */
package io.titandata.titan.utils

import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import kotlin.concurrent.thread

class ProgressTracker {
    private fun getStyle(): ProgressBarStyle {
        val term = System.getenv("TERM")
        return when  {
            term.contains("xterm") -> ProgressBarStyle.COLORFUL_UNICODE_BLOCK
            else -> ProgressBarStyle.ASCII
        }
    }
    fun track(title: String, function: () -> Any) {
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
}