package com.kamath.taleweaver.signup.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kamath.taleweaver.signup.presentation.viewmodel.SignUpScreenAction
import com.kamath.taleweaver.signup.presentation.viewmodel.SignUpScreenEvent
import com.kamath.taleweaver.signup.presentation.viewmodel.SignUpScreenState
import com.kamath.taleweaver.signup.presentation.viewmodel.SignUpViewmodel

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        topBar = { TopAppBar({ Text("Tale Weaver") }) }
    ) { paddingValues ->
        SignUpScreenContent(
            uiState,
            onEmailChange = { viewmodel.onEvent(SignUpScreenEvent.OnEmailChange(it)) },
            onUsernameChange = { viewmodel.onEvent(SignUpScreenEvent.OnUsernameChange(it)) },
            onPasswordChange = { viewmodel.onEvent(SignUpScreenEvent.OnPasswordChange(it)) },
            onSignUpClick = { viewmodel.onEvent(SignUpScreenEvent.OnSignUpClick) },
        )
    }

}

@Composable
fun SignUpScreenContent(
    uiState: SignUpScreenState,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit
) {
    when (uiState) {
        is SignUpScreenState.Input -> {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { onEmailChange(it) },
                    label = { Text("Enter Email") },
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = { onUsernameChange(it) },
                    label = { Text("Enter Username") },
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { onPasswordChange(it) },
                    label = { Text("Enter Password") },
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Button(onClick = {
                    onSignUpClick()
                }) {
                    Text("Sign Up")
                }
            }
        }

        is SignUpScreenState.Loading -> {}
        is SignUpScreenState.Success -> {}
        is SignUpScreenState.Error -> {}
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    val uiState = SignUpScreenState.Input(
        email = "",
        username = "",
        password = "",
        isButtonEnabled = false
    )
    SignUpScreenContent(
        uiState = uiState,
        onEmailChange = {},
        onUsernameChange = {},
        onPasswordChange = {},
        onSignUpClick = {}
    )
}