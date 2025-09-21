package com.kamath.taleweaver.util.errorHandler.network

import retrofit2.HttpException

fun Throwable.toNetworkError(): NetworkError {
    val error = when (this) {
        is HttpException -> {
            when (this.code()) {
                401 -> ApiError.INVALID_CREDENTIALS
                in 400..499 -> ApiError.CLIENT_ERROR
                in 500..599 -> ApiError.SERVER_ERROR
                else -> ApiError.UNKNOWN_ERROR
            }
        }

        else -> ApiError.UNKNOWN_ERROR
    }
    return NetworkError(error, this)
}