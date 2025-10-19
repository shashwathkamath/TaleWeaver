package com.kamath.taleweaver.login.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun LoginScreen(
    viewmodel: LoginScreenViewmodel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = uiState.successMessage, key2 = uiState.errorMessage) {
        if (uiState.successMessage != null) {
            snackbarHostState.showSnackbar("Login Successful")
            onLoginSuccess()
        } else if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage.toString())
            viewmodel.onEvent(LoginUiEvent.ErrorDismissed)
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            val email = uiState.email
            val password = uiState.password
            val isLoading = uiState.isLoading
            LoginScreenContent(
                email = email,
                password = password,
                onEvent = viewmodel::onEvent,
                isLoading = isLoading
            )
        }
    }
}

@Composable
fun LoginScreenContent(
    email: String,
    password: String,
    onEvent: (LoginUiEvent) -> Unit,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = {
                onEvent(LoginUiEvent.OnEmailChange(it))
            },
            label = { Text("Enter email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                onEvent(LoginUiEvent.OnPasswordChange(it))
            },
            label = { Text("Enter password") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            modifier = Modifier
                .height(40.dp)
                .width(100.dp),
            onClick = {
                onEvent(LoginUiEvent.LoginButtonPress)
            }) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .width(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            } else {
                Text("Login")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenContentPreview() {
    LoginScreenContent(
        email = "",
        password = "",
        onEvent = {},
        isLoading = true
    )
}