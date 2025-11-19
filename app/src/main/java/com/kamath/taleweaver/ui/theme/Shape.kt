package com.kamath.taleweaver.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * TaleWeaver Shape System
 * Book-themed shapes with rounded corners for a friendly, approachable feel
 */
val AppShapes = Shapes(
    // Extra Small - For chips, badges, small indicators
    extraSmall = RoundedCornerShape(4.dp),

    // Small - For small buttons, small cards
    small = RoundedCornerShape(8.dp),

    // Medium - For cards, dialogs, text fields
    medium = RoundedCornerShape(12.dp),

    // Large - For bottom sheets, large cards
    large = RoundedCornerShape(16.dp),

    // Extra Large - For modal sheets, large containers
    extraLarge = RoundedCornerShape(24.dp)
)
