package com.kamath.taleweaver.core.util

interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    object ClearFocus : UiEvent
}