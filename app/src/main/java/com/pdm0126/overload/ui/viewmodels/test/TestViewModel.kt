package com.pdm0126.overload.ui.viewmodels.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TestViewModel(
    private val repository: ExerciseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<TestUiState>(TestUiState.Loading)
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            repository.getAllExercises()
                .catch { error ->
                    _uiState.value = TestUiState.Error(error.message ?: "Error desconocido")
                }
                .collect { exerciseList ->
                    if (exerciseList.isEmpty()) {
                        _uiState.value = TestUiState.Loading
                    } else {
                        _uiState.value = TestUiState.Success(exerciseList)
                    }
                }
        }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY] as OverloadApplication)
                TestViewModel(application.exerciseRepository)
            }
        }
    }
}

