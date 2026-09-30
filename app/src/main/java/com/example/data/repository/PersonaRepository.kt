package com.example.data.repository

import com.example.data.local.dao.PersonaDao
import com.example.data.local.entity.PersonaEntity
import kotlinx.coroutines.flow.Flow

class PersonaRepository(private val personaDao: PersonaDao) {
    val allPersonas: Flow<List<PersonaEntity>> = personaDao.getAllPersonas()

    suspend fun getPersonaById(id: Long): PersonaEntity? = personaDao.getPersonaById(id)

    suspend fun getDefaultPersona(): PersonaEntity? = personaDao.getDefaultPersona()

    suspend fun insertPersona(persona: PersonaEntity): Long = personaDao.insertPersona(persona)

    suspend fun updatePersona(persona: PersonaEntity) = personaDao.updatePersona(persona)

    suspend fun deletePersona(id: Long) = personaDao.deletePersonaById(id)
}
