package com.pdm0126.overload.ui.screens.routineEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.repository.RoutineRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoutineEditorUiState(
    val isLoading: Boolean = true,
    val microcycle: RoutineMicrocycle? = null,
    val isWorkoutSessionActive: Boolean = false
)

class RoutineEditorViewModel(
    private val microcycleId: Long,
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineEditorUiState())
    val uiState: StateFlow<RoutineEditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                routineRepository.getMicrocycleById(microcycleId),
                workoutRepository.getActiveSession()
            ) { microcycle, activeSession ->
                RoutineEditorUiState(
                    isLoading = false,
                    microcycle = microcycle,
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
            routineRepository.updateMicrocycleName(microcycleId, newName.trim())
        }
    }

    fun setActive() {
        viewModelScope.launch {
            routineRepository.updateActiveMicrocycle(microcycleId)
        }
    }

    fun deleteRoutine() {
        viewModelScope.launch {
            routineRepository.deleteMicrocycle(microcycleId)
        }
    }

    fun addDay() {
        val currentMicrocycle = _uiState.value.microcycle ?: return
        if (currentMicrocycle.days.size >= 9) return

        viewModelScope.launch {
            val nextOrder = (currentMicrocycle.days.maxOfOrNull { it.order } ?: 0) + 1
            routineRepository.addDayToMicrocycle(
                microcycleId = microcycleId,
                order = nextOrder,
                focus = "Día $nextOrder"
            )
        }
    }

    fun deleteDay(dayId: Long) {
        viewModelScope.launch {
            routineRepository.deleteDay(dayId)
        }
    }

    companion object {
        fun provideFactory(microcycleId: Long) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                RoutineEditorViewModel(
                    microcycleId = microcycleId,
                    routineRepository = app.overloadProvider.provideRoutineRepository(),
                    workoutRepository = app.overloadProvider.provideWorkoutRepository()
                )
            }
        }
    }
}