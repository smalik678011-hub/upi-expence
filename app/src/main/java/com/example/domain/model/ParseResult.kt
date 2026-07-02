package com.example.domain.model

sealed class ParseResult {
    data class Success(val transaction: ParsedTransaction) : ParseResult()
    data class Ignored(val reason: String) : ParseResult()
    data class Invalid(val reason: String) : ParseResult()
    data class Failed(val reason: String, val error: Throwable? = null) : ParseResult()
}
