package com.instagramfocus.app

import android.app.Application
import android.util.Log
import java.io.File

class FocusApplication : Application() {

    lateinit var container: FocusContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("InstagramFocus", "FATAL CRASH on thread ${thread.name}: ${throwable.message}", throwable)
            try {
                val f = File(filesDir, "crash.txt")
                f.writeText("${throwable.javaClass.name}: ${throwable.message}\n" + Log.getStackTraceString(throwable))
            } catch (_: Throwable) {}
        }

        try {
            container = FocusContainer(this)
        } catch (t: Throwable) {
            Log.e("InstagramFocus", "Error initializing container", t)
        }
    }

    companion object {
        lateinit var instance: FocusApplication
            private set
    }
}
