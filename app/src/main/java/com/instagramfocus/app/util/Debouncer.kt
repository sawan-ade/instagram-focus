package com.instagramfocus.app.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Utility to debounce rapid accessibility events to minimize CPU usage
 * and prevent UI stutter or battery consumption.
 */
class Debouncer(
    private val delayMs: Long = 150L,
    private val scope: CoroutineScope
) {
    private var job: Job? = null

    fun debounce(action: suspend () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(delayMs)
            action()
        }
    }

    fun cancel() {
        job?.cancel()
    }
}
