package io.titandata.progresstracker


object ApplicationProgressTracker {
    var progressTracker: io.titandata.progresstracker.ProgressTracker = io.titandata.progresstracker.PrintStreamProgressTracker(
            successMessage = "Success!",
            failureMessage = "Something failed!")
}
