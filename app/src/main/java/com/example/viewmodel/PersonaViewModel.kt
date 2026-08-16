package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.AgentDao
import com.example.data.AgentPersona

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PersonaViewModel(private val agentDao: AgentDao) : ViewModel() {
    val personas: Flow<List<AgentPersona>> = agentDao.getAllPersonas()

    fun addPersona(persona: AgentPersona) {
        viewModelScope.launch {
            agentDao.insertPersona(persona)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return PersonaViewModel(
                    com.example.data.AppDatabase.getDatabase(application).agentDao()
                ) as T
            }
        }
    }
}
