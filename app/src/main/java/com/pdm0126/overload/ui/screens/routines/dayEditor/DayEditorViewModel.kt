package com.pdm0126.overload.ui.screens.routines.dayEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DayEditorUiState(
    val isLoading: Boolean = true,
    val day: RoutineDay? = null
)

class DayEditorViewModel(
    private val dayId: Long,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DayEditorUiState())
    val uiState: StateFlow<DayEditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            routineRepository.getRoutineDay(dayId).collect { routineDay ->
                _uiState.update { it.copy(isLoading = false, day = routineDay) }
            }
        }
    }

    fun addExerciseToSlot(exerciseId: String) {
        viewModelScope.launch {
            val currentDay = routineRepository.getRoutineDay(dayId).firstOrNull()
            val nextOrder = (currentDay?.plannedExercises?.maxOfOrNull { it.order } ?: 0) + 1

            routineRepository.addPlannedExercise(
                dayId = dayId,
                exerciseId = exerciseId,
                order = nextOrder,
                targetSets = 3,
                targetReps = null
            )
        }
    }

    fun updateTargetSets(slotId: Long, newSets: Int) {
        if (newSets in 1..10) {
            viewModelScope.launch {
                routineRepository.updateTargetSets(slotId, newSets)
            }
        }
    }

    fun updateTargetReps(slotId: Long, newReps: Int?) {
        if (newReps == null || newReps in 1..20) {
            viewModelScope.launch {
                routineRepository.updateTargetReps(slotId, newReps)
            }
        }
    }

    fun removePlannedExercise(plannedExerciseId: Long?) {
        viewModelScope.launch { routineRepository.removePlannedExercise(plannedExerciseId) }
    }

    fun updateDayName(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            routineRepository.updateDayFocus(dayId, newName.trim())
        }
    }

    fun deleteDay() {
        viewModelScope.launch {
            routineRepository.deleteRoutineDay(dayId)
        }
    }

    companion object {
        fun provideFactory(dayId: Long) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                DayEditorViewModel(dayId, app.overloadProvider.provideRoutineRepository())
            }
        }
    }
}