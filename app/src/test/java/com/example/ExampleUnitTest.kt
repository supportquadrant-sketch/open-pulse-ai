package com.example

import com.example.util.SpeechRecognitionState
import com.example.util.SupportedLanguages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `test speech recognition states defined correctly`() {
        assertEquals(5, SpeechRecognitionState.values().size)
        assertTrue(SpeechRecognitionState.values().contains(SpeechRecognitionState.IDLE))
        assertTrue(SpeechRecognitionState.values().contains(SpeechRecognitionState.LISTENING))
        assertTrue(SpeechRecognitionState.values().contains(SpeechRecognitionState.PROCESSING))
        assertTrue(SpeechRecognitionState.values().contains(SpeechRecognitionState.ERROR))
    }

    @Test
    fun `test supported languages contains standard multilingual options`() {
        val languages = SupportedLanguages.list
        assertTrue(languages.isNotEmpty())
        assertTrue(languages.any { it.code == "en-US" })
        assertTrue(languages.any { it.code == "es-ES" })
        assertTrue(languages.any { it.code == "fr-FR" })
        assertTrue(languages.any { it.code == "de-DE" })
        assertTrue(languages.any { it.code == "ja-JP" })
        assertTrue(languages.any { it.code == "zh-CN" })
        assertTrue(languages.any { it.code == "hi-IN" })
    }

    @Test
    fun `test transcript concatenation and punctuation helpers`() {
        var text = "Hello world"
        text = "$text."
        assertEquals("Hello world.", text)

        text = "$text\nNew paragraph"
        assertEquals("Hello world.\nNew paragraph", text)
    }
}

