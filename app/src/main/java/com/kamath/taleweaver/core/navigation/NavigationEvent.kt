package com.kamath.taleweaver.core.navigation

sealed interface NavigationEvent {
    object NavigateToLogin : NavigationEvent
    object NavigateToHome : NavigationEvent
    data class NavigateToOtp(val email: String, val username: String? = null) : NavigationEvent
}