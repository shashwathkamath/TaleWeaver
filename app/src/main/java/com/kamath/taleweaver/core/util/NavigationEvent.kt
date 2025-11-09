package com.kamath.taleweaver.core.util

interface NavigationEvent {
    object NavigateToLogin : NavigationEvent
    object NavigateToHome : NavigationEvent
}