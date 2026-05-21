package com.taleweaver.app.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taleweaver.app.core.components.BookPageLoadingAnimation
import com.taleweaver.app.core.components.ButtonVariant
import com.taleweaver.app.core.components.TaleWeaverButton
import com.taleweaver.app.core.components.TaleWeaverScaffold
import com.taleweaver.app.core.components.TaleWeaverTextField
import com.taleweaver.app.core.components.TopBars.AppBarType
import com.taleweaver.app.core.navigation.NavigationEvent
import com.taleweaver.app.core.util.Strings

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
        LoginScreenContent(
            email = uiState.email,
            isLoading = uiState.isLoading,
            isButtonEnabled = uiState.isButtonEnabled,
            onEvent = viewmodel::onEvent,
            onNavigateToSignUp = onNavigateToSignUp,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
internal fun LoginScreenContent(
    email: String,
    isLoading: Boolean,
    isButtonEnabled: Boolean,
    onEvent: (LoginUiEvent) -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxSize()) {
        // Hero section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 32.dp)
                .padding(top = 56.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = Strings.Titles.TALE_WEAVER,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Strings.Messages.WELCOME_BACK,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }

        // Form section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 32.dp)
        ) {
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
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(36.dp))

            TaleWeaverButton(
                onClick = { onEvent(LoginUiEvent.SendCodeButtonPress) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isButtonEnabled && !isLoading,
                variant = ButtonVariant.Primary
            ) {
                if (isLoading) {
                    BookPageLoadingAnimation(size = 24.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        "Send Code",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.Messages.NEW_TO_TALEWEAVER,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = Strings.Buttons.CREATE_ACCOUNT,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onNavigateToSignUp)
                )
            }
        }
    }
}
