package com.kamath.taleweaver.util.errorHandler.network

enum class ApiError(val message: String) {
    INVALID_CREDENTIALS("Invalid credentials"),
    CLIENT_ERROR("Client error"),
    SERVER_ERROR("Server error"),
    UNKNOWN_ERROR("Unknown error")
}