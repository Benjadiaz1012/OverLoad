package com.pdm0126.overload.ui.screens.routines.editor

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
            val nextOrder = (currentDay?.slots?.maxOfOrNull { it.order } ?: 0) + 1

            routineRepository.addExerciseSlot(
                dayId = dayId,
                exerciseId = exerciseId,
                order = nextOrder,
                targetSets = 3
            )
        }
    }

    fun updateTargetSets(slotId: Long, currentSets: Int, change: Int) {
        val newSets = currentSets + change
        if (newSets in 1..10) {
            viewModelScope.launch {
                routineRepository.updateSlotTargetSets(slotId, newSets)
            }
        }
    }

    fun removeSlot(slotId: Long) {
        viewModelScope.launch { routineRepository.removeExerciseSlot(slotId) }
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

