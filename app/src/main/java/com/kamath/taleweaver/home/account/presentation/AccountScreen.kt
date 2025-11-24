package com.kamath.taleweaver.home.account.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kamath.taleweaver.core.components.MyBox
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.account.presentation.components.AccountDetails
import timber.log.Timber

@Composable
fun AccountScreen(
    navController: NavController,
    onListingClick: (String) -> Unit,
    onViewAllListingsClick: () -> Unit,
    viewModel: AccountScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val onEvent = viewModel::onEvent
    val focusManager = LocalFocusManager.current
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
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
        appBarType = AppBarType.WithActions(
            title = Strings.Titles.ACCOUNT,
            actions = {
                val successState = uiState as? AccountScreenState.Success
                if (successState?.hasUnsavedChanges == true) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onEvent(AccountScreenEvent.OnSaveClick)
                        },
                        enabled = !successState.isSaving
                    ) {
                        if (successState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = Strings.Buttons.SAVE,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            is AccountScreenState.Loading -> {
                MyBox(
                    modifier = Modifier.padding(innerPadding)
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
                        address = state.userProfile.address,
                        myListings = state.myListings,
                        isLoadingListings = state.isLoadingListings,
                        onNameChange = { /* TODO */ },
                        onDescriptionChange = { newDesc ->
                            onEvent(AccountScreenEvent.OnDescriptionChange(newDesc))
                        },
                        onAddressChange = { newAddress ->
                            onEvent(AccountScreenEvent.OnAddressChange(newAddress))
                        },
                        onListingClick = onListingClick,
                        onViewAllListingsClick = onViewAllListingsClick,
                        onLogoutClick = { onEvent(AccountScreenEvent.OnLogoutClick) }
                    )
                } else {
                    MyBox(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        Text(Strings.Errors.PROFILE_LOAD_FAILED)
                    }
                }
            }

            is AccountScreenState.Error -> {
                MyBox(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    Text(state.message)
                }
            }
        }
    }
}