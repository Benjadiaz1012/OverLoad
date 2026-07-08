package com.pdm0126.overload.ui.screens.routines.routineEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.Routine
import com.pdm0126.overload.domain.repository.RoutineRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoutineEditorUiState(
    val isLoading: Boolean = true,
    val routine: Routine? = null,
    val isWorkoutSessionActive: Boolean = false
)

class RoutineEditorViewModel(
    private val routineId: Long,
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineEditorUiState())
    val uiState: StateFlow<RoutineEditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                routineRepository.getRoutineById(routineId),
                workoutRepository.getActiveSession()
            ) { routine, activeSession ->
                RoutineEditorUiState(
                    isLoading = false,
                    routine = routine,
                    isWorkoutSessionActive = activeSession != null
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun renameRoutine(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            routineRepository.updateRoutineName(routineId, newName.trim())
        }
    }

    fun setActive() {
        viewModelScope.launch {
            routineRepository.updateActiveRoutine(routineId)
        }
    }

    fun deleteRoutine() {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routineId)
        }
    }

    fun addDay() {
        val currentMicrocycle = _uiState.value.routine ?: return
        if (currentMicrocycle.days.size >= 9) return

        viewModelScope.launch {
            val nextOrder = (currentMicrocycle.days.maxOfOrNull { it.order } ?: 0) + 1
            routineRepository.addDayToRoutine(
                routineId = routineId,
                order = nextOrder,
                focus = "Día $nextOrder"
            )
        }
    }

    fun deleteDay(dayId: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutineDay(dayId)
        }
    }

    companion object {
        fun provideFactory(microcycleId: Long) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                RoutineEditorViewModel(
                    routineId = microcycleId,
                    routineRepository = app.overloadProvider.provideRoutineRepository(),
                    workoutRepository = app.overloadProvider.provideWorkoutRepository()
                )
            }
        }
    }
}