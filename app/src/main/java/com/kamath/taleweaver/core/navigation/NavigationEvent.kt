package com.kamath.taleweaver.core.navigation

sealed interface NavigationEvent {
    object NavigateToLogin:NavigationEvent
    object NavigateToHome:NavigationEvent
}