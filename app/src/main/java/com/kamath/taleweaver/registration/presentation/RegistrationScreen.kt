package com.kamath.taleweaver.registration.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
internal fun RegistrationScreen(
    viewmodel: RegistrationViewModel
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val onEvent = viewmodel::onEvent
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            val username = uiState.username
            val password = uiState.password
            val email = uiState.email
            val isLoading = uiState.isLoading
            RegistrationScreenContent(
                username = username,
                email = email,
                password = password,
                isLoading = isLoading,
                onEvent = onEvent
            )
        }
    }
}

@Composable
fun RegistrationScreenContent(
    username: String,
    password: String,
    email: String,
    isLoading: Boolean,
    onEvent: (RegistrationScreenEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = {
                onEvent(RegistrationScreenEvent.OnUsernameChange(it))
            },
            label = { Text("Enter username") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                onEvent(RegistrationScreenEvent.OnEmailChange(it))
            },
            label = { Text("Enter email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                onEvent(RegistrationScreenEvent.OnPasswordChange(it))
            },
            label = { Text("Enter password") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                onEvent(RegistrationScreenEvent.OnSignUpButtonPress)
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Register")
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun RegistrationScreenContentPreview() {
    RegistrationScreenContent(
        "",
        "",
        "",
        false,
        {}
    )
}