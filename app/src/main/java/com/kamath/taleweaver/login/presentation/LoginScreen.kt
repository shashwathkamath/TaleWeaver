package com.kamath.taleweaver.login.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.core.components.BookPageLoadingAnimation
import com.kamath.taleweaver.core.components.ButtonVariant
import com.kamath.taleweaver.core.components.TaleWeaverButton
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TaleWeaverTextField
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.core.util.Strings

@Composable
fun LoginScreen(
    viewmodel: LoginScreenViewmodel = hiltViewModel(),
    onNavigateToSignUp: () -> Unit,
    onNavigateToOtp: (email: String) -> Unit
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewmodel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToOtp -> onNavigateToOtp(event.email)
                else -> {}
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewmodel.onEvent(LoginUiEvent.ErrorDismissed)
        }
    }

    TaleWeaverScaffold(
        appBarType = AppBarType.None,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            LoginScreenContent(
                email = uiState.email,
                isLoading = uiState.isLoading,
                isButtonEnabled = uiState.isButtonEnabled,
                onEvent = viewmodel::onEvent,
                onNavigateToSignUp = onNavigateToSignUp
            )
        }
    }
}

@Composable
internal fun LoginScreenContent(
    email: String,
    isLoading: Boolean,
    isButtonEnabled: Boolean,
    onEvent: (LoginUiEvent) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = Strings.Titles.TALE_WEAVER,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = Strings.Messages.WELCOME_BACK,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )

        TaleWeaverTextField(
            value = email,
            onValueChange = { onEvent(LoginUiEvent.OnEmailChange(it)) },
            label = Strings.Labels.EMAIL,
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onEvent(LoginUiEvent.SendCodeButtonPress)
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We'll send a one-time code to your inbox — no password needed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        TaleWeaverButton(
            onClick = { onEvent(LoginUiEvent.SendCodeButtonPress) },
            modifier = Modifier.fillMaxWidth(),
            enabled = isButtonEnabled && !isLoading,
            variant = ButtonVariant.Primary
        ) {
            if (isLoading) {
                BookPageLoadingAnimation(size = 24.dp, color = MaterialTheme.colorScheme.primary)
            } else {
                Text(
                    "Send Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "OR",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = Strings.Messages.NEW_TO_TALEWEAVER,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TaleWeaverButton(
                text = Strings.Buttons.CREATE_ACCOUNT,
                onClick = onNavigateToSignUp,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Strings.Messages.START_BUYING_SELLING,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
