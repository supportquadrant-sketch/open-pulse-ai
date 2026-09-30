package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Fog Sphere Background Component (inspired by React Bits Pro "Fog Sphere").
 *
 * Renders an ethereal, soft, swirling sphere of volumetric fog with luminous
 * core glow, rotating gaseous filaments, drifting mist motes, and organic breathing turbulence.
 */
@Composable
fun FogSphereBackground(
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false,
    coreColor: Color = Color(0xFF22D3EE),      // Luminous Cyan / Teal
    mantleColor: Color = Color(0xFF818CF8),    // Soft Indigo / Violet
    ambientGlow: Color = Color(0xFF0F172A),    // Deep Cosmic Slate
    fogAlpha: Float = 0.45f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fog_sphere_transition")

    // Slow primary rotation of the fog sphere
    val primaryRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isStreaming) 12000 else 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "primary_rotation"
    )

    // Counter swirl rotation for secondary volumetric layers
    val counterSwirl by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isStreaming) 8000 else 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_swirl"
    )

    // Gentle volumetric breathing pulse
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isStreaming) 2200 else 5500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    // Turbulence drift phase
    val turbulencePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isStreaming) 4000 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "turbulence_phase"
    )

    // Pre-calculate deterministic mist motes
    val motes = remember {
        val rand = Random(42)
        List(40) {
            FogMote(
                angle = rand.nextFloat() * 360f,
                distanceFactor = rand.nextFloat() * 0.85f + 0.1f,
                radius = rand.nextFloat() * 12f + 4f,
                speedMultiplier = rand.nextFloat() * 0.7f + 0.6f,
                alpha = rand.nextFloat() * 0.4f + 0.15f,
                isClockwise = rand.nextBoolean()
            )
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.5f, size.height * 0.44f)
            val baseRadius = (size.minDimension * 0.38f) * breathingScale

            // 1. Ambient Outer Fog Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        mantleColor.copy(alpha = 0.22f * fogAlpha),
                        coreColor.copy(alpha = 0.10f * fogAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 2.2f
                ),
                radius = baseRadius * 2.2f,
                center = center
            )

            // 2. Volumetric Spherical Core Base
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to coreColor.copy(alpha = 0.55f * fogAlpha),
                    0.45f to mantleColor.copy(alpha = 0.35f * fogAlpha),
                    0.75f to ambientGlow.copy(alpha = 0.20f * fogAlpha),
                    1.0f to Color.Transparent,
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )

            // 3. Swirling Wisps - Forward Stream Layer
            rotate(degrees = primaryRotation, pivot = center) {
                drawSwirlingFogTendrils(
                    center = center,
                    radius = baseRadius,
                    turbulence = turbulencePhase,
                    color = coreColor.copy(alpha = 0.28f * fogAlpha),
                    tendrilCount = 6,
                    scaleFactor = 0.95f
                )
            }

            // 4. Counter-Swirling Volumetric Mantle Layer
            rotate(degrees = counterSwirl, pivot = center) {
                drawSwirlingFogTendrils(
                    center = center,
                    radius = baseRadius * 0.85f,
                    turbulence = -turbulencePhase * 1.3f,
                    color = mantleColor.copy(alpha = 0.32f * fogAlpha),
                    tendrilCount = 5,
                    scaleFactor = 0.8f
                )
            }

            // 5. Orbiting Fog Particles / Mist Motes
            for (mote in motes) {
                val currentAngle = if (mote.isClockwise) {
                    mote.angle + (primaryRotation * mote.speedMultiplier)
                } else {
                    mote.angle - (counterSwirl * mote.speedMultiplier)
                }
                val rad = Math.toRadians(currentAngle.toDouble())
                val dist = baseRadius * mote.distanceFactor * (1f + 0.08f * sin(turbulencePhase + mote.distanceFactor * 5))
                val px = center.x + (dist * cos(rad)).toFloat()
                val py = center.y + (dist * sin(rad) * 0.85f).toFloat() // slightly elliptic for 3D sphere perspective

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            coreColor.copy(alpha = mote.alpha * fogAlpha),
                            Color.Transparent
                        ),
                        center = Offset(px, py),
                        radius = mote.radius * 2.5f
                    ),
                    radius = mote.radius * 2.5f,
                    center = Offset(px, py),
                    blendMode = BlendMode.Screen
                )
            }

            // 6. Intense Ethereal Fog Center Flare
            val coreShiftX = center.x + 12f * cos(turbulencePhase)
            val coreShiftY = center.y + 10f * sin(turbulencePhase)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color.White.copy(alpha = 0.35f * fogAlpha),
                    0.3f to coreColor.copy(alpha = 0.40f * fogAlpha),
                    0.7f to mantleColor.copy(alpha = 0.15f * fogAlpha),
                    1.0f to Color.Transparent,
                    center = Offset(coreShiftX, coreShiftY),
                    radius = baseRadius * 0.45f
                ),
                radius = baseRadius * 0.45f,
                center = Offset(coreShiftX, coreShiftY),
                blendMode = BlendMode.Screen
            )
        }
    }
}

/**
 * Draws curved, ethereal mist plumes swirling in vortex formation.
 */
private fun DrawScope.drawSwirlingFogTendrils(
    center: Offset,
    radius: Float,
    turbulence: Float,
    color: Color,
    tendrilCount: Int,
    scaleFactor: Float
) {
    val stepAngle = 360f / tendrilCount
    val path = Path()

    for (i in 0 until tendrilCount) {
        val angleDeg = i * stepAngle
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val turbOffset = sin(turbulence + i) * (radius * 0.12f)

        val rOuter = (radius * scaleFactor) + turbOffset
        val rInner = (radius * 0.25f)

        val startX = center.x + (rInner * cos(angleRad)).toFloat()
        val startY = center.y + (rInner * sin(angleRad)).toFloat()

        // Swirled arc control points
        val cp1Angle = angleRad + 0.4
        val cp1Dist = radius * 0.55f
        val cp1X = center.x + (cp1Dist * cos(cp1Angle)).toFloat()
        val cp1Y = center.y + (cp1Dist * sin(cp1Angle)).toFloat()

        val endAngle = angleRad + 1.2
        val endX = center.x + (rOuter * cos(endAngle)).toFloat()
        val endY = center.y + (rOuter * sin(endAngle)).toFloat()

        path.reset()
        path.moveTo(startX, startY)
        path.quadraticTo(cp1X, cp1Y, endX, endY)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color,
                    color.copy(alpha = color.alpha * 0.3f),
                    Color.Transparent
                ),
                center = Offset(cp1X, cp1Y),
                radius = radius * 0.35f
            ),
            radius = radius * 0.35f,
            center = Offset(cp1X, cp1Y),
            blendMode = BlendMode.Screen
        )
    }
}

private data class FogMote(
    val angle: Float,
    val distanceFactor: Float,
    val radius: Float,
    val speedMultiplier: Float,
    val alpha: Float,
    val isClockwise: Boolean
)
