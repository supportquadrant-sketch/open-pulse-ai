package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personas")
data class PersonaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val avatarIcon: String = "smart_toy", // icon key identifier
    val systemPrompt: String,
    val defaultTemperature: Float = 0.7f,
    val category: String = "General",
    val isDefault: Boolean = false,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
