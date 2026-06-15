package com.pdm0126.overload.ui.viewmodels.test

import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.model.WorkoutSession
import com.pdm0126.overload.domain.model.WorkoutSet

sealed interface WorkoutTestUiState {
    object Loading : WorkoutTestUiState

    // No hay microciclo activo configurado aún
    object NoRoutine : WorkoutTestUiState

    // Hay microciclo pero no hay sesión activa (Dashboard pre-sesión)
    data class Ready(
        val microcycle: RoutineMicrocycle,
        val currentDayIndex: Int,       // Índice del día a entrenar hoy (0-based)
        val lastSets: Map<Long, List<WorkoutSet>> // slotId -> series de la sesión anterior
    ) : WorkoutTestUiState

    // Sesión en curso: la ficha activa está expandida
    data class SessionActive(
        val microcycle: RoutineMicrocycle,
        val currentDayIndex: Int,
        val session: WorkoutSession,
        val setsBySlot: Map<Long, List<WorkoutSet>>, // slotId -> series registradas en ESTA sesión
        val lastSets: Map<Long, List<WorkoutSet>>    // slotId -> referencia de la sesión anterior
    ) : WorkoutTestUiState

    data class Error(val message: String) : WorkoutTestUiState
}
