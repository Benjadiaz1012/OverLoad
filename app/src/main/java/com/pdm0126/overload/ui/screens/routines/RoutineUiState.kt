package com.pdm0126.overload.ui.screens.routines

import com.pdm0126.overload.domain.model.RoutineMicrocycle

data class RoutinesUiState(
    val savedMicrocycles: List<RoutineMicrocycle> = emptyList(),
    val activeMicrocycleId: Long? = null,
    val isCreating: Boolean = false
)

