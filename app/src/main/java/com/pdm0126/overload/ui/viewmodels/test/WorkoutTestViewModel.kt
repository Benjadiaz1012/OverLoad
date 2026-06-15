package com.pdm0126.overload.ui.viewmodels.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.WorkoutSet
import com.pdm0126.overload.domain.repository.RoutineRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WorkoutTestViewModel(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    // Estado interno
    private val _uiState = MutableStateFlow<WorkoutTestUiState>(WorkoutTestUiState.Loading)
    val uiState: StateFlow<WorkoutTestUiState> = _uiState.asStateFlow()

    // Para la pantalla de prueba usamos siempre el primer día (índice 0)
    // En la UI real, este índice vendrá del cálculo de progresión del microciclo
    private val testDayIndex = 0

    init {
        observeState()
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                routineRepository.getActiveMicrocycle(),
                workoutRepository.getActiveSession()
            ) { microcycle, activeSession ->

                if (microcycle == null) {
                    return@combine WorkoutTestUiState.NoRoutine
                }

                val day = microcycle.days.getOrNull(testDayIndex)
                    ?: return@combine WorkoutTestUiState.Error("El microciclo no tiene días configurados.")

                if (activeSession == null) {
                    // Modo Dashboard: cargamos referencia histórica de cada slot
                    val lastSets = day.slots.associate { slot ->
                        slot.slotId to workoutRepository.getLastSetsForSlot(
                            slotId = slot.slotId,
                            dayId = day.dayId,
                            targetSets = slot.targetSets
                        )
                    }
                    WorkoutTestUiState.Ready(
                        microcycle = microcycle,
                        currentDayIndex = testDayIndex,
                        lastSets = lastSets
                    )
                } else {
                    // Modo Sesión Activa: necesitamos las series en vivo de cada slot
                    val setsBySlot = day.slots.associate { slot ->
                        slot.slotId to workoutRepository
                            .getSetsBySlotAndSession(slot.slotId, activeSession.sessionId)
                            .first() // snapshot puntual; el Flow individual se lee en la UI por slot
                    }
                    val lastSets = day.slots.associate { slot ->
                        slot.slotId to workoutRepository.getLastSetsForSlot(
                            slotId = slot.slotId,
                            dayId = day.dayId,
                            targetSets = slot.targetSets
                        )
                    }
                    WorkoutTestUiState.SessionActive(
                        microcycle = microcycle,
                        currentDayIndex = testDayIndex,
                        session = activeSession,
                        setsBySlot = setsBySlot,
                        lastSets = lastSets
                    )
                }
            }
            .catch { emit(WorkoutTestUiState.Error(it.message ?: "Error desconocido")) }
            .collect { _uiState.value = it }
        }
    }

    // Intenciones del usuario

    // Inicia una sesión de entrenamiento para el día actual
    fun startSession(dayId: Long) {
        viewModelScope.launch {
            try {
                workoutRepository.startSession(dayId)
            } catch (e: Exception) {
                _uiState.value = WorkoutTestUiState.Error("No se pudo iniciar la sesión: ${e.message}")
            }
        }
    }

    // Finaliza la sesión activa y guarda el endTimestamp
    fun endSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                workoutRepository.endSession(sessionId)
            } catch (e: Exception) {
                _uiState.value = WorkoutTestUiState.Error("No se pudo finalizar la sesión: ${e.message}")
            }
        }
    }

     // Registra una serie para un slot específico.
     // El ViewModel valida entradas básicas; el repositorio aplica la lógica de rirFactor.
    fun logSet(
        sessionId: Long,
        slotId: Long,
        exerciseId: String,
        currentSetCount: Int,
        weightKg: Float,
        reps: Int,
        rir: Int?,
        isRirEnabled: Boolean
    ) {
        // Validación de límites antes de delegar al repositorio
        if (weightKg <= 0f || reps <= 0) return

        viewModelScope.launch {
            try {
                workoutRepository.logSet(
                    sessionId = sessionId,
                    slotId = slotId,
                    exerciseId = exerciseId,
                    setNumber = currentSetCount + 1,
                    weightKg = weightKg,
                    reps = reps,
                    rir = rir,
                    isRirEnabled = isRirEnabled
                )
                // El combine() de observeState() detecta el cambio automáticamente via Flow
            } catch (e: Exception) {
                _uiState.value = WorkoutTestUiState.Error("Error al guardar la serie: ${e.message}")
            }
        }
    }

    // Elimina una serie (corrección de error del usuario)
    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            workoutRepository.deleteSet(setId)
        }
    }

    // Inyección de dependencias

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as OverloadApplication)
                WorkoutTestViewModel(
                    routineRepository = application.overloadProvider.provideRoutineRepository(),
                    workoutRepository = application.overloadProvider.provideWorkoutRepository()
                )
            }
        }
    }
}
