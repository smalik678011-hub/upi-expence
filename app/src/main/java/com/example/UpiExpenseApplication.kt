package com.example

import android.app.Application
import com.example.core.di.AppContainer

class UpiExpenseApplication : Application() {
    
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.adManager.initialize()
        setupGlobalCrashHandler()
    }

    private fun setupGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                container.logger.e("CrashHandler", "Uncaught exception on thread: ${thread.name}", throwable)
            } catch (e: Exception) {
                // Fail-safe to avoid crash in handler
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
