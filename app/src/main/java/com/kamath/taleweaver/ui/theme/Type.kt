package com.kamath.taleweaver.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.kamath.taleweaver.R

/**
 * TaleWeaver Typography System
 * Premium book-themed typography with Google Fonts for impressive, professional text
 *
 * Fonts used:
 * - Playfair Display: Elegant serif for headings (book titles, headers)
 * - Inter: Modern, readable sans-serif for body text
 * - Literata: Literary serif for special displays
 */

// Google Fonts provider
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Playfair Display - Elegant serif for headings
private val playfairDisplayFont = GoogleFont("Playfair Display")
private val PlayfairDisplay = FontFamily(
    Font(googleFont = playfairDisplayFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = playfairDisplayFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = playfairDisplayFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = playfairDisplayFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = playfairDisplayFont, fontProvider = provider, weight = FontWeight.ExtraBold)
)

// Inter - Modern sans-serif for body text
private val interFont = GoogleFont("Inter")
private val Inter = FontFamily(
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Bold)
)

// Literata - Literary serif for special displays
private val literataFont = GoogleFont("Literata")
private val Literata = FontFamily(
    Font(googleFont = literataFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = literataFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = literataFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = literataFont, fontProvider = provider, weight = FontWeight.Bold)
)

/**
 * Premium Typography System
 */
val Typography = Typography(
    // Display - Largest text, for hero sections and splash screens
    // Uses Playfair Display for dramatic, elegant impact
    displayLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headlines - For screen titles, section headers, empty states
    // Uses Playfair Display for elegant, literary feel
    headlineLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Titles - For card titles, book titles, component headers
    // Uses Literata for literary credibility with modern readability
    titleLarge = TextStyle(
        fontFamily = Literata,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Literata,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Literata,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body - For descriptions, help text, permissions, instructions
    // Uses Inter for maximum readability and modern feel
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Labels - For buttons, tabs, chips, badges
    // Uses Inter for clarity and modern UI feel
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)