package com.pdm0126.overload.data.mapper

import com.pdm0126.overload.data.local.relation.RoutineDayWithExercises
import com.pdm0126.overload.data.local.relation.RoutineWithDays
import com.pdm0126.overload.data.local.relation.PlannedExerciseWithExercise
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.Routine
import com.pdm0126.overload.domain.model.PlannedExercise

fun PlannedExerciseWithExercise.toDomainModel(): PlannedExercise {
    return PlannedExercise(
        plannedExerciseId = plannedExercise.plannedExerciseId,
        order = plannedExercise.order,
        targetSets = plannedExercise.targetSets,
        targetReps = plannedExercise.targetReps,
        exercise = exercise.toDomainModel()
    )
}

fun RoutineDayWithExercises.toDomainModel(): RoutineDay {
    return RoutineDay(
        dayId = day.dayId,
        order = day.order,
        focus = day.focus,
        plannedExercises = plannedExercises.map { it.toDomainModel() }.sortedBy { it.order }
    )
}

fun RoutineWithDays.toDomainModel(): Routine {
    return Routine(
        routineId = routine.routineId,
        name = routine.name,
        blueprintType = routine.blueprintType,
        isActive = routine.isActive,
        days = days.map { it.toDomainModel() }.sortedBy { it.order }
    )
}

