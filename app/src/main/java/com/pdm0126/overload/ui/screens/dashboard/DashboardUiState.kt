package com.pdm0126.overload.ui.screens.dashboard

import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.model.WorkoutSession
import com.pdm0126.overload.domain.model.WorkoutSet

data class DashboardUiState(
    val isLoading: Boolean = true,
    val activeMicrocycle: RoutineMicrocycle? = null,
    val activeSessionDayId: Long? = null
)