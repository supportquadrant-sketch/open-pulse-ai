package com.example.ui.screens.analytics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CanvasBarChart
import com.example.ui.components.CanvasDonutChart
import com.example.ui.components.MetricCard
import com.example.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    analyticsViewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalConversations by analyticsViewModel.totalConversations.collectAsState()
    val totalMessages by analyticsViewModel.totalMessages.collectAsState()
    val totalTokens by analyticsViewModel.totalTokens.collectAsState()
    val avgLatency by analyticsViewModel.avgLatency.collectAsState()
    val topPersona by analyticsViewModel.topPersona.collectAsState()
    val modelStats by analyticsViewModel.modelUsageStats.collectAsState()
    val dailyStats by analyticsViewModel.dailyStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Analytics & Telemetry",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Offline SQLite Metrics Dashboard",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics 2x2 Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Total Tokens",
                            value = String.format("%,d", totalTokens),
                            subtitle = "Prompts & Responses",
                            icon = Icons.Default.Token,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Avg Latency",
                            value = "${avgLatency.toInt()} ms",
                            subtitle = "Per Assistant Turn",
                            icon = Icons.Default.Speed,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Total Messages",
                            value = "$totalMessages",
                            subtitle = "Across $totalConversations chats",
                            icon = Icons.Default.ChatBubbleOutline,
                            accentColor = Color(0xFFA78BFA),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Top Persona",
                            value = (topPersona ?: "Default").take(10),
                            subtitle = "Most utilized role",
                            icon = Icons.Default.Memory,
                            accentColor = Color(0xFFFBBF24),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Daily Token Consumption Bar Chart
            item {
                CanvasBarChart(data = dailyStats)
            }

            // Model Distribution Donut Chart
            item {
                CanvasDonutChart(stats = modelStats)
            }

            // Privacy and Local Storage Info Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Zero Cloud Tracking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All tokens, latencies, and message histories are strictly persisted inside your local Room SQLite database.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Copy Telemetry Report Button
            item {
                Button(
                    onClick = {
                        val report = buildString {
                            append("=== OPENPULSE AI TELEMETRY REPORT ===\n")
                            append("Total Tokens: $totalTokens\n")
                            append("Total Messages: $totalMessages\n")
                            append("Total Conversations: $totalConversations\n")
                            append("Average Latency: ${avgLatency.toInt()}ms\n")
                            append("Top Persona: ${topPersona ?: "Default"}\n")
                            append("Model Breakdown:\n")
                            modelStats.forEach {
                                append(" - ${it.modelId}: ${it.count} calls (${it.totalTokens} tokens)\n")
                            }
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("telemetry_report", report))
                        Toast.makeText(context, "Telemetry summary copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("export_telemetry_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Full Telemetry Summary", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
