package com.example.core.error

sealed interface ResultWrapper<out T> {
    data class Success<out T>(val data: T) : ResultWrapper<T>
    data class Error(val error: ErrorModel) : ResultWrapper<Nothing>
}
