package com.pdm0126.overload.domain.model

data class PlannedExercise(
    val plannedExerciseId: Long,
    val order: Int,
    val targetSets: Int,
    val targetReps: Int?,
    val exercise: Exercise // El objeto completo del ejercicio asignado con toda su metadata
)
