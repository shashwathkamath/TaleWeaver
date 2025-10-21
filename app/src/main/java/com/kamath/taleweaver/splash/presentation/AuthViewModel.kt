package com.kamath.taleweaver.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.splash.domain.usecases.AuthState
import com.kamath.taleweaver.splash.domain.usecases.CheckAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val checkAuthStateUseCase: CheckAuthStateUseCase
) : ViewModel() {
    private val _authState = MutableStateFlow<Resource<AuthState>>(Resource.Loading())
    val authState = _authState.asStateFlow()

    init {
        determineInitialScreen()
    }

    private fun determineInitialScreen() {
        viewModelScope.launch {
            _authState.value = checkAuthStateUseCase()
        }
    }
}