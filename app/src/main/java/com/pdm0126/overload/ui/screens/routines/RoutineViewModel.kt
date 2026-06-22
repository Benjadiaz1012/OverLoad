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
                _uiState.update { it.copy(
                    savedMicrocycles = microcycles,
                    activeMicrocycleId = activeId
                ) }
            }
        }
    }

    // Funciones del borrador en memoria

    fun startCreating() {
        _uiState.update { state ->
            state.copy(isCreating = true, draftName = "", selectedBlueprint = null, draftDays = emptyList())
        }
    }

    fun cancelCreating() {
        _uiState.update { state ->
            state.copy(isCreating = false)
        } // Se desecha el borrador al instante
    }

    fun updateDraftName(name: String) {
        _uiState.update { state ->
            state.copy(draftName = name)
        }
    }

    fun selectBlueprint(blueprint: Blueprint) {
        val initialDays = blueprint.defaultDays.map { DraftDay(focus = it) }
        _uiState.update {  state ->
            state.copy(
            selectedBlueprint = blueprint,
            draftDays = initialDays
        ) }
    }

    fun addDraftDay(focus: String = "Nuevo Día") {
        val currentDays = _uiState.value.draftDays
        if (currentDays.size < 9) { // Límite funcional definido en la arquitectura
            _uiState.update { state ->
                state.copy(
                    draftDays = currentDays + DraftDay(focus = "Día ${currentDays.size + 1}") // Mas genérico
                )
            }
        }
    }

    fun removeDraftDay(tempId: String) {
        _uiState.update { state ->
            state.copy(draftDays = state.draftDays.filterNot { it.tempId == tempId })
        }
    }

    fun updateDraftDayFocus(tempId: String, newFocus: String) {
        _uiState.update { state ->
            state.copy(draftDays = state.draftDays.map {
                if (it.tempId == tempId) it.copy(focus = newFocus) else it
            })
        }
    }

    // Guardar en la base de datos
    fun saveDraftToDatabase() {
        val state = _uiState.value
        if (state.draftName.isBlank() || state.draftDays.isEmpty()) return

        viewModelScope.launch {
            val isFirst = state.savedMicrocycles.isEmpty()
            val blueprintName = state.selectedBlueprint?.name ?: "Personalizado"

            val newMicrocycleId = routineRepository.createMicrocycle(
                name = state.draftName,
                blueprintType = blueprintName,
                isActive = isFirst
            )
            state.draftDays.forEachIndexed { index, draftDay ->
                routineRepository.addDayToMicrocycle(
                    microcycleId = newMicrocycleId,
                    order = index + 1,
                    focus = draftDay.focus
                )
            }

            // Limpiamos y salimos del modo creación
            cancelCreating()
        }
    }

    fun addDayToSavedMicrocycle(microcycleId: Long) {
        // Buscamos el microciclo actual en la RAM
        val microcycle = _uiState.value.savedMicrocycles.find { it.microcycleId == microcycleId } ?: return
        if (microcycle.days.size >= 9) return // Respetamos el límite de 9 días

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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                RoutineViewModel(app.overloadProvider.provideRoutineRepository())
            }
        }
    }
}

