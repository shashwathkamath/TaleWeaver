package com.kamath.taleweaver.core.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin

/**
 * Book page turning animation for loading states
 * Represents pages flipping in a book-like manner
 */
@Composable
fun BookPageLoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bookPageAnimation")

    // Page flip animation - cycles through 0 to 360 degrees
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Page curl effect - creates a wave-like motion
    val curlPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "curlPhase"
    )

    Canvas(modifier = modifier.size(size)) {
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2

        // Draw 3 pages with different opacities and rotations
        for (i in 0 until 3) {
            val pageRotation = rotation + (i * 120f)
            val opacity = 1f - (i * 0.3f)
            val pageColor = color.copy(alpha = opacity)

            rotate(pageRotation, pivot = Offset(centerX, centerY)) {
                drawBookPage(
                    color = pageColor,
                    curlPhase = curlPhase,
                    pageIndex = i
                )
            }
        }
    }
}

private fun DrawScope.drawBookPage(
    color: Color,
    curlPhase: Float,
    pageIndex: Int
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val centerX = canvasWidth / 2
    val centerY = canvasHeight / 2

    val pageWidth = canvasWidth * 0.4f
    val pageHeight = canvasHeight * 0.6f

    // Calculate curl effect
    val curlAmount = sin(curlPhase + pageIndex * 0.5f) * 0.15f

    // Draw left page
    val leftPagePath = Path().apply {
        moveTo(centerX - pageWidth, centerY - pageHeight / 2)
        lineTo(centerX - curlAmount * pageWidth, centerY)
        lineTo(centerX - pageWidth, centerY + pageHeight / 2)
        close()
    }
    drawPath(leftPagePath, color)

    // Draw right page (with curl)
    val rightPagePath = Path().apply {
        moveTo(centerX + curlAmount * pageWidth, centerY)
        lineTo(centerX + pageWidth, centerY - pageHeight / 2)
        lineTo(centerX + pageWidth, centerY + pageHeight / 2)
        close()
    }
    drawPath(rightPagePath, color.copy(alpha = color.alpha * 0.7f))

    // Draw spine
    drawLine(
        color = color,
        start = Offset(centerX, centerY - pageHeight / 2),
        end = Offset(centerX, centerY + pageHeight / 2),
        strokeWidth = 2f
    )
}

/**
 * Simpler dots animation alternative - three dots bouncing like turning pages
 */
@Composable
fun BookDotsLoadingAnimation(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    spacing: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotsAnimation")

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing, delayMillis = 100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Canvas(modifier = modifier.size(dotSize * 3 + spacing * 2, dotSize * 2)) {
        val maxBounce = dotSize.toPx()
        val dotRadius = dotSize.toPx() / 2
        val spacingPx = spacing.toPx()

        // Draw three dots
        listOf(dot1Offset, dot2Offset, dot3Offset).forEachIndexed { index, offset ->
            val x = dotRadius + (index * (dotSize.toPx() + spacingPx))
            val y = size.height / 2 - (offset * maxBounce)

            drawCircle(
                color = color,
                radius = dotRadius,
                center = Offset(x, y)
            )
        }
    }
}
