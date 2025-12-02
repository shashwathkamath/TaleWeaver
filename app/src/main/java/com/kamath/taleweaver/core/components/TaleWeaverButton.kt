package com.kamath.taleweaver.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standardized button component matching the TabChip theme
 * Uses rounded corners, shadows, and Material3 color scheme
 */
@Composable
fun TaleWeaverButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate shadow elevation on press - creates spotlight effect
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 16.dp else 2.dp,
        animationSpec = tween(durationMillis = 200),
        label = "buttonElevation"
    )

    // Animate background color on press
    val animatedPrimaryColor by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(durationMillis = 150),
        label = "buttonPrimaryColor"
    )

    val animatedSecondaryColor by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 150),
        label = "buttonSecondaryColor"
    )

    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .shadow(animatedElevation, RoundedCornerShape(cornerRadius))
                    .height(48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(cornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedPrimaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                content = content
            )
        }

        ButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .shadow(animatedElevation, RoundedCornerShape(cornerRadius))
                    .height(48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(cornerRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = animatedSecondaryColor,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                content = content
            )
        }

        ButtonVariant.Error -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .shadow(animatedElevation, RoundedCornerShape(cornerRadius))
                    .height(48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(cornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPressed && enabled) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                content = content
            )
        }

        ButtonVariant.Text -> {
            androidx.compose.material3.TextButton(
                onClick = onClick,
                modifier = modifier.height(48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(cornerRadius),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isPressed && enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                content = content
            )
        }
    }
}

/**
 * Text-only version of TaleWeaverButton for convenience
 */
@Composable
fun TaleWeaverButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 4.dp
) {
    TaleWeaverButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        cornerRadius = cornerRadius,
        elevation = elevation
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Button variants matching the app theme
 */
enum class ButtonVariant {
    Primary,    // Filled primary color with shadow
    Secondary,  // Outlined with surface background
    Error,      // Outlined with error color
    Text        // Text only, no background
}
