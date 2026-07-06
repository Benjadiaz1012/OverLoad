package com.pdm0126.overload.Interfaz.screens.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.Blueprint
import com.pdm0126.overload.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SystemUiState(
    val isCreating: Boolean = false,
    val error: String? = null,
    val createdMicrocycleId: Long? = null
)

class SystemViewModel(
    private val routineRepository: RoutineRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SystemUiState())
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()
    fun createMicrocycleFromBlueprint(blueprint: Blueprint) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }

            try {
                val newMicrocycleId = routineRepository.createMicrocycle(
                    name = blueprint.name,
                    blueprintType = blueprint.id,
                    isActive = true
                )
                blueprint.defaultDays.forEachIndexed { index, dayName ->
                    routineRepository.addDayToMicrocycle(
                        microcycleId = newMicrocycleId,
                        order = index + 1,
                        focus = dayName
                    )
                }
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        createdMicrocycleId = newMicrocycleId
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        error = e.message ?: "Error al crear la rutina"
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as OverloadApplication
                SystemViewModel(
                    routineRepository = app.overloadProvider.provideRoutineRepository()
                )
            }
        }
    }
}