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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class RayOrigin {
    TopRight,
    TopLeft,
    BottomRight,
    BottomLeft;

    companion object {
        fun fromString(str: String): RayOrigin {
            return when (str.lowercase()) {
                "top-left" -> TopLeft
                "bottom-right" -> BottomRight
                "bottom-left" -> BottomLeft
                else -> TopRight
            }
        }
    }
}

/**
 * SideRays animated background component recreated in Jetpack Compose
 * matching the React Bits <SideRays /> component.
 *
 * Implements harmonic volumetric light rays projected from canvas corners with
 * angular fan spreading, dynamic multi-seed oscillation, color layer blending,
 * radial falloff, and optional drag parallax.
 *
 * @param speed Animation speed of the rays (default: 2.5)
 * @param rayColor1 Color of the first ray layer (default: #EAB308)
 * @param rayColor2 Color of the second ray layer (default: #96c8ff)
 * @param intensity Overall brightness of the rays (default: 2.0)
 * @param spread Angular width of the ray fan (default: 2.0)
 * @param origin Corner of the canvas from which the rays emerge (default: TopRight)
 * @param tilt Rotation of the ray fan in degrees (default: 0.0)
 * @param saturation Color saturation booster factor (default: 1.5)
 * @param blend Balance between rayColor1 (0.0) and rayColor2 (1.0) (default: 0.75)
 * @param falloff Distance attenuation power (default: 1.6)
 * @param opacity Overall opacity of the effect (default: 1.0)
 * @param backgroundColor Base dark backdrop color (default: #050811)
 * @param interactive Whether dragging adjusts ray tilt interactively (default: true)
 */
@Composable
fun SideRaysBackground(
    modifier: Modifier = Modifier,
    speed: Float = 2.5f,
    rayColor1: Color = Color(0xFFEAB308), // Golden Yellow
    rayColor2: Color = Color(0xFF96C8FF), // Sky Blue
    intensity: Float = 2.0f,
    spread: Float = 2.0f,
    origin: RayOrigin = RayOrigin.TopRight,
    tilt: Float = 0.0f,
    saturation: Float = 1.5f,
    blend: Float = 0.75f,
    falloff: Float = 1.6f,
    opacity: Float = 1.0f,
    backgroundColor: Color = Color(0xFF040711),
    interactive: Boolean = true,
    content: @Composable () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "side_rays_anim")
    val durationMs = (10000 / speed.coerceAtLeast(0.1f)).toInt().coerceIn(1500, 30000)
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rays_time"
    )

    var interactiveTilt by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (interactive) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                interactiveTilt = (interactiveTilt + dragAmount.x * 0.05f).coerceIn(-25f, 25f)
                            },
                            onDragEnd = {
                                interactiveTilt = 0f
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

            // 1. Deep Space Atmospheric Backdrop
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0A1124),
                        backgroundColor,
                        Color(0xFF020409)
                    ),
                    center = Offset(
                        when (origin) {
                            RayOrigin.TopRight -> w * 0.9f
                            RayOrigin.TopLeft -> w * 0.1f
                            RayOrigin.BottomRight -> w * 0.9f
                            RayOrigin.BottomLeft -> w * 0.1f
                        },
                        when (origin) {
                            RayOrigin.TopRight, RayOrigin.TopLeft -> h * 0.1f
                            RayOrigin.BottomRight, RayOrigin.BottomLeft -> h * 0.9f
                        }
                    ),
                    radius = max(w, h) * 1.2f
                )
            )

            // Determine light source origin coordinates
            val sourcePos = when (origin) {
                RayOrigin.TopRight -> Offset(w * 1.05f, -h * 0.15f)
                RayOrigin.TopLeft -> Offset(-w * 0.05f, -h * 0.15f)
                RayOrigin.BottomRight -> Offset(w * 1.05f, h * 1.15f)
                RayOrigin.BottomLeft -> Offset(-w * 0.05f, h * 1.15f)
            }

            // Base direction angle pointing inward
            val baseAngleRad = when (origin) {
                RayOrigin.TopRight -> (3.0 * PI / 4.0).toFloat() // ~135 deg (towards bottom-left)
                RayOrigin.TopLeft -> (PI / 4.0).toFloat()         // ~45 deg (towards bottom-right)
                RayOrigin.BottomRight -> (-3.0 * PI / 4.0).toFloat() // ~ -135 deg (towards top-left)
                RayOrigin.BottomLeft -> (-PI / 4.0).toFloat()     // ~ -45 deg (towards top-right)
            }

            val totalTiltDeg = tilt + interactiveTilt
            val halfSpreadRad = spread * 0.22f
            val rayCount = 18
            val maxRayLen = sqrt(w * w + h * h) * 1.4f

            // Compute saturated ray colors
            val satRay1 = applySaturation(rayColor1, saturation)
            val satRay2 = applySaturation(rayColor2, saturation)

            // Blended composite tone
            val blendedRayColor = Color(
                red = (satRay1.red * (1f - blend) + satRay2.red * blend).coerceIn(0f, 1f),
                green = (satRay1.green * (1f - blend) + satRay2.green * blend).coerceIn(0f, 1f),
                blue = (satRay1.blue * (1f - blend) + satRay2.blue * blend).coerceIn(0f, 1f),
                alpha = 1f
            )

            rotate(degrees = totalTiltDeg, pivot = sourcePos) {
                // 2. Volumetric Ray Beams Fan
                for (i in 0 until rayCount) {
                    val progress = (i.toFloat() / (rayCount - 1) - 0.5f) * 2f // -1.0 to 1.0
                    val rayAngle = baseAngleRad + progress * halfSpreadRad

                    // Multi-seed harmonic pulsation matching shader seeds 36.2214 & 21.11349
                    val harmonic1 = sin(progress * 3.6f + animTime * 1.2f) * 0.45f + 0.55f
                    val harmonic2 = cos(-progress * 2.1f + animTime * 0.4f) * 0.35f + 0.65f
                    val seedMod = (harmonic1 * (1f - blend) + harmonic2 * blend)

                    val beamIntensity = intensity * seedMod * (1f - 0.3f * (progress * progress))
                    val beamAlpha = (0.28f * beamIntensity * opacity).coerceIn(0f, 0.85f)

                    // Alternate/blend ray colors across the fan
                    val isPrimary = (i % 2 == 0)
                    val baseBeamColor = if (isPrimary) {
                        satRay1.copy(alpha = beamAlpha * (1f - blend * 0.5f))
                    } else {
                        satRay2.copy(alpha = beamAlpha * (0.5f + blend * 0.5f))
                    }

                    val angularWidth = (0.045f + 0.025f * sin(animTime + i)) * (spread * 0.5f).coerceAtLeast(0.4f)
                    val angleLeft = rayAngle - angularWidth
                    val angleRight = rayAngle + angularWidth

                    val p1 = Offset(
                        x = sourcePos.x + cos(angleLeft) * maxRayLen,
                        y = sourcePos.y + sin(angleLeft) * maxRayLen
                    )
                    val p2 = Offset(
                        x = sourcePos.x + cos(angleRight) * maxRayLen,
                        y = sourcePos.y + sin(angleRight) * maxRayLen
                    )

                    val beamPath = Path().apply {
                        moveTo(sourcePos.x, sourcePos.y)
                        lineTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        close()
                    }

                    // Ray beam gradient with distance falloff
                    val beamGradient = Brush.radialGradient(
                        colors = listOf(
                            baseBeamColor.copy(alpha = beamAlpha),
                            baseBeamColor.copy(alpha = (beamAlpha * 0.6f)),
                            baseBeamColor.copy(alpha = (beamAlpha * 0.15f)),
                            Color.Transparent
                        ),
                        center = sourcePos,
                        radius = maxRayLen * (1.2f / falloff.coerceAtLeast(0.5f))
                    )

                    drawPath(
                        path = beamPath,
                        brush = beamGradient,
                        blendMode = BlendMode.Screen
                    )
                }

                // 3. Central Atmospheric Source Flare & Glow
                val flareRadius = max(w, h) * 0.75f
                val flareAlpha = (0.45f * (intensity * 0.5f) * opacity).coerceIn(0f, 0.9f)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = flareAlpha * 0.9f),
                            blendedRayColor.copy(alpha = flareAlpha * 0.7f),
                            satRay2.copy(alpha = flareAlpha * 0.4f),
                            satRay1.copy(alpha = flareAlpha * 0.15f),
                            Color.Transparent
                        ),
                        center = sourcePos,
                        radius = flareRadius / (falloff * 0.8f).coerceAtLeast(0.5f)
                    ),
                    center = sourcePos,
                    radius = flareRadius,
                    blendMode = BlendMode.Screen
                )

                // 4. Secondary Soft Ambient Cone
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            satRay2.copy(alpha = (0.25f * opacity * intensity).coerceIn(0f, 0.6f)),
                            Color.Transparent
                        ),
                        center = Offset(
                            sourcePos.x + cos(baseAngleRad) * (w * 0.35f),
                            sourcePos.y + sin(baseAngleRad) * (h * 0.35f)
                        ),
                        radius = max(w, h) * 0.6f
                    ),
                    center = Offset(
                        sourcePos.x + cos(baseAngleRad) * (w * 0.35f),
                        sourcePos.y + sin(baseAngleRad) * (h * 0.35f)
                    ),
                    radius = max(w, h) * 0.6f,
                    blendMode = BlendMode.Plus
                )
            }
        }

        // Child UI rendered above SideRays
        content()
    }
}

/**
 * Boosts or desaturates color saturation factor
 */
private fun applySaturation(color: Color, saturation: Float): Color {
    val r = color.red
    val g = color.green
    val b = color.blue
    val gray = 0.299f * r + 0.587f * g + 0.114f * b
    val newR = (gray + (r - gray) * saturation).coerceIn(0f, 1f)
    val newG = (gray + (g - gray) * saturation).coerceIn(0f, 1f)
    val newB = (gray + (b - gray) * saturation).coerceIn(0f, 1f)
    return Color(newR, newG, newB, color.alpha)
}
