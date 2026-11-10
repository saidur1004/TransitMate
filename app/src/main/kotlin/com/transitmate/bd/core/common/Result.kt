package com.transitmate.bd.core.common

/**
 * A generic sealed class representing the result of an operation with its status.
 */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val throwable: Throwable, val message: String? = throwable.message) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>
}

