package com.kamath.taleweaver.core.components

import androidx.compose.foundation.BorderStroke
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
    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .shadow(if (enabled) 2.dp else 0.dp, RoundedCornerShape(cornerRadius))
                    .height(48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(cornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = contentPadding,
                content = content
            )
        }

        ButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .shadow(if (enabled) 2.dp else 0.dp, RoundedCornerShape(cornerRadius))
                    .height(48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(cornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = contentPadding,
                content = content
            )
        }

        ButtonVariant.Error -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .shadow(if (enabled) 2.dp else 0.dp, RoundedCornerShape(cornerRadius))
                    .height(48.dp),
                enabled = enabled,
                shape = RoundedCornerShape(cornerRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = contentPadding,
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
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = contentPadding,
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
