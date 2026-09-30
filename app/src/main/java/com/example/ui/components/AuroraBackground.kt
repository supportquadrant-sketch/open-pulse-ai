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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Aurora component recreated in Jetpack Compose matching the React Bits <Aurora /> component.
 *
 * Implements smooth undulating aurora curtains with harmonic wave synthesis,
 * exponential vertical drop-off, 3-stop color ramp, screen-blend bloom,
 * and interactive touch parallax.
 *
 * @param colorStops 3 hex colors defining the aurora gradient. Default: ["#67b7ff", "#b9ffe8", "#5227FF"]
 * @param speed Controls animation speed. Higher values make the aurora move faster. Default: 0.5
 * @param blend Controls the blending of the aurora effect with the background. Default: 1.0
 * @param amplitude Controls the height intensity of the aurora effect. Default: 1.0
 * @param lightMode Whether to render in light mode. Default: false
 * @param backgroundColor Deep background color behind the aurora curtains. Default: #030712
 * @param interactive Enables dragging to tilt and interact with the aurora. Default: true
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    colorStops: List<Color> = listOf(
        Color(0xFF67B7FF), // Electric Blue (#67b7ff)
        Color(0xFFB9FFE8), // Mint Aqua (#b9ffe8)
        Color(0xFF5227FF)  // Deep Cobalt (#5227FF)
    ),
    speed: Float = 0.5f,
    blend: Float = 1.0f,
    amplitude: Float = 1.0f,
    lightMode: Boolean = false,
    backgroundColor: Color = if (lightMode) Color(0xFFF8FAFC) else Color(0xFF030712),
    interactive: Boolean = true,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_anim")
    val durationMs = (18000 / speed.coerceAtLeast(0.1f)).toInt().coerceIn(2000, 60000)
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831853f * 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora_time"
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
                                touchOffsetX = (touchOffsetX + dragAmount.x * 0.002f).coerceIn(-0.8f, 0.8f)
                                touchOffsetY = (touchOffsetY + dragAmount.y * 0.002f).coerceIn(-0.8f, 0.8f)
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
            val w = size.width
            val h = size.height
            if (w <= 0 || h <= 0) return@Canvas

            // 1. Deep Space Atmospheric Base
            if (lightMode) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor,
                            Color(0xFFE2E8F0),
                            backgroundColor
                        ),
                        startY = 0f,
                        endY = h
                    )
                )
            } else {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor,
                            Color(0xFF070E22),
                            backgroundColor
                        ),
                        startY = 0f,
                        endY = h
                    )
                )
            }

            val c1 = colorStops.getOrElse(0) { Color(0xFF67B7FF) }
            val c2 = colorStops.getOrElse(1) { Color(0xFFB9FFE8) }
            val c3 = colorStops.getOrElse(2) { Color(0xFF5227FF) }

            // 2. Multi-layer undulating Aurora Ribbons
            val curtains = 4
            for (layer in 0 until curtains) {
                val progress = layer.toFloat() / (curtains - 1)
                val layerSpeed = 0.5f + layer * 0.3f
                val layerTime = animTime * layerSpeed + layer * 1.5f
                val layerAmp = amplitude * (36f + layer * 18f)
                val baseY = h * (0.22f + progress * 0.26f) + (touchOffsetY * 60f * (1f - progress))

                // Color ramp across horizon
                val layerBrush = Brush.horizontalGradient(
                    colors = listOf(
                        c1.copy(alpha = (0.45f + progress * 0.35f) * blend),
                        c2.copy(alpha = (0.55f + progress * 0.35f) * blend),
                        c3.copy(alpha = (0.45f + progress * 0.35f) * blend),
                        c1.copy(alpha = (0.35f + progress * 0.25f) * blend)
                    ),
                    startX = 0f,
                    endX = w
                )

                drawAuroraRibbon(
                    w = w,
                    h = h,
                    baseY = baseY,
                    time = layerTime,
                    amplitude = layerAmp,
                    touchX = touchOffsetX,
                    brush = layerBrush,
                    frequency = 0.0028f + layer * 0.0006f
                )
            }

            // 3. Central Ambient Aurora Radial Bloom
            val glowCenter = Offset(
                x = w * (0.5f + touchOffsetX * 0.25f),
                y = h * (0.32f + touchOffsetY * 0.15f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        c2.copy(alpha = 0.30f * blend),
                        c1.copy(alpha = 0.18f * blend),
                        Color.Transparent
                    ),
                    center = glowCenter,
                    radius = w * 0.70f
                ),
                center = glowCenter,
                radius = w * 0.70f,
                blendMode = if (lightMode) BlendMode.Darken else BlendMode.Screen
            )

            // 4. Secondary Electric Top/Corner Bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        c3.copy(alpha = 0.22f * blend),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.25f, h * 0.18f),
                    radius = w * 0.50f
                ),
                blendMode = if (lightMode) BlendMode.Darken else BlendMode.Plus
            )
        }

        // Child UI
        content()
    }
}

/**
 * Draws an organic undulating Aurora Ribbon with multi-frequency harmonics and exponential vertical modulation
 */
private fun DrawScope.drawAuroraRibbon(
    w: Float,
    h: Float,
    baseY: Float,
    time: Float,
    amplitude: Float,
    touchX: Float,
    brush: Brush,
    frequency: Float
) {
    val path = Path()
    path.moveTo(0f, h)

    val step = 10f
    var x = 0f

    while (x <= w + step) {
        val t1 = (x * frequency * 1.2f) + (time * 0.4f) + (touchX * 2.0f)
        val t2 = (x * frequency * 2.4f) - (time * 0.60f)
        val t3 = (x * frequency * 0.5f) + (time * 0.15f)

        val s1 = sin(t1)
        val s2 = cos(t2) * 0.55f
        val s3 = sin(t3) * 0.35f
        val harmonic = (s1 + s2 + s3) / 1.9f

        // Exponential height modulation matching the React Bits shader height = exp(snoise) curve
        val expHeight = exp((harmonic * 0.65f).coerceIn(-1.5f, 1.5f))
        val yOffset = harmonic * amplitude * expHeight

        val y = (baseY + yOffset).coerceIn(0f, h)

        if (x == 0f) {
            path.lineTo(0f, y)
        } else {
            path.lineTo(x, y)
        }
        x += step
    }

    path.lineTo(w, h)
    path.close()

    drawPath(path = path, brush = brush)
}
