package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val blocks = splitMarkdownBlocks(content)
        for (block in blocks) {
            when (block) {
                is MarkdownBlock.Code -> {
                    CodeBlockView(
                        code = block.code,
                        language = block.language
                    )
                }
                is MarkdownBlock.Header -> {
                    Text(
                        text = buildInlineAnnotatedString(block.text, textColor),
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            2 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            else -> MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        },
                        color = textColor,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                is MarkdownBlock.Bullet -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                        )
                        Text(
                            text = buildInlineAnnotatedString(block.text, textColor),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.Quote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                .padding(vertical = 2.dp)
                        )
                        Text(
                            text = buildInlineAnnotatedString(block.text, textColor),
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = textColor.copy(alpha = 0.85f),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    if (block.text.isNotBlank()) {
                        Text(
                            text = buildInlineAnnotatedString(block.text, textColor),
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Code(val code: String, val language: String) : MarkdownBlock()
    data class Header(val text: String, val level: Int) : MarkdownBlock()
    data class Bullet(val text: String) : MarkdownBlock()
    data class Quote(val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

fun splitMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Code block start ```
        if (line.trim().startsWith("```")) {
            val language = line.trim().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.Code(codeLines.joinToString("\n"), language))
            i++
            continue
        }

        // Headers
        if (line.startsWith("### ")) {
            blocks.add(MarkdownBlock.Header(line.removePrefix("### ").trim(), 3))
            i++
            continue
        }
        if (line.startsWith("## ")) {
            blocks.add(MarkdownBlock.Header(line.removePrefix("## ").trim(), 2))
            i++
            continue
        }
        if (line.startsWith("# ")) {
            blocks.add(MarkdownBlock.Header(line.removePrefix("# ").trim(), 1))
            i++
            continue
        }

        // Bullet point
        if (line.trim().startsWith("- ") || line.trim().startsWith("* ") || line.trim().startsWith("• ")) {
            val clean = line.trim().substring(2).trim()
            blocks.add(MarkdownBlock.Bullet(clean))
            i++
            continue
        }

        // Quote
        if (line.trim().startsWith("> ")) {
            val clean = line.trim().removePrefix("> ").trim()
            blocks.add(MarkdownBlock.Quote(clean))
            i++
            continue
        }

        // Paragraph
        blocks.add(MarkdownBlock.Paragraph(line))
        i++
    }

    return blocks
}

fun buildInlineAnnotatedString(text: String, defaultColor: Color) = buildAnnotatedString {
    var cursor = 0
    while (cursor < text.length) {
        // Check for **bold**
        val nextBold = text.indexOf("**", cursor)
        val nextCode = text.indexOf("`", cursor)

        if (nextBold != -1 && (nextCode == -1 || nextBold < nextCode)) {
            // Append plain text before bold
            append(text.substring(cursor, nextBold))
            val endBold = text.indexOf("**", nextBold + 2)
            if (endBold != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                    append(text.substring(nextBold + 2, endBold))
                }
                cursor = endBold + 2
            } else {
                append("**")
                cursor = nextBold + 2
            }
        } else if (nextCode != -1) {
            // Append plain text before code
            append(text.substring(cursor, nextCode))
            val endCode = text.indexOf("`", nextCode + 1)
            if (endCode != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFF262C36).copy(alpha = 0.5f),
                        color = Color(0xFF38EF7D),
                        fontSize = 13.sp
                    )
                ) {
                    append(" ${text.substring(nextCode + 1, endCode)} ")
                }
                cursor = endCode + 1
            } else {
                append("`")
                cursor = nextCode + 1
            }
        } else {
            append(text.substring(cursor))
            break
        }
    }
}
