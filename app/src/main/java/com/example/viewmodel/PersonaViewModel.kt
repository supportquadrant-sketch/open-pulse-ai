package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.PersonaEntity
import com.example.data.repository.PersonaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PersonaUiState(
    val editingPersona: PersonaEntity? = null,
    val showCreateDialog: Boolean = false
)

class PersonaViewModel(
    private val personaRepository: PersonaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonaUiState())
    val uiState: StateFlow<PersonaUiState> = _uiState.asStateFlow()

    val personas: StateFlow<List<PersonaEntity>> = personaRepository.allPersonas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPersona(
        name: String,
        description: String,
        systemPrompt: String,
        category: String = "Custom",
        temperature: Float = 0.7f,
        avatarIcon: String = "smart_toy"
    ) {
        viewModelScope.launch {
            val entity = PersonaEntity(
                name = name,
                description = description,
                systemPrompt = systemPrompt,
                category = category,
                defaultTemperature = temperature,
                avatarIcon = avatarIcon,
                isDefault = false,
                isCustom = true
            )
            personaRepository.insertPersona(entity)
            _uiState.value = _uiState.value.copy(showCreateDialog = false)
        }
    }

    fun updatePersona(persona: PersonaEntity) {
        viewModelScope.launch {
            personaRepository.updatePersona(persona)
            _uiState.value = _uiState.value.copy(editingPersona = null)
        }
    }

    fun deletePersona(id: Long) {
        viewModelScope.launch {
            personaRepository.deletePersona(id)
        }
    }

    fun toggleCreateDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateDialog = show)
    }

    fun setEditingPersona(persona: PersonaEntity?) {
        _uiState.value = _uiState.value.copy(editingPersona = persona)
    }
}

class PersonaViewModelFactory(
    private val personaRepository: PersonaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PersonaViewModel(personaRepository) as T
    }
}
