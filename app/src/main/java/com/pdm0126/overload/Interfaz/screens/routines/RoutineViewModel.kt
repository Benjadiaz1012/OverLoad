package com.pdm0126.overload.Interfaz.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.repository.RoutineRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RoutinesUiState(
    val isLoading: Boolean = true,
    val savedMicrocycles: List<RoutineMicrocycle> = emptyList(),
    val activeMicrocycleId: Long? = null,
    val isWorkoutSessionActive: Boolean = false
)

class RoutinesViewModel(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val uiState: StateFlow<RoutinesUiState> = combine(
        routineRepository.getAllMicrocycles(),
        workoutRepository.getActiveSession()
    ) { microcycles, activeSession ->
        val activeId = microcycles.find { it.isActive }?.microcycleId
        val sortedMicrocycles = microcycles.sortedByDescending { it.isActive }

        RoutinesUiState(
            isLoading = false,
            savedMicrocycles = sortedMicrocycles,
            activeMicrocycleId = activeId,
            isWorkoutSessionActive = activeSession != null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RoutinesUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                RoutinesViewModel(
                    routineRepository = app.overloadProvider.provideRoutineRepository(),
                    workoutRepository = app.overloadProvider.provideWorkoutRepository()
                )
            }
        }
    }
}