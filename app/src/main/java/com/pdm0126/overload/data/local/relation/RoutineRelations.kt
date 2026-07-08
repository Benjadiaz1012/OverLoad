package com.pdm0126.overload.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.pdm0126.overload.data.local.entity.RoutineDayEntity
import com.pdm0126.overload.data.local.entity.ExerciseEntity
import com.pdm0126.overload.data.local.entity.RoutineEntity
import com.pdm0126.overload.data.local.entity.PlannedExerciseEntity

// Un ejercicio planificado con la metadata completa de su ejercicio
data class PlannedExerciseWithExercise(
    @Embedded val plannedExercise: PlannedExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "exerciseId"
    )
    val exercise: ExerciseEntity
)

// Un día con todos sus ejercicios planificados ordenados
data class RoutineDayWithExercises(
    @Embedded val day: RoutineDayEntity,
    @Relation(
        entity = PlannedExerciseEntity::class,
        parentColumn = "dayId",
        entityColumn = "dayId"
    )
    val plannedExercises: List<PlannedExerciseWithExercise>
)

// La rutina final que contiene todos sus días ordenados
data class RoutineWithDays(
    @Embedded val routine: RoutineEntity,
    @Relation(
        entity = RoutineDayEntity::class,
        parentColumn = "routineId",
        entityColumn = "routineId"
    )
    val days: List<RoutineDayWithExercises>
)