package com.pdm0126.overload.ui.viewmodels.test

import com.pdm0126.overload.domain.model.RoutineMicrocycle

sealed interface RoutineConfigUiState {
    object Loading : RoutineConfigUiState
    // Estado cuando el usuario aún no ha creado un microciclo activo
    object Empty : RoutineConfigUiState
    // Estado cuando ya hay un lienzo (Microciclo) sobre el cual trabajar
    data class Active(val microcycle: RoutineMicrocycle) : RoutineConfigUiState
    data class Error(val message: String) : RoutineConfigUiState
}