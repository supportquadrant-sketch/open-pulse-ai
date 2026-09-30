package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MessageEntity
import com.example.ui.theme.DesignTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(
    message: MessageEntity,
    personaName: String = "OpenPulse Assistant",
    onRegenerate: (() -> Unit)? = null,
    onBookmarkToggle: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.role == "user"
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isUser) {
        DesignTokens.ColorPalette.SurfaceElevated
    } else if (message.isError) {
        DesignTokens.ColorPalette.StateErrorBg
    } else {
        DesignTokens.ColorPalette.SurfaceRaised
    }

    val borderColor = if (isUser) {
        DesignTokens.ColorPalette.AccentCobaltDeep
    } else if (message.isError) {
        DesignTokens.ColorPalette.StateError
    } else {
        DesignTokens.ColorPalette.BorderMuted
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.Space6, vertical = DesignTokens.Spacing.Space4),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Author Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.Space2, vertical = DesignTokens.Spacing.Space2)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(DesignTokens.ColorPalette.AccentCobalt),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = DesignTokens.ColorPalette.TextInverse,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.Space4))
                Text(
                    text = personaName,
                    style = MaterialTheme.typography.labelLarge,
                    color = DesignTokens.ColorPalette.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (!message.modelUsed.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.Space4))
                    Surface(
                        modifier = Modifier.clip(DesignTokens.Radius.ShapeTag),
                        color = DesignTokens.ColorPalette.AccentCobaltDeep.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = message.modelUsed,
                            style = MaterialTheme.typography.labelSmall,
                            color = DesignTokens.ColorPalette.AccentCobaltLight,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "You",
                    style = MaterialTheme.typography.labelLarge,
                    color = DesignTokens.ColorPalette.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.Space4))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(DesignTokens.ColorPalette.SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = DesignTokens.ColorPalette.TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Bubble Content Card
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.88f else 1f)
                .clip(bubbleShape)
                .border(1.dp, borderColor, bubbleShape),
            color = bubbleBg,
            shape = bubbleShape
        ) {
            Column(modifier = Modifier.padding(DesignTokens.Spacing.Space6)) {
                if (message.isError) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = DesignTokens.ColorPalette.StateError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.Space4))
                        Text(
                            text = "Generation Issue",
                            style = MaterialTheme.typography.titleMedium,
                            color = DesignTokens.ColorPalette.StateError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.padding(top = DesignTokens.Spacing.Space2))
                }

                // Render text with markdown support
                MarkdownText(
                    content = message.content,
                    textColor = DesignTokens.ColorPalette.TextPrimary
                )

                // Metadata Footer (Tokens, Latency, Timestamp, Actions)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = DesignTokens.Spacing.Space5),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Telemetry badges
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = DesignTokens.ColorPalette.TextTertiary,
                            fontSize = 10.sp
                        )

                        if (message.tokenUsage > 0) {
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.Space5))
                            Text(
                                text = "${message.tokenUsage} tok",
                                style = MaterialTheme.typography.labelSmall,
                                color = DesignTokens.ColorPalette.AccentCobaltLight,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }

                        if (message.latencyMs > 0) {
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.Space5))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = DesignTokens.ColorPalette.AccentCyan,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${message.latencyMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DesignTokens.ColorPalette.AccentCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Action Icons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("chat_message", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("copy_message_btn_${message.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = DesignTokens.ColorPalette.TextTertiary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        if (onBookmarkToggle != null) {
                            IconButton(
                                onClick = onBookmarkToggle,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("bookmark_message_btn_${message.id}")
                            ) {
                                Icon(
                                    imageVector = if (message.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (message.isBookmarked) DesignTokens.ColorPalette.AccentCobalt else DesignTokens.ColorPalette.TextTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        if (!isUser && onRegenerate != null) {
                            IconButton(
                                onClick = onRegenerate,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("regenerate_message_btn_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Regenerate",
                                    tint = DesignTokens.ColorPalette.TextTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        if (onDelete != null) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("delete_message_btn_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = DesignTokens.ColorPalette.TextTertiary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
