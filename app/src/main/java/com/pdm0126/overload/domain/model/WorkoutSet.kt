package com.pdm0126.overload.domain.model

data class WorkoutSet(
    val setId: Long,
    val sessionId: Long,
    val slotId: Long?,
    val exerciseId: String,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val rir: Int?,              // null si el RIR fue desactivado por el usuario
    val isRirEnabled: Boolean,
    val rirFactor: Float        // Factor de ponderación pre-calculado (0.4, 0.5, 0.8 o 1.0)
)
