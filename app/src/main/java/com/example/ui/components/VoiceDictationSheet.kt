package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.util.SpeechLanguage
import com.example.util.SpeechRecognitionHelper
import com.example.util.SpeechRecognitionState
import com.example.util.SupportedLanguages
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceDictationSheet(
    initialText: String = "",
    onTextResult: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    var composedText by remember { mutableStateOf(initialText) }
    var partialText by remember { mutableStateOf("") }
    var speechState by remember { mutableStateOf(SpeechRecognitionState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var normalizedRms by remember { mutableFloatStateOf(0f) }
    var autoSendOnFinish by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(SupportedLanguages.list.first()) }
    var isLanguageMenuOpen by remember { mutableStateOf(false) }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (!isGranted) {
            Toast.makeText(
                context,
                "Microphone permission is required for speech-to-text",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Speech Recognition Helper instance
    val speechHelper = remember {
        SpeechRecognitionHelper(
            context = context,
            onStateChanged = { newState ->
                speechState = newState
                if (newState == SpeechRecognitionState.LISTENING) {
                    errorMessage = null
                }
            },
            onPartialTranscript = { partial ->
                partialText = partial
            },
            onFinalTranscript = { finalPhrase ->
                val updated = if (composedText.isBlank()) {
                    finalPhrase
                } else {
                    "${composedText.trim()} $finalPhrase"
                }
                composedText = updated
                partialText = ""

                if (autoSendOnFinish && updated.isNotBlank()) {
                    onSendMessage(updated)
                    onDismiss()
                }
            },
            onError = { err ->
                errorMessage = err
            },
            onRmsChanged = { rms ->
                normalizedRms = rms
            }
        )
    }

    // Auto-start listening on sheet open if permission is granted
    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            speechHelper.startListening(selectedLanguage.code)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechHelper.destroy()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            speechHelper.stopListening()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("voice_dictation_sheet")
        ) {
            // Header Row with Title, State Badge, Language selector, and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when (speechState) {
                            SpeechRecognitionState.LISTENING -> MaterialTheme.colorScheme.errorContainer
                            SpeechRecognitionState.PROCESSING -> MaterialTheme.colorScheme.primaryContainer
                            SpeechRecognitionState.ERROR -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (speechState) {
                                    SpeechRecognitionState.LISTENING -> Icons.Default.Mic
                                    SpeechRecognitionState.PROCESSING -> Icons.Default.SettingsVoice
                                    SpeechRecognitionState.ERROR -> Icons.Default.MicOff
                                    else -> Icons.Default.Mic
                                },
                                contentDescription = null,
                                tint = when (speechState) {
                                    SpeechRecognitionState.LISTENING -> MaterialTheme.colorScheme.error
                                    SpeechRecognitionState.PROCESSING -> MaterialTheme.colorScheme.primary
                                    SpeechRecognitionState.ERROR -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Speech-to-Text Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (speechState) {
                                SpeechRecognitionState.INITIALIZING -> "Starting microphone..."
                                SpeechRecognitionState.LISTENING -> "Listening... Speak clearly"
                                SpeechRecognitionState.PROCESSING -> "Transcribing speech..."
                                SpeechRecognitionState.ERROR -> errorMessage ?: "Microphone error"
                                SpeechRecognitionState.IDLE -> "Paused. Tap mic to resume"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when (speechState) {
                                SpeechRecognitionState.LISTENING -> MaterialTheme.colorScheme.primary
                                SpeechRecognitionState.ERROR -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language picker pill
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isLanguageMenuOpen = true }
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .testTag("voice_language_picker"),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedLanguage.displayName.take(10),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isLanguageMenuOpen,
                            onDismissRequest = { isLanguageMenuOpen = false }
                        ) {
                            SupportedLanguages.list.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.displayName) },
                                    onClick = {
                                        selectedLanguage = lang
                                        isLanguageMenuOpen = false
                                        if (speechState == SpeechRecognitionState.LISTENING) {
                                            speechHelper.startListening(lang.code)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            speechHelper.stopListening()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_voice_sheet_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Audio Waveform Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .border(
                        1.dp,
                        if (speechState == SpeechRecognitionState.LISTENING) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                AudioWaveformDisplay(
                    isListening = speechState == SpeechRecognitionState.LISTENING,
                    rms = normalizedRms,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Transcript Display Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp, max = 200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        RoundedCornerShape(14.dp)
                    )
                    .testTag("voice_transcript_box"),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (composedText.isEmpty() && partialText.isEmpty()) {
                        Text(
                            text = if (speechState == SpeechRecognitionState.LISTENING)
                                "Speak now... what you say will appear here in real time."
                            else
                                "Tap the microphone below to start speaking...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        val annotated = buildAnnotatedString {
                            append(composedText)
                            if (partialText.isNotBlank()) {
                                if (composedText.isNotBlank()) append(" ")
                                withStyle(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Medium
                                    )
                                ) {
                                    append(partialText)
                                }
                            }
                        }

                        Text(
                            text = annotated,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Punctuation & Formatting Quick-Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PunctuationChip(label = "Period .", onClick = {
                    composedText = if (composedText.isBlank()) "." else "${composedText.trimEnd()}."
                })
                PunctuationChip(label = "Comma ,", onClick = {
                    composedText = if (composedText.isBlank()) "," else "${composedText.trimEnd()},"
                })
                PunctuationChip(label = "Question ?", onClick = {
                    composedText = if (composedText.isBlank()) "?" else "${composedText.trimEnd()}?"
                })
                PunctuationChip(label = "Exclamation !", onClick = {
                    composedText = if (composedText.isBlank()) "!" else "${composedText.trimEnd()}!"
                })
                PunctuationChip(label = "New line ↵", onClick = {
                    composedText = "$composedText\n"
                })
                if (composedText.isNotEmpty()) {
                    PunctuationChip(
                        label = "Clear ⌫",
                        onClick = {
                            composedText = ""
                            partialText = ""
                        },
                        isDestructive = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hands-Free Auto-Send Toggle Switch
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hands-free auto-send",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatically sends when you finish speaking",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoSendOnFinish,
                        onCheckedChange = { autoSendOnFinish = it },
                        modifier = Modifier.testTag("hands_free_autosend_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls Center: Clear / Insert / Giant Pulsing Mic / Send
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left action: Insert into chat draft
                OutlinedButton(
                    onClick = {
                        val fullText = if (partialText.isNotBlank()) {
                            if (composedText.isBlank()) partialText else "$composedText $partialText"
                        } else composedText

                        if (fullText.isNotBlank()) {
                            onTextResult(fullText)
                            speechHelper.stopListening()
                            onDismiss()
                        }
                    },
                    enabled = composedText.isNotBlank() || partialText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("insert_transcript_btn")
                ) {
                    Text("Insert Draft", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Center Main Action: Large Pulsing Mic Button
                PulsingMicButton(
                    isListening = speechState == SpeechRecognitionState.LISTENING,
                    rms = normalizedRms,
                    onClick = {
                        if (!hasAudioPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            return@PulsingMicButton
                        }

                        if (speechState == SpeechRecognitionState.LISTENING) {
                            speechHelper.stopListening()
                        } else {
                            speechHelper.startListening(selectedLanguage.code)
                        }
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Right action: Direct Send Message
                Button(
                    onClick = {
                        val fullText = if (partialText.isNotBlank()) {
                            if (composedText.isBlank()) partialText else "$composedText $partialText"
                        } else composedText

                        if (fullText.isNotBlank()) {
                            onSendMessage(fullText)
                            speechHelper.stopListening()
                            onDismiss()
                        }
                    },
                    enabled = composedText.isNotBlank() || partialText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("send_voice_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun PunctuationChip(
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(
                1.dp,
                if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                RoundedCornerShape(8.dp)
            )
            .testTag("punctuation_chip_$label"),
        color = if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun PulsingMicButton(
    isListening: Boolean,
    rms: Float,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.22f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val dynamicScale by animateFloatAsState(
        targetValue = if (isListening) 1f + (rms * 0.35f) else 1f,
        label = "dynamic_rms_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(64.dp)
    ) {
        // Outer pulsing ring when listening
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .scale(pulseScale * dynamicScale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            )
        }

        // Core Button
        Surface(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .clickable { onClick() }
                .testTag("voice_main_mic_action_button"),
            color = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            tonalElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop Recording" else "Start Recording",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun AudioWaveformDisplay(
    isListening: Boolean,
    rms: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val animatedRms by animateFloatAsState(
        targetValue = if (isListening) rms.coerceIn(0.12f, 1f) else 0.05f,
        animationSpec = tween(120),
        label = "rms_anim"
    )

    Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
        val barCount = 28
        val spacing = size.width / (barCount + 1)
        val maxBarHeight = size.height * 0.85f
        val minBarHeight = size.height * 0.12f

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount
            val sinFactor = if (isListening) {
                (sin(progress * 4f * Math.PI + wavePhase).toFloat() + 1f) / 2f
            } else {
                0.2f
            }

            val dynamicHeight = (minBarHeight + (maxBarHeight - minBarHeight) * animatedRms * (0.4f + 0.6f * sinFactor))
                .coerceIn(minBarHeight, maxBarHeight)

            val x = spacing * (i + 1)
            val y = (size.height - dynamicHeight) / 2f
            val barWidth = spacing * 0.55f

            val brush = Brush.verticalGradient(
                colors = listOf(primaryColor, secondaryColor),
                startY = y,
                endY = y + dynamicHeight
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x - barWidth / 2f, y),
                size = Size(barWidth, dynamicHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
