package com.example.domain.parser

interface ParserLogger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String, throwable: Throwable? = null)
    fun error(message: String, throwable: Throwable? = null)
}
