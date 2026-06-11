package com.pdm0126.overload.domain.model

data class RoutineSlot(
    val slotId: Long,
    val order: Int,
    val targetSets: Int,
    val exercise: Exercise // El objeto completo del ejercicio asignado con toda su metadata
)
