package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyTokenStat

@Composable
fun CanvasBarChart(
    data: List<DailyTokenStat>,
    modifier: Modifier = Modifier,
    barColorStart: Color = MaterialTheme.colorScheme.primary,
    barColorEnd: Color = MaterialTheme.colorScheme.secondary,
    gridColor: Color = MaterialTheme.colorScheme.outline
) {
    val animationProgress = remember { Animatable(0f) }
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val maxTokens = remember(data) {
        (data.maxOfOrNull { it.totalTokens } ?: 100).coerceAtLeast(50)
    }

    val selectedStat = selectedBarIndex?.let { data.getOrNull(it) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "DAILY TOKEN CONSUMPTION",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        if (selectedStat != null) {
            Text(
                text = "${selectedStat.dateString}: ${selectedStat.totalTokens} tokens (${selectedStat.totalMessages} msgs)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Text(
                text = "Tap a bar to inspect daily metrics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(top = 12.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            if (data.isEmpty()) return@detectTapGestures
                            val canvasWidth = size.width
                            val totalBars = data.size
                            val barSlotWidth = canvasWidth / totalBars
                            val index = (offset.x / barSlotWidth).toInt().coerceIn(0, totalBars - 1)
                            selectedBarIndex = if (selectedBarIndex == index) null else index
                        }
                    }
            ) {
                if (data.isEmpty()) return@Canvas

                val width = size.width
                val height = size.height
                val bottomPadding = 24.dp.toPx()
                val topPadding = 12.dp.toPx()
                val usableHeight = height - bottomPadding - topPadding

                // Draw horizontal guide lines
                val steps = 3
                for (step in 0..steps) {
                    val y = topPadding + (usableHeight / steps) * step
                    drawLine(
                        color = gridColor.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val totalBars = data.size
                val slotWidth = width / totalBars
                val barWidth = (slotWidth * 0.58f).coerceAtLeast(6.dp.toPx())

                data.forEachIndexed { index, stat ->
                    val fraction = (stat.totalTokens.toFloat() / maxTokens.toFloat()) * animationProgress.value
                    val barHeight = (usableHeight * fraction).coerceAtLeast(4.dp.toPx())
                    val xCenter = slotWidth * index + slotWidth / 2
                    val left = xCenter - barWidth / 2
                    val top = topPadding + usableHeight - barHeight

                    val isSelected = selectedBarIndex == index

                    val brush = Brush.verticalGradient(
                        colors = if (isSelected) {
                            listOf(Color(0xFF00E5FF), barColorStart)
                        } else {
                            listOf(barColorStart, barColorEnd)
                        },
                        startY = top,
                        endY = topPadding + usableHeight
                    )

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Draw day text label on X-axis (e.g. DD)
                    val label = if (stat.dateString.length >= 5) {
                        stat.dateString.takeLast(5)
                    } else stat.dateString

                    val textPaint = android.graphics.Paint().apply {
                        color = if (isSelected) barColorStart.toArgb() else gridColor.copy(alpha = 0.9f).toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        xCenter,
                        height - 4.dp.toPx(),
                        textPaint
                    )
                }
            }
        }
    }
}
