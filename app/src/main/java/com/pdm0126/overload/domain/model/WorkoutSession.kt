package com.pdm0126.overload.domain.model

data class WorkoutSession(
    val sessionId: Long,
    val dayId: Long,
    val startTimestamp: Long,
    val endTimestamp: Long?,    // null = sesión actualmente en curso
    val sets: List<WorkoutSet>  // Todas las series registradas en esta sesión
)
