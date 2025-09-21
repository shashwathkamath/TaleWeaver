package com.kamath.taleweaver.util.errorHandler.network

data class NetworkError(
    val error: ApiError,
    val throwable: Throwable? = null
)
