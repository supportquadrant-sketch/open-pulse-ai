package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.theme.DesignTokens
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance animated Gradient Waves background in Jetpack Compose
 * Recreating the React Bits GradientWaves WebGL plasma effect with multi-layered undulating
 * sine waves, horizon fog gradient, crest highlights, and interactive touch parallax.
 */
@Composable
fun GradientWavesBackground(
    modifier: Modifier = Modifier,
    horizonColor: Color = DesignTokens.ColorPalette.SurfaceBase,
    waveColor: Color = Color(0xFF1F3B9B), // Deep Cobalt
    crestColor: Color = Color(0xFF60A5FA), // Bright Cobalt Crest
    accentColor: Color = Color(0xFF00E5FF), // Cyan Spark
    speed: Float = 1.0f,
    amplitude: Float = 28f,
    opacity: Float = 0.85f,
    interactive: Boolean = true,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f * 4,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (14000 / speed).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_phase"
    )

    var touchOffsetX by remember { mutableFloatStateOf(0f) }
    var touchOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (interactive) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                touchOffsetX = (touchOffsetX + dragAmount.x * 0.005f).coerceIn(-1f, 1f)
                                touchOffsetY = (touchOffsetY + dragAmount.y * 0.005f).coerceIn(-1f, 1f)
                            },
                            onDragEnd = {
                                touchOffsetX = 0f
                                touchOffsetY = 0f
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (width <= 0 || height <= 0) return@Canvas

            // 1. Base Horizon Background Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        horizonColor,
                        Color(0xFF050B1E).copy(alpha = opacity),
                        Color(0xFF000000)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // 2. Layered Sine-Plasma Wave Fields (Back to Front)
            val layers = 5
            val stepY = height / (layers + 1.2f)

            for (i in 0 until layers) {
                val layerProgress = i.toFloat() / (layers - 1)
                val baseHeight = height * 0.38f + (i * stepY * 0.7f)
                val phaseOffset = animTime * (0.4f + i * 0.15f) + (i * 1.3f)
                val layerAmp = amplitude * (1.0f + i * 0.5f)

                val waveBrush = Brush.verticalGradient(
                    colors = listOf(
                        crestColor.copy(alpha = (0.20f + layerProgress * 0.45f) * opacity),
                        waveColor.copy(alpha = (0.35f + layerProgress * 0.55f) * opacity),
                        Color(0xFF050914).copy(alpha = (0.8f + layerProgress * 0.2f) * opacity)
                    ),
                    startY = baseHeight - layerAmp * 2,
                    endY = height
                )

                drawWaveLayer(
                    width = width,
                    height = height,
                    baseY = baseHeight + (touchOffsetY * 40f * (1f - layerProgress)),
                    amplitude = layerAmp,
                    phase = phaseOffset + (touchOffsetX * 2.5f),
                    frequency = 0.0035f - (i * 0.0004f),
                    brush = waveBrush,
                    swell = 25f + i * 8f
                )
            }

            // 3. Top Crest Accent Glow
            val topCrestPath = Path()
            val crestY = height * 0.42f
            topCrestPath.moveTo(0f, crestY)
            var x = 0f
            while (x <= width) {
                val y = crestY +
                        sin(x * 0.004f + animTime + touchOffsetX) * amplitude * 0.8f +
                        cos(x * 0.008f - animTime * 0.7f) * (amplitude * 0.4f)
                topCrestPath.lineTo(x, y)
                x += 16f
            }
            topCrestPath.lineTo(width, crestY)

            // 4. Subtle Radial Atmospheric Glow in Center Horizon
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.12f * opacity),
                        Color.Transparent
                    ),
                    center = Offset(width * (0.5f + touchOffsetX * 0.2f), height * 0.45f),
                    radius = width * 0.7f
                )
            )
        }

        // Render content over the gradient waves
        content()
    }
}

private fun DrawScope.drawWaveLayer(
    width: Float,
    height: Float,
    baseY: Float,
    amplitude: Float,
    phase: Float,
    frequency: Float,
    brush: Brush,
    swell: Float
) {
    val path = Path()
    path.moveTo(0f, height)
    path.lineTo(0f, baseY)

    val step = 12f
    var currX = 0f
    while (currX <= width + step) {
        val mx = currX + swell * sin((baseY + currX) / 180f + phase)
        val y = baseY +
                (sin(mx * frequency + phase) * amplitude) +
                (cos(currX * frequency * 1.8f - phase * 0.6f) * (amplitude * 0.45f))

        path.lineTo(currX, y)
        currX += step
    }

    path.lineTo(width, height)
    path.close()

    drawPath(path = path, brush = brush)
}
