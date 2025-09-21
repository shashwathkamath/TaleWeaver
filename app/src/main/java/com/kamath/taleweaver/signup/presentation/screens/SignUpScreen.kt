package com.kamath.taleweaver.signup.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kamath.taleweaver.signup.presentation.viewmodel.SignUpScreenAction
import com.kamath.taleweaver.signup.presentation.viewmodel.SignUpScreenState
import com.kamath.taleweaver.signup.presentation.viewmodel.SignUpViewmodel

@Composable
internal fun SignUpScreen(
    viewmodel: SignUpViewmodel = hiltViewModel(),
    navigateToLogin: () -> Unit
) {
    val uiState by viewmodel.uiState.collectAsState()
    LaunchedEffect(key1 = viewmodel.uiAction) {
        viewmodel.uiAction.collect { action ->
            when (action) {
                is SignUpScreenAction.ShowToast -> {}
                is SignUpScreenAction.NavigateToLogin -> {
                    navigateToLogin()
                }
            }
        }
    }
    SignUpScreenContent(uiState)
}

@Composable
fun SignUpScreenContent(uiState: SignUpScreenState) {
    when (uiState) {
        is SignUpScreenState.Input -> {}
        is SignUpScreenState.Loading -> {}
        is SignUpScreenState.Success -> {}
        is SignUpScreenState.Error -> {}
    }
}