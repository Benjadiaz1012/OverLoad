package com.pdm0126.overload.Interfaz.screens.detail

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

data class DetailUiState(
    val isLoading: Boolean = true,
    val exercise: Exercise? = null
)

class DetailViewModel(
    private val exerciseId: String,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExerciseById(exerciseId)
            _uiState.value = DetailUiState(isLoading = false, exercise = exercise)
        }
    }

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
}