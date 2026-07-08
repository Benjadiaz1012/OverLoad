package com.pdm0126.overload.domain.model

data class Routine(
    val routineId: Long,
    val name: String,
    val blueprintType: String,
    val isActive: Boolean,
    val days: List<RoutineDay>
)