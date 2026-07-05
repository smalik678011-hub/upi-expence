package com.example.core.log

import android.util.Log

interface Logger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

class AndroidLogger : Logger {
    override fun d(tag: String, message: String) {
        if (com.example.BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    override fun i(tag: String, message: String) {
        if (com.example.BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (com.example.BuildConfig.DEBUG) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, sanitize(message), throwable)
        }
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (com.example.BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, sanitize(message), throwable)
        }
    }

    private fun sanitize(message: String): String {
        return message.replace(Regex("\\d{4,}"), "[REDACTED]")
    }
}
