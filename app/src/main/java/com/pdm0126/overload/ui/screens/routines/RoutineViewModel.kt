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

    fun startCreating() {
        _uiState.update { it.copy(isCreating = true) }
    }

    fun cancelCreating() {
        _uiState.update { it.copy(isCreating = false) }
    }

    fun createMicrocycleFromBlueprint(blueprint: Blueprint) {
        viewModelScope.launch {
            val state = _uiState.value
            val isFirst = state.savedMicrocycles.isEmpty()

            // Creamos usando el nombre base del Blueprint
            val newMicrocycleId = routineRepository.createMicrocycle(
                name = "Nuevo: ${blueprint.name}",
                blueprintType = blueprint.name,
                isActive = isFirst
            )

            // Insertamos los días predeterminados directamente
            blueprint.defaultDays.forEachIndexed { index, dayName ->
                routineRepository.addDayToMicrocycle(
                    microcycleId = newMicrocycleId,
                    order = index + 1,
                    focus = dayName
                )
            }

            // Volvemos a la lista automáticamente
            cancelCreating()
        }
    }

    fun addDayToSavedMicrocycle(microcycleId: Long) {
        val microcycle = _uiState.value.savedMicrocycles.find { it.microcycleId == microcycleId } ?: return
        if (microcycle.days.size >= 9) return

        viewModelScope.launch {
            val nextOrder = (microcycle.days.maxOfOrNull { it.order } ?: 0) + 1
            routineRepository.addDayToMicrocycle(
                microcycleId = microcycleId,
                order = nextOrder,
                focus = "Día $nextOrder"
            )
        }
    }

    fun deleteDayFromSavedMicrocycle(dayId: Long) {
        viewModelScope.launch {
            routineRepository.deleteDay(dayId)
        }
    }

    fun updateMicrocycleName(microcycleId: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            routineRepository.updateMicrocycleName(microcycleId, newName.trim())
        }
    }

    fun setActiveMicrocycle(microcycleId: Long) {
        viewModelScope.launch {
            routineRepository.updateActiveMicrocycle(microcycleId)
        }
    }

    fun deleteMicrocycle(microcycleId: Long) {
        viewModelScope.launch {
            routineRepository.deleteMicrocycle(microcycleId)
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

