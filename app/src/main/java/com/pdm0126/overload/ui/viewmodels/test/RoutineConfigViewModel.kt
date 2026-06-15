package com.pdm0126.overload.ui.viewmodels.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pdm0126.overload.OverloadApplication
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutineConfigViewModel(
    private val repository: RoutineRepository
) : ViewModel() {

    // ESTADO REACTIVO (SINGLE SOURCE OF TRUTH)
    // Escucha directamente a Room. Cualquier INSERT o DELETE se refleja aquí automáticamente.
    val uiState: StateFlow<RoutineConfigUiState> = repository.getActiveMicrocycle()
        .map { microcycle ->
            if (microcycle == null) RoutineConfigUiState.Empty
            else RoutineConfigUiState.Active(microcycle)
        }
        .catch { emit(RoutineConfigUiState.Error(it.message ?: "Error al cargar la rutina")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RoutineConfigUiState.Loading
        )

    //  INTENCIONES DEL USUARIO (LÓGICA DE NEGOCIOS Y LÍMITES)

    // Inicia un nuevo bloque desde cero basado en una sugerencia (PPL, Arnold, etc.)
    fun startNewMicrocycle(name: String, blueprintType: String) {
        viewModelScope.launch {
            repository.createMicrocycle(name, blueprintType, isActive = true)
        }
    }

    // Agrega un día al microciclo actual (ej. "Push", "Pull")
    fun addDay(microcycle: RoutineMicrocycle, focus: String) {
        // Límite Estructural: Máximo 6 días
        if (microcycle.days.size >= 6) {
            // Aquí en el futuro se podría emitir un evento para mostrar un Toast en Compose
            return
        }

        viewModelScope.launch {
            // Calculamos el siguiente orden lógico (Día 1, Día 2, etc.)
            val nextOrder = (microcycle.days.maxOfOrNull { it.order } ?: 0) + 1
            repository.addDayToMicrocycle(microcycle.microcycleId, nextOrder, focus)
        }
    }

    // Asigna un ejercicio de la librería a un día específico
    fun addExerciseToDay(day: RoutineDay, exerciseId: String, targetSets: Int) {
        // Límite de fatiga: Máximo 12 ejercicios por día
        if (day.slots.size >= 12) {
            return
        }

        // Límite de volumen: Máximo 6 series por ejercicio
        val safeSets = targetSets.coerceIn(1, 6)

        viewModelScope.launch {
            val nextOrder = (day.slots.maxOfOrNull { it.order } ?: 0) + 1
            repository.addExerciseSlot(day.dayId, exerciseId, nextOrder, safeSets)
        }
    }

    // Permite al usuario corregir errores eliminando un slot
    fun removeExercise(slotId: Long) {
        viewModelScope.launch {
            repository.removeExerciseSlot(slotId)
        }
    }

    // Inyección de dependencias:
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as OverloadApplication)
                // Obtenemos el repositorio a través del Provider configurado
                RoutineConfigViewModel(application.overloadProvider.provideRoutineRepository())
            }
        }
    }
}