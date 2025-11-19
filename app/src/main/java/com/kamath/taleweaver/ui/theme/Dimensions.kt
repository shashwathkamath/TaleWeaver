package com.kamath.taleweaver.ui.theme

import androidx.compose.ui.unit.dp

/**
 * TaleWeaver Dimensions System
 * Centralized dimensions for consistent spacing and sizing across the app
 *
 * Usage:
 * ```
 * modifier = Modifier.height(Dimensions.buttonHeight)
 * ```
 */
object Dimensions {

    // SPACING
    /** 4dp - Minimum spacing */
    val spaceExtraSmall = 4.dp

    /** 8dp - Small spacing between related elements */
    val spaceSmall = 8.dp

    /** 12dp - Default spacing between elements */
    val spaceMedium = 12.dp

    /** 16dp - Standard content padding */
    val spaceLarge = 16.dp

    /** 24dp - Large spacing between sections */
    val spaceExtraLarge = 24.dp

    /** 32dp - Extra large spacing for major sections */
    val space2XLarge = 32.dp

    /** 48dp - Maximum spacing */
    val space3XLarge = 48.dp

    // COMPONENT HEIGHTS
    /** 40dp - Compact button/input height */
    val buttonHeightCompact = 40.dp

    /** 48dp - Standard button/input height */
    val buttonHeight = 48.dp

    /** 56dp - Large button height */
    val buttonHeightLarge = 56.dp

    /** 28dp - Extra compact tab height for cleaner UI */
    val tabHeight = 28.dp

    /** 40dp - Extra compact app bar height for cleaner UI */
    val appBarHeight = 40.dp

    /** 40dp - Extra compact bottom navigation bar height */
    val bottomNavigationHeight = 40.dp

    // ICON SIZES
    /** 16dp - Extra small icon */
    val iconExtraSmall = 16.dp

    /** 20dp - Small icon */
    val iconSmall = 20.dp

    /** 24dp - Standard icon */
    val iconMedium = 24.dp

    /** 32dp - Large icon */
    val iconLarge = 32.dp

    /** 48dp - Extra large icon */
    val iconExtraLarge = 48.dp

    // CARD DIMENSIONS
    /** 8dp - Card padding */
    val cardPaddingSmall = 8.dp

    /** 12dp - Standard card padding */
    val cardPadding = 12.dp

    /** 16dp - Large card padding */
    val cardPaddingLarge = 16.dp

    /** 2dp - Card elevation */
    val cardElevation = 2.dp

    /** 4dp - Card elevation when pressed */
    val cardElevationPressed = 4.dp

    /** 8dp - Card elevation for featured content */
    val cardElevationFeatured = 8.dp

    // IMAGE/THUMBNAIL SIZES
    /** 48dp - Small thumbnail */
    val thumbnailSmall = 48.dp

    /** 80dp - Standard thumbnail */
    val thumbnail = 80.dp

    /** 120dp - Large thumbnail */
    val thumbnailLarge = 120.dp

    /** 200dp - Book cover size */
    val bookCoverWidth = 120.dp
    val bookCoverHeight = 180.dp

    // PROFILE/AVATAR SIZES
    /** 32dp - Small avatar */
    val avatarSmall = 32.dp

    /** 48dp - Standard avatar */
    val avatar = 48.dp

    /** 72dp - Large avatar */
    val avatarLarge = 72.dp

    /** 96dp - Extra large avatar (profile pages) */
    val avatarExtraLarge = 96.dp

    // DIVIDERS
    /** 1dp - Standard divider thickness */
    val dividerThickness = 1.dp

    /** 3dp - Tab indicator thickness */
    val tabIndicatorThickness = 3.dp

    // MIN HEIGHTS FOR TEXT FIELDS
    /** 120dp - Minimum height for multiline text fields */
    val textFieldMultilineMinHeight = 120.dp
}

/**
 * Common padding values used throughout the app
 */
object Padding {
    /** 0dp - No padding */
    val none = 0.dp

    /** 4dp - Extra small padding */
    val extraSmall = Dimensions.spaceExtraSmall

    /** 8dp - Small padding */
    val small = Dimensions.spaceSmall

    /** 12dp - Medium padding */
    val medium = Dimensions.spaceMedium

    /** 16dp - Large padding (default screen padding) */
    val large = Dimensions.spaceLarge

    /** 24dp - Extra large padding */
    val extraLarge = Dimensions.spaceExtraLarge

    /** Screen edge padding */
    val screen = Dimensions.spaceLarge

    /** Card content padding */
    val card = Dimensions.cardPadding

    /** List item padding */
    val listItem = Dimensions.spaceLarge
}
