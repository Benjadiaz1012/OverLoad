package com.pdm0126.overload.ui.viewmodels.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchTestViewModel(
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchTestUiState>(SearchTestUiState.Idle)
    val uiState: StateFlow<SearchTestUiState> = _uiState.asStateFlow()

    // 1. Busca en internet
    fun search(query: String) {
        if (query.isBlank()) return

        _uiState.value = SearchTestUiState.Loading
        viewModelScope.launch {
            try {
                val results = repository.searchRemoteExercises(query)
                if (results.isEmpty()) {
                    _uiState.value = SearchTestUiState.Error("No se encontraron ejercicios.")
                } else {
                    _uiState.value = SearchTestUiState.Success(results)
                }
            } catch (e: Exception) {
                _uiState.value = SearchTestUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    // 2. Guarda el ejercicio seleccionado en la base de datos local
    fun saveExerciseToLocal(exercise: Exercise, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.saveRemoteExerciseToLocal(exercise)
                // Llamamos a un callback para avisarle a la UI que ya se guardó
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as OverloadApplication)
                SearchTestViewModel(application.exerciseRepository)
            }
        }
    }
}

