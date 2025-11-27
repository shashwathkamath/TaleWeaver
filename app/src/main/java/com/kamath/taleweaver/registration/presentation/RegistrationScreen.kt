package com.kamath.taleweaver.registration.presentation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.core.components.BookPageLoadingAnimation
import com.kamath.taleweaver.core.components.ButtonVariant
import com.kamath.taleweaver.core.components.TaleWeaverButton
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TaleWeaverTextField
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.core.util.UiEvent

@Composable
internal fun RegistrationScreen(
    viewmodel: RegistrationViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val onEvent = viewmodel::onEvent

    LaunchedEffect(key1 = true) {
        viewmodel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
            }
        }
    }

    TaleWeaverScaffold(
        appBarType = AppBarType.None,
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
                onEvent = onEvent,
                onNavigateBack = onNavigateBack
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
    onEvent: (RegistrationScreenEvent) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title/Logo
        Text(
            text = Strings.Titles.JOIN_TALE_WEAVER,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = Strings.Messages.CREATE_ACCOUNT_MESSAGE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
        )

        // Username Field
        TaleWeaverTextField(
            value = username,
            onValueChange = {
                onEvent(RegistrationScreenEvent.OnUsernameChange(it))
            },
            label = Strings.Labels.USERNAME,
            leadingIcon = Icons.Default.Person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        TaleWeaverTextField(
            value = email,
            onValueChange = {
                onEvent(RegistrationScreenEvent.OnEmailChange(it))
            },
            label = Strings.Labels.EMAIL,
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        TaleWeaverTextField(
            value = password,
            onValueChange = {
                onEvent(RegistrationScreenEvent.OnPasswordChange(it))
            },
            label = Strings.Labels.PASSWORD,
            leadingIcon = Icons.Default.Lock,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onEvent(RegistrationScreenEvent.OnSignUpButtonPress)
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Helper Text
        Text(
            text = Strings.Messages.PASSWORD_HELPER,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Register Button
        TaleWeaverButton(
            onClick = {
                onEvent(RegistrationScreenEvent.OnSignUpButtonPress)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            variant = ButtonVariant.Primary
        ) {
            if (isLoading) {
                BookPageLoadingAnimation(
                    size = 24.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    Strings.Buttons.CREATE_ACCOUNT,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider with "OR"
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

        // Back to Login
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = Strings.Messages.ALREADY_HAVE_ACCOUNT,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TaleWeaverButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    Strings.Buttons.BACK_TO_LOGIN,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
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
