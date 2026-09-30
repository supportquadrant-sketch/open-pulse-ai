package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PersonaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonaDao {
    @Query("SELECT * FROM personas ORDER BY isDefault DESC, name ASC")
    fun getAllPersonas(): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM personas WHERE id = :id LIMIT 1")
    suspend fun getPersonaById(id: Long): PersonaEntity?

    @Query("SELECT * FROM personas WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultPersona(): PersonaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: PersonaEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPersonas(personas: List<PersonaEntity>)

    @Update
    suspend fun updatePersona(persona: PersonaEntity)

    @Query("DELETE FROM personas WHERE id = :id AND isDefault = 0")
    suspend fun deletePersonaById(id: Long)

    @Query("SELECT COUNT(*) FROM personas")
    suspend fun getPersonaCount(): Int
}
