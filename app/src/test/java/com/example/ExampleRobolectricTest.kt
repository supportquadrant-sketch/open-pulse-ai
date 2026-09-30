package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PersonaEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OpenPulse AI", appName)
    }

    @Test
    fun `test conversation and message insertion`() = runBlocking {
        val convId = database.conversationDao().insertConversation(
            ConversationEntity(title = "Architecture Review", modelId = "openpulse-3.5-flash")
        )
        assertTrue(convId > 0)

        val msgId = database.messageDao().insertMessage(
            MessageEntity(
                conversationId = convId,
                role = "user",
                content = "How do I optimize Room queries?",
                tokenUsage = 8
            )
        )
        assertTrue(msgId > 0)

        val messages = database.messageDao().getMessagesListForConversation(convId)
        assertEquals(1, messages.size)
        assertEquals("How do I optimize Room queries?", messages[0].content)
    }

    @Test
    fun `test database seeding`() = runBlocking {
        AppDatabase.seedInitialData(database)
        val personaCount = database.personaDao().getPersonaCount()
        assertTrue(personaCount >= 5)

        val promptCount = database.promptDao().getPromptCount()
        assertTrue(promptCount >= 6)
    }

    @Test
    fun `test export conversation as JSON and Text`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val chatRepo = com.example.data.repository.ChatRepository(
            database.conversationDao(),
            database.messageDao()
        )
        val personaRepo = com.example.data.repository.PersonaRepository(database.personaDao())
        val settingsRepo = com.example.data.repository.SettingsRepository(context)
        val viewModel = com.example.viewmodel.ChatViewModel(chatRepo, personaRepo, settingsRepo)

        val conv = ConversationEntity(
            id = 42,
            title = "Quantum Physics Exploration",
            modelId = "gemini-2.5-flash",
            personaName = "Albert Einstein"
        )
        val messages = listOf(
            MessageEntity(
                id = 1,
                conversationId = 42,
                role = "user",
                content = "Explain quantum entanglement.",
                tokenUsage = 10,
                timestamp = 1727622600000L
            ),
            MessageEntity(
                id = 2,
                conversationId = 42,
                role = "assistant",
                content = "Spooky action at a distance occurs when particles interact...",
                tokenUsage = 45,
                latencyMs = 320L,
                modelUsed = "gemini-2.5-flash",
                timestamp = 1727622602000L
            )
        )

        // 1. Verify JSON export formatting
        val jsonOutput = viewModel.formatConversationExportJson(messages, conv)
        assertNotNull(jsonOutput)
        val root = org.json.JSONObject(jsonOutput)
        assertEquals("1.0", root.getString("exportVersion"))
        assertTrue(root.has("exportedAt"))

        val convObj = root.getJSONObject("conversation")
        assertEquals(42L, convObj.getLong("id"))
        assertEquals("Quantum Physics Exploration", convObj.getString("title"))
        assertEquals("gemini-2.5-flash", convObj.getString("modelId"))
        assertEquals("Albert Einstein", convObj.getString("personaName"))

        val msgsArray = root.getJSONArray("messages")
        assertEquals(2, msgsArray.length())
        assertEquals("user", msgsArray.getJSONObject(0).getString("role"))
        assertEquals("Explain quantum entanglement.", msgsArray.getJSONObject(0).getString("content"))
        assertEquals("assistant", msgsArray.getJSONObject(1).getString("role"))
        assertEquals("gemini-2.5-flash", msgsArray.getJSONObject(1).getString("modelUsed"))
        assertEquals(45, msgsArray.getJSONObject(1).getInt("tokenUsage"))

        // 2. Verify Text export formatting
        val textOutput = viewModel.formatConversationExportText(messages, conv)
        assertNotNull(textOutput)
        assertTrue(textOutput.contains("OpenPulse AI Chat Export"))
        assertTrue(textOutput.contains("Quantum Physics Exploration"))
        assertTrue(textOutput.contains("Albert Einstein"))
        assertTrue(textOutput.contains("Explain quantum entanglement."))
        assertTrue(textOutput.contains("Spooky action at a distance"))

        // 3. Verify filename generation
        val jsonFileName = viewModel.getExportFileName(conv, "json")
        assertTrue(jsonFileName.startsWith("openpulse_quantum_physics_"))
        assertTrue(jsonFileName.endsWith(".json"))

        val txtFileName = viewModel.getExportFileName(conv, "txt")
        assertTrue(txtFileName.startsWith("openpulse_quantum_physics_"))
        assertTrue(txtFileName.endsWith(".txt"))
    }
}

