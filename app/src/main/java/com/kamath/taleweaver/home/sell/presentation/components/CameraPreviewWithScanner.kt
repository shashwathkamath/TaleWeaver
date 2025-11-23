package com.kamath.taleweaver.home.sell.presentation.components

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.kamath.taleweaver.home.sell.util.IsbnBarcodeAnalyzer
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraPreviewWithScanner(
    onIsbnScanned: (String) -> Unit,
    lifecycleOwner: LifecycleOwner
) {
    val context = LocalContext.current
    var isScanned by remember { mutableStateOf(false) }

    val previewView = remember { PreviewView(context) }
    LaunchedEffect(Unit) {
        val cameraProvider = context.getCameraProvider()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    IsbnBarcodeAnalyzer { isbn ->
                        if (!isScanned) {
                            isScanned = true
                            onIsbnScanned(isbn)
                        }
                    }
                )
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            Timber.e(e, "Camera binding failed")
        }
    }
    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider {
    return suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener({
                continuation.resume(future.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }
}

@Composable
fun ScanFrameOverlay(
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier.size(280.dp, 150.dp)
    ) {
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 30.dp.toPx()
        val cornerRadius = 8.dp.toPx()

        // Semi-transparent background outside the frame
        // (optional - can add dimming effect)

        // Draw corner brackets
        val path = Path().apply {
            // Top-left corner
            moveTo(0f, cornerLength)
            lineTo(0f, cornerRadius)
            quadraticBezierTo(0f, 0f, cornerRadius, 0f)
            lineTo(cornerLength, 0f)

            // Top-right corner
            moveTo(size.width - cornerLength, 0f)
            lineTo(size.width - cornerRadius, 0f)
            quadraticBezierTo(size.width, 0f, size.width, cornerRadius)
            lineTo(size.width, cornerLength)

            // Bottom-right corner
            moveTo(size.width, size.height - cornerLength)
            lineTo(size.width, size.height - cornerRadius)
            quadraticBezierTo(size.width, size.height, size.width - cornerRadius, size.height)
            lineTo(size.width - cornerLength, size.height)

            // Bottom-left corner
            moveTo(cornerLength, size.height)
            lineTo(cornerRadius, size.height)
            quadraticBezierTo(0f, size.height, 0f, size.height - cornerRadius)
            lineTo(0f, size.height - cornerLength)
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
