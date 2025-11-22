package com.kamath.taleweaver.home.account.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.kamath.taleweaver.ui.theme.TaleWeaverScaffold
import timber.log.Timber

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
                    Timber.d("Inside NavController")
                }

                else -> {}
            }
        }
    }

    TaleWeaverScaffold(
        title = "My Account",
        actions = {
            // Only show the Save button when the state is Success and there's a profile
            if (uiState is AccountScreenState.Success && (uiState as AccountScreenState.Success).userProfile != null) {
                TextButton(
                    onClick = { onEvent(AccountScreenEvent.OnSaveClick) },
                ) {
                    Text(
                        "Save",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            is AccountScreenState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is AccountScreenState.Success -> {
                if (state.userProfile != null) {
                    AccountDetails(
                        modifier = Modifier.padding(innerPadding),
                        userProfile = state.userProfile,
                        name = state.userProfile.username,
                        description = state.userProfile.description,
                        onNameChange = { /* TODO */ },
                        onDescriptionChange = { newDesc ->
                            onEvent(AccountScreenEvent.OnDescriptionChange(newDesc))
                        },
                        onLogoutClick = { onEvent(AccountScreenEvent.OnLogoutClick) }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Could not load profile.")
                    }
                }
            }

            is AccountScreenState.Error -> {}
        }
    }
}