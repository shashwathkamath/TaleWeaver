package com.taleweaver.app.core.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import authNavGraph
import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.presentation.HomeScreen
import com.taleweaver.app.splash.domain.usecases.AuthState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos

// 12 pages, staggered 50ms apart, each flip 130ms → overlapping cascade
private const val PAGE_COUNT = 12
private const val STAGGER_MS = 50L
private const val FLIP_MS = 130

private val PAGE_COLORS = buildList {
    val base = listOf(
        Color(0xFFFFF8F0), Color(0xFFFFF0DC),
        Color(0xFFFFEDD5), Color(0xFFFFF3E4)
    )
    repeat(PAGE_COUNT) { add(base[it % base.size]) }
}

@Composable
fun AppNavigation(authState: ApiResult<AuthState>) {
    var animationDone by remember { mutableStateOf(false) }
    val readyToNavigate = animationDone && authState !is ApiResult.Loading

    if (!readyToNavigate) {
        SplashScreen(onDone = { animationDone = true })
        return
    }

    val startDestination = if (
        authState is ApiResult.Success && authState.data == AuthState.AUTHENTICATED
    ) AppDestination.HOME_SCREEN else AppDestination.AUTH_FLOW

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        authNavGraph(navController)
        composable(AppDestination.HOME_SCREEN) {
            HomeScreen(rootNavController = navController)
        }
    }
}

@Composable
private fun SplashScreen(onDone: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    val bookScale = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    // One Animatable per page — enables overlapping flips
    val flips = remember { Array(PAGE_COUNT) { Animatable(0f) } }

    LaunchedEffect(Unit) {
        // Book pops in with spring
        bookScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        delay(120)

        // Rapid riffle — pages launch with 50ms stagger, overlap in flight
        val jobs = flips.mapIndexed { i, flip ->
            launch {
                delay(i * STAGGER_MS)
                // Haptic on every 3rd page — enough tactile rhythm without overwhelming
                if (i % 3 == 0) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                flip.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(FLIP_MS, easing = FastOutSlowInEasing)
                )
            }
        }
        jobs.forEach { it.join() }

        delay(80)

        // Title and tagline fade in
        launch { titleAlpha.animateTo(1f, animationSpec = tween(420)) }
        delay(200)
        taglineAlpha.animateTo(1f, animationSpec = tween(350))
        delay(520)
        onDone()
    }

    // Derive all splash colors from the active theme's primary so light/dark both look right
    val primary = MaterialTheme.colorScheme.primary
    val gradientTop = lerp(primary, Color.Black, 0.28f)
    val bookCoverColor = lerp(primary, Color.Black, 0.55f)
    val spineColor = lerp(primary, Color.Black, 0.65f)
    val spineHighlightColor = lerp(primary, Color.White, 0.12f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to gradientTop,
                        1.0f to primary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BookCanvas(
                bookScale = bookScale.value,
                flipValues = flips.map { it.value },
                bookCoverColor = bookCoverColor,
                spineColor = spineColor,
                spineHighlightColor = spineHighlightColor
            )

            Spacer(modifier = Modifier.height(44.dp))

            Text(
                text = "TaleWeaver",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your book marketplace",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}

@Composable
private fun BookCanvas(
    bookScale: Float,
    flipValues: List<Float>,
    bookCoverColor: Color,
    spineColor: Color,
    spineHighlightColor: Color
) {
    Canvas(
        modifier = Modifier
            .size(240.dp)
            .scale(bookScale)
    ) {
        val w = size.width
        val h = size.height

        // Book bounds — fills most of the canvas
        val bL = w * 0.05f
        val bR = w * 0.95f
        val bT = h * 0.07f
        val bB = h * 0.93f
        val bW = bR - bL
        val bH = bB - bT

        val spineW = bW * 0.18f
        val spineX = bL + spineW          // x where pages start
        val pageW = bR - spineX           // max page width

        // ── Back cover ──
        drawRoundRect(
            color = bookCoverColor,
            topLeft = Offset(bL, bT),
            size = Size(bW, bH),
            cornerRadius = CornerRadius(14f, 14f)
        )

        // ── Draw each page in order (earlier = further back visually) ──
        for (i in 0 until PAGE_COUNT) {
            val p = flipValues[i]           // 0 = unflipped, 1 = flipped
            val angle = p * 180.0
            val cosVal = cos(Math.toRadians(angle)).toFloat()
            val pageColor = PAGE_COLORS[i]

            when {
                p == 0f -> {
                    // Not yet started — draw as part of right stack
                    val inset = (PAGE_COUNT - 1 - i) * 1.8f
                    drawRect(
                        color = pageColor.copy(alpha = 0.9f),
                        topLeft = Offset(spineX + inset, bT + 4f),
                        size = Size(pageW - inset * 2f, bH - 8f)
                    )
                }
                p == 1f -> {
                    // Fully flipped — draw as part of left stack
                    val inset = i * 1.8f
                    drawRect(
                        color = pageColor.copy(alpha = 0.72f),
                        topLeft = Offset(spineX - (pageW - inset * 2f), bT + 4f),
                        size = Size(pageW - inset * 2f, bH - 8f)
                    )
                }
                cosVal >= 0f -> {
                    // First half of flip — collapsing on the right
                    val pw = pageW * cosVal
                    drawRect(
                        color = pageColor,
                        topLeft = Offset(spineX, bT + 4f),
                        size = Size(pw, bH - 8f)
                    )
                    // Shadow on leading edge gives depth
                    if (pw > 8f) {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.20f * (1f - cosVal)),
                            topLeft = Offset(spineX + pw - 8f, bT + 4f),
                            size = Size(8f, bH - 8f)
                        )
                    }
                }
                else -> {
                    // Second half — expanding on the left
                    val pw = pageW * abs(cosVal)
                    drawRect(
                        color = pageColor.copy(alpha = 0.85f),
                        topLeft = Offset(spineX - pw, bT + 4f),
                        size = Size(pw, bH - 8f)
                    )
                    // Inner shadow on spine edge
                    drawRect(
                        color = Color.Black.copy(alpha = 0.12f),
                        topLeft = Offset(spineX - pw, bT + 4f),
                        size = Size(7f, bH - 8f)
                    )
                }
            }
        }

        // ── Spine (always on top of pages) ──
        drawRoundRect(
            color = spineColor,
            topLeft = Offset(bL, bT),
            size = Size(spineW, bH),
            cornerRadius = CornerRadius(14f, 14f)
        )
        // Spine highlight stripe
        drawRect(
            color = spineHighlightColor.copy(alpha = 0.55f),
            topLeft = Offset(bL + 5f, bT + 16f),
            size = Size(5f, bH - 32f)
        )

        // ── Top edge light ──
        drawRoundRect(
            color = Color.White.copy(alpha = 0.10f),
            topLeft = Offset(bL, bT),
            size = Size(bW, 6f),
            cornerRadius = CornerRadius(14f, 14f)
        )
    }
}
