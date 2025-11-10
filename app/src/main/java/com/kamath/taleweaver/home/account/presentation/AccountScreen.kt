package com.kamath.taleweaver.home.account.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.account.presentation.components.AccountDetails
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController,
    viewModel: AccountScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val onEvent = viewModel::onEvent

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        withDismissAction = true
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToLogin -> {
                    Timber.d("Inside navcontroller")
                }

                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account") },
                actions = {
                    // Show save button only when profile is loaded
                    if (uiState.userProfile != null) {
                        TextButton(
                            onClick = { TODO() },
                        ) {
                            Text(
                                "Save",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.userProfile != null -> {
                    // Pass the state down and hoist events up
                    AccountDetails(
                        userProfile = uiState.userProfile!!,
                        name = uiState.userProfile!!.username,
                        description = uiState.userProfile!!.description,
                        onNameChange = { newName -> TODO() },
                        onDescriptionChange = { newDesc ->
                            onEvent(
                                AccountScreenEvent.OnDescriptionChange(
                                    newDesc
                                )
                            )
                        },
                        onLogoutClick = { onEvent(AccountScreenEvent.OnLogoutClick) }
                    )
                }

                else -> Text("Could not load profile.")
            }
        }
    }
}