package com.pdm0126.overload.ui.screens.dashboard.workout

import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.WorkoutSession
import com.pdm0126.overload.domain.model.WorkoutSet

data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val activeSession: WorkoutSession? = null,
    val activeDay: RoutineDay? = null,
    val sessionSets: List<WorkoutSet> = emptyList(),
    val lastSets: Map<String, List<WorkoutSet>> = emptyMap()
)