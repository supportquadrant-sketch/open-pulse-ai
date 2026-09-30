package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

enum class SpeechRecognitionState {
    IDLE,
    INITIALIZING,
    LISTENING,
    PROCESSING,
    ERROR
}

data class SpeechLanguage(
    val code: String,
    val displayName: String
)

object SupportedLanguages {
    val list = listOf(
        SpeechLanguage(Locale.getDefault().toLanguageTag(), "Auto (${Locale.getDefault().displayLanguage})"),
        SpeechLanguage("en-US", "English (US)"),
        SpeechLanguage("en-GB", "English (UK)"),
        SpeechLanguage("es-ES", "Spanish"),
        SpeechLanguage("fr-FR", "French"),
        SpeechLanguage("de-DE", "German"),
        SpeechLanguage("it-IT", "Italian"),
        SpeechLanguage("pt-BR", "Portuguese"),
        SpeechLanguage("zh-CN", "Chinese (Mandarin)"),
        SpeechLanguage("ja-JP", "Japanese"),
        SpeechLanguage("ko-KR", "Korean"),
        SpeechLanguage("hi-IN", "Hindi"),
        SpeechLanguage("ar-SA", "Arabic")
    )
}

class SpeechRecognitionHelper(
    private val context: Context,
    private val onStateChanged: (SpeechRecognitionState) -> Unit,
    private val onPartialTranscript: (String) -> Unit,
    private val onFinalTranscript: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onRmsChanged: (Float) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isCurrentlyListening = false

    companion object {
        fun isRecognitionAvailable(context: Context): Boolean {
            return SpeechRecognizer.isRecognitionAvailable(context)
        }
    }

    fun startListening(languageCode: String = Locale.getDefault().toLanguageTag()) {
        try {
            stopListening()

            if (!isRecognitionAvailable(context)) {
                onStateChanged(SpeechRecognitionState.ERROR)
                onError("Speech recognition is not supported on this device.")
                return
            }

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            onStateChanged(SpeechRecognitionState.INITIALIZING)
            speechRecognizer?.startListening(intent)
            isCurrentlyListening = true
        } catch (e: Exception) {
            Log.e("SpeechRecognitionHelper", "Error starting speech recognition", e)
            onStateChanged(SpeechRecognitionState.ERROR)
            onError("Unable to initialize microphone: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        if (isCurrentlyListening || speechRecognizer != null) {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.w("SpeechRecognitionHelper", "Error stopping speech recognizer", e)
            } finally {
                speechRecognizer = null
                isCurrentlyListening = false
                onStateChanged(SpeechRecognitionState.IDLE)
                onRmsChanged(0f)
            }
        }
    }

    fun destroy() {
        stopListening()
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            onStateChanged(SpeechRecognitionState.LISTENING)
        }

        override fun onBeginningOfSpeech() {
            onStateChanged(SpeechRecognitionState.LISTENING)
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Normalize -2dB..10dB to 0..1 range
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            onRmsChanged(normalized)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            onStateChanged(SpeechRecognitionState.PROCESSING)
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client speech recognition error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout while listening"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap the mic to try again."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                SpeechRecognizer.ERROR_SERVER -> "Server recognition error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap mic to restart."
                else -> "Speech recognition error ($error)"
            }

            // For common timeouts/no-match, handle smoothly without jarring errors
            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                onStateChanged(SpeechRecognitionState.IDLE)
            } else {
                onStateChanged(SpeechRecognitionState.ERROR)
                onError(message)
            }
            isCurrentlyListening = false
            onRmsChanged(0f)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()?.trim()
            if (!recognizedText.isNullOrEmpty()) {
                onFinalTranscript(recognizedText)
            }
            onStateChanged(SpeechRecognitionState.IDLE)
            isCurrentlyListening = false
            onRmsChanged(0f)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialText = matches?.firstOrNull()?.trim()
            if (!partialText.isNullOrEmpty()) {
                onPartialTranscript(partialText)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
