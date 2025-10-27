package com.kamath.taleweaver.home.feed.utils


/**
 * Formats large numbers for display (e.g., 1200 -> 1.2k).
 */
private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> "${(count / 100_000) / 10.0}M"
        count >= 1000 -> "${(count / 100) / 10.0}k"
        else -> count.toString()
    }
}