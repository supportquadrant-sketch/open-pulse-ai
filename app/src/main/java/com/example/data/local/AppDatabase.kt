package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AnalyticsDao
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.PersonaDao
import com.example.data.local.dao.PromptDao
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PersonaEntity
import com.example.data.local.entity.PromptTemplateEntity
import com.example.data.local.entity.TokenUsageSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        PersonaEntity::class,
        PromptTemplateEntity::class,
        TokenUsageSnapshot::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun personaDao(): PersonaDao
    abstract fun promptDao(): PromptDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "openpulse_ai.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val personaDao = database.personaDao()
            val promptDao = database.promptDao()

            if (personaDao.getPersonaCount() == 0) {
                val initialPersonas = listOf(
                    PersonaEntity(
                        name = "OpenPulse Default",
                        description = "Direct, insightful, and adaptable assistant with strong analytical reasoning.",
                        avatarIcon = "smart_toy",
                        systemPrompt = "You are OpenPulse AI, an intelligent, open-source AI assistant. Deliver clear, precise, and well-structured answers using clean Markdown formatting and concise explanations.",
                        defaultTemperature = 0.7f,
                        category = "General",
                        isDefault = true,
                        isCustom = false
                    ),
                    PersonaEntity(
                        name = "Senior Android Architect",
                        description = "Specialized in modern Kotlin, Jetpack Compose, Room DB, M3, and Clean Architecture.",
                        avatarIcon = "terminal",
                        systemPrompt = "You are an Elite Android Systems Architect. Provide idiomatic Kotlin code, Jetpack Compose best practices, Room database patterns, coroutines/Flow guidance, and architecture reviews.",
                        defaultTemperature = 0.2f,
                        category = "Development",
                        isDefault = false,
                        isCustom = false
                    ),
                    PersonaEntity(
                        name = "Cybersecurity Auditor",
                        description = "Focuses on threat modeling, vulnerability detection, and secure coding.",
                        avatarIcon = "security",
                        systemPrompt = "You are a Chief Information Security Officer & Cyber Auditor. Analyze code and architecture for OWASP top 10 vulnerabilities, insecure secret handling, injection flaws, and zero-trust policies.",
                        defaultTemperature = 0.3f,
                        category = "Security",
                        isDefault = false,
                        isCustom = false
                    ),
                    PersonaEntity(
                        name = "Technical Writer",
                        description = "Produces clear documentation, API reference specs, and architectural RFCs.",
                        avatarIcon = "menu_book",
                        systemPrompt = "You are a Principal Technical Writer. Format all responses with crisp documentation headers, diagrams, bulleted takeaways, and exact technical terminology.",
                        defaultTemperature = 0.5f,
                        category = "Writing",
                        isDefault = false,
                        isCustom = false
                    ),
                    PersonaEntity(
                        name = "Data & AI Scientist",
                        description = "Expert in machine learning models, statistical analysis, and algorithmic optimization.",
                        avatarIcon = "analytics",
                        systemPrompt = "You are a Lead AI Researcher & Data Scientist. Explain machine learning math, embedding architectures, neural network tuning, and statistical significance.",
                        defaultTemperature = 0.4f,
                        category = "Data",
                        isDefault = false,
                        isCustom = false
                    )
                )
                personaDao.insertPersonas(initialPersonas)
            }

            if (promptDao.getPromptCount() == 0) {
                val initialPrompts = listOf(
                    PromptTemplateEntity(
                        title = "Code Review & Refactor",
                        promptText = "Review the following code for performance bottlenecks, idiomatic patterns, memory leaks, and readability. Suggest concrete refactorings with code blocks:\n\n```kotlin\n\n```",
                        category = "Coding",
                        tags = "kotlin, clean-code, refactor"
                    ),
                    PromptTemplateEntity(
                        title = "Database Schema Design",
                        promptText = "Design a normalized database schema with foreign keys, indexes, and Room entity definitions for the following domain requirements:\n\n",
                        category = "Database",
                        tags = "room, sql, architecture"
                    ),
                    PromptTemplateEntity(
                        title = "Explain with Analogies",
                        promptText = "Explain the following complex concept using intuitive real-world analogies, step-by-step intuition, and a key summary takeaway:\n\n",
                        category = "Learning",
                        tags = "explanation, learning"
                    ),
                    PromptTemplateEntity(
                        title = "Debug Crash / Stack Trace",
                        promptText = "Analyze this Android crash stack trace, identify the root cause, explain why it happened, and provide the exact fix:\n\n```\n\n```",
                        category = "Debugging",
                        tags = "crash, bugfix, android"
                    ),
                    PromptTemplateEntity(
                        title = "Security Vulnerability Scan",
                        promptText = "Analyze this code snippet or API endpoint definition for potential security flaws (injection, improper state, auth bypass, data leakage) and suggest mitigation steps:\n\n",
                        category = "Security",
                        tags = "security, owasp, audit"
                    ),
                    PromptTemplateEntity(
                        title = "Draft Architectural RFC",
                        promptText = "Draft a structured Request for Comments (RFC) covering context, proposed design, alternatives considered, security implications, and rollout plan for:",
                        category = "Writing",
                        tags = "rfc, architecture, planning"
                    )
                )
                promptDao.insertPrompts(initialPrompts)
            }
        }
    }
}
