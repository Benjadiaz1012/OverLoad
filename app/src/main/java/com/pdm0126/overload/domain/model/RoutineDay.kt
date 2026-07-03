package com.pdm0126.overload.domain.model

data class RoutineDay(
    val dayId: Long,
    val order: Int,
    val focus: String,
    val slots: List<RoutineSlot> // Lista plana de ejercicios del día en su orden respectivo
)
