package com.pdm0126.overload.domain.model

data class WorkoutSet(
    val setId: Long,
    val sessionId: Long,
    val plannedExerciseId: Long?,
    val exerciseId: String,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val rir: Int?,
    val isRirEnabled: Boolean,
    val rirFactor: Float
)
