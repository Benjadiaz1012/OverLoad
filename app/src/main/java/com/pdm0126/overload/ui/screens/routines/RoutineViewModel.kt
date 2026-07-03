package com.pdm0126.overload.ui.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.Blueprint
import com.pdm0126.overload.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RoutineViewModel(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutinesUiState())
    val uiState: StateFlow<RoutinesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            routineRepository.getAllMicrocycles().collect { microcycles ->
                val activeId = microcycles.find { it.isActive }?.microcycleId
                val sortedMicrocycles = microcycles.sortedByDescending { it.isActive }
                _uiState.update { it.copy(
                    savedMicrocycles = sortedMicrocycles,
                    activeMicrocycleId = activeId
                ) }
            }
        }
    }
    fun createMicrocycleFromBlueprint(blueprint: Blueprint) {
        viewModelScope.launch {
            val state = _uiState.value
            val isFirst = state.savedMicrocycles.isEmpty()

            val newMicrocycleId = routineRepository.createMicrocycle(
                name = "Nuevo: ${blueprint.name}",
                blueprintType = blueprint.name,
                isActive = isFirst
            )
            blueprint.defaultDays.forEachIndexed { index, dayName ->
                routineRepository.addDayToMicrocycle(
                    microcycleId = newMicrocycleId,
                    order = index + 1,
                    focus = dayName
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                RoutineViewModel(app.overloadProvider.provideRoutineRepository())
            }
        }
    }
}

