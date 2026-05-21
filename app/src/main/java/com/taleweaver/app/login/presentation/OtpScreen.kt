package com.taleweaver.app.login.presentation

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.taleweaver.app.core.components.BookPageLoadingAnimation
import com.taleweaver.app.core.components.ButtonVariant
import com.taleweaver.app.core.components.TaleWeaverButton
import com.taleweaver.app.core.components.TaleWeaverScaffold
import com.taleweaver.app.core.components.TopBars.AppBarType
import com.taleweaver.app.core.navigation.NavigationEvent

@Composable
fun OtpScreen(
    viewModel: OtpViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current

    // Navigate on success
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            if (event is NavigationEvent.NavigateToHome) onNavigateToHome()
        }
    }

    // Show error in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(OtpUiEvent.ErrorDismissed)
        }
    }

    // Autofill from clipboard when the screen resumes (e.g., user copies code from email app)
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
            if (clip != null && clip.length == 6 && clip.all { it.isDigit() }) {
                viewModel.onEvent(OtpUiEvent.OnOtpChanged(clip))
            }
        }
    }

    TaleWeaverScaffold(
        appBarType = AppBarType.None,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Check your email",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We sent a 6-digit code to",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OtpInputField(
                value = uiState.otpValue,
                onValueChange = { viewModel.onEvent(OtpUiEvent.OnOtpChanged(it)) },
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            TaleWeaverButton(
                onClick = { viewModel.onEvent(OtpUiEvent.VerifyButtonPress) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.otpValue.length == 6 && !uiState.isLoading,
                variant = ButtonVariant.Primary
            ) {
                if (uiState.isLoading) {
                    BookPageLoadingAnimation(
                        size = 24.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "Verify Code",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.canResend) {
                TextButton(onClick = { viewModel.onEvent(OtpUiEvent.ResendCode) }) {
                    Text(
                        "Resend Code",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Text(
                    text = "Resend code in ${uiState.remainingSeconds}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OtpInputField(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int = 6,
    enabled: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(contentAlignment = Alignment.Center) {
        // Invisible real text field captures keyboard input
        BasicTextField(
            value = value,
            onValueChange = { new ->
                if (new.length <= length && new.all { it.isDigit() }) onValueChange(new)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            enabled = enabled,
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(1.dp)
                .alpha(0.01f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(length) { index ->
                val char = value.getOrNull(index)
                val isCurrent = value.length == index
                OtpBox(
                    char = char?.toString() ?: "",
                    isFocused = isCurrent && enabled,
                    onClick = { focusRequester.requestFocus() }
                )
            }
        }
    }
}

@Composable
private fun OtpBox(char: String, isFocused: Boolean, onClick: () -> Unit) {
    val borderColor = when {
        isFocused -> MaterialTheme.colorScheme.primary
        char.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outline
    }
    val borderWidth = if (isFocused) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isFocused && char.isEmpty()) {
            // Blinking cursor indicator
            Box(
                modifier = Modifier
                    .size(width = 2.dp, height = 24.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            Text(
                text = char,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
