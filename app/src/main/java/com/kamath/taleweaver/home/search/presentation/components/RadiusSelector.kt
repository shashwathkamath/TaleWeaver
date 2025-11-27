package com.kamath.taleweaver.home.search.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Radius options in kilometers
 */
enum class RadiusOption(val km: Double, val label: String) {
    SMALL(10.0, "10 km"),
    MEDIUM(20.0, "20 km"),
    LARGE(50.0, "50 km")
}

@Composable
fun RadiusSelector(
    selectedRadius: Double,
    onRadiusSelected: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Radius options (shown when expanded)
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RadiusOption.entries.forEach { option ->
                    RadiusOptionButton(
                        option = option,
                        isSelected = option.km == selectedRadius,
                        onClick = {
                            onRadiusSelected(option.km)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Main FAB - Circular with matching theme
        FloatingActionButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .shadow(2.dp, CircleShape)
                .size(56.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Select radius",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun RadiusOptionButton(
    option: RadiusOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .shadow(if (isSelected) 2.dp else 1.dp, CircleShape)
            .size(56.dp),
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onBackground
        }
    ) {
        Text(
            text = "${option.km.toInt()}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}
