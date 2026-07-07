package com.pdm0126.overload.Interfaz.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.repository.ExerciseRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val exercise: Exercise? = null,
    val isBookmarked: Boolean = false,
    val errorMessage: String? = null,
    val isWorkoutSessionActive: Boolean = false
)

class DetailViewModel(
    private val exerciseId: String,
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadExercise()
        observeActiveSession()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            workoutRepository.getActiveSession().collect { session ->
                _uiState.value = _uiState.value.copy(isWorkoutSessionActive = session != null)
            }
        }
    }

    private fun loadExercise() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                var exercise = exerciseRepository.getExerciseById(exerciseId)
                val isBookmarked = exercise != null

                if (exercise == null) {
                    val remoteCache = exerciseRepository.getRemoteExercises("").getOrNull()
                    exercise = remoteCache?.find { it.id == exerciseId }
                }

                if (exercise != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        exercise = exercise,
                        isBookmarked = isBookmarked,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Ejercicio no encontrado"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
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

    companion object {
        fun provideFactory(exerciseId: String) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                DetailViewModel(
                    exerciseId = exerciseId,
                    exerciseRepository = app.overloadProvider.provideExerciseRepository(),
                    workoutRepository = app.overloadProvider.provideWorkoutRepository()
                )
            }
        }
    }
}