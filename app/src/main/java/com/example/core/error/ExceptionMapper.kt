package com.example.core.error

import java.io.IOException
import java.security.GeneralSecurityException

object ExceptionMapper {
    fun map(throwable: Throwable): ErrorModel {
        return when (throwable) {
            is GeneralSecurityException -> ErrorModel(
                code = "SECURITY_EXCEPTION",
                message = throwable.localizedMessage ?: "General security or cryptographic failure.",
                localizedMsgRes = com.example.R.string.error_security,
                throwable = throwable
            )
            is IOException -> ErrorModel(
                code = "STORAGE_EXCEPTION",
                message = throwable.localizedMessage ?: "Disk I/O failure.",
                localizedMsgRes = com.example.R.string.error_db,
                throwable = throwable
            )
            else -> ErrorModel(
                code = "UNKNOWN_EXCEPTION",
                message = throwable.localizedMessage ?: "An unexpected error occurred.",
                localizedMsgRes = com.example.R.string.error_generic,
                throwable = throwable
            )
        }
    }
}
