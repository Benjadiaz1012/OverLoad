package com.pdm0126.overload.ui.screens.detail

import androidx.lifecycle.ViewModel
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

class DetailViewModel(
    private val exerciseId: String,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadExercise()
    }

    private fun loadExercise() {
        viewModelScope.launch {
            _uiState.value = DetailUiState(isLoading = true)
            try {
                // Buscamos en la base de datos local
                var exercise = exerciseRepository.getExerciseById(exerciseId)
                val isBookmarked = exercise != null
                // Si el usuario tocó un ejercicio de la pestaña "Explorar" que aún no guarda,
                // getExerciseById devolverá null porque no está en Room.
                // Entonces, lo buscamos en la lista que ya trajimos de internet (caché).
                if (exercise == null) {
                    val remoteCache = exerciseRepository.getRemoteExercises("").getOrNull()
                    exercise = remoteCache?.find { it.id == exerciseId }
                }
                if (exercise != null) { // Si lo encontramos, lo mostramos
                    _uiState.value = DetailUiState(
                        isLoading = false,
                        exercise = exercise,
                        isBookmarked = isBookmarked
                    )
                } else {
                    _uiState.value = DetailUiState(
                        isLoading = false, errorMessage = "Ejercicio no encontrado"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState(
                    isLoading = false,
                    errorMessage = "Error al cargar los detalles"
                )
            }
        }
    }

    fun toggleBookmark() {
        val exercise = uiState.value.exercise ?: return
        viewModelScope.launch {
            if (uiState.value.isBookmarked) {
                exerciseRepository.deleteLocalExercise(exercise)
                _uiState.value = uiState.value.copy(isBookmarked = false)
            } else {
                exerciseRepository.saveRemoteExerciseToLocal(exercise)
                _uiState.value = uiState.value.copy(isBookmarked = true)
            }
        }
    }


    // Método alvarito
    companion object {
        fun provideFactory(exerciseId: String) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                DetailViewModel(
                    exerciseId = exerciseId,
                    exerciseRepository = app.overloadProvider.provideExerciseRepository()
                )
            }
        }
    }

    /*@Suppress("UNCHECKED_CAST")
    class Factory(
        private val exerciseId: String,
        private val application: OverloadApplication
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailViewModel(
                exerciseId = exerciseId,
                exerciseRepository = application.overloadProvider.provideExerciseRepository()
            ) as T
        }
    }*/
}