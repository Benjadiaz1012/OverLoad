package com.pdm0126.overload.domain.model

data class RoutineMicrocycle(
    val microcycleId: Long,
    val name: String,
    val blueprintType: String,
    val isActive: Boolean,
    val days: List<RoutineDay>
)