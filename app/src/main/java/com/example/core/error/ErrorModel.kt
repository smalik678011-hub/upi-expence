package com.example.core.error

import androidx.annotation.StringRes
import com.example.R

data class ErrorModel(
    val code: String,
    val message: String,
    @param:StringRes val localizedMsgRes: Int = R.string.error_generic,
    val throwable: Throwable? = null
) {
    companion object {
        val Generic = ErrorModel(
            code = "GENERIC_ERROR",
            message = "Something went wrong. Please try again later.",
            localizedMsgRes = R.string.error_generic
        )
        val DatabaseError = ErrorModel(
            code = "DATABASE_ERROR",
            message = "Unable to read/write from local storage.",
            localizedMsgRes = R.string.error_db
        )
        val SecurityError = ErrorModel(
            code = "SECURITY_ERROR",
            message = "Authentication or encryption operation failed.",
            localizedMsgRes = R.string.error_security
        )
        val ParserError = ErrorModel(
            code = "PARSER_ERROR",
            message = "Unable to extract financial details from standard format.",
            localizedMsgRes = R.string.error_parser
        )
    }
}
