package com.pdm0126.overload.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.repository.RoutineRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Observamos combinadamente la rutina activa Y la sesión activa
        viewModelScope.launch {
            combine(
                routineRepository.getActiveMicrocycle(),
                workoutRepository.getActiveSession()
            ) { microcycle, session ->
                DashboardUiState(
                    isLoading = false,
                    activeMicrocycle = microcycle,
                    activeSession = session
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun startWorkout(dayId: Long) {
        viewModelScope.launch {
            workoutRepository.startSession(dayId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                DashboardViewModel(
                    routineRepository = app.overloadProvider.provideRoutineRepository(),
                    workoutRepository = app.overloadProvider.provideWorkoutRepository()
                )
            }
        }
    }
}