package com.pdm0126.overload

import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineSlot
import com.pdm0126.overload.domain.model.WorkoutSet

data class Exercise(
    val id : String,
    val name : String,
    val muscleGroup : String,
    val mechanic : String,
    val targetMuscles : List<String>,
    val secondaryMuscles : List<String>,
    val equipment : String,
    val instructions : List<String>,
    val remoteImages : List<String>
)

/*-------------------------------------------------------------------------------------*/
data class RoutineDay(
    val dayId: Long,
    val order: Int,
    val focus: String,
    val slots: List<RoutineSlot> // Lista plana de ejercicios del día en su orden respectivo
)
/*-------------------------------------------------------------------------------------*/
data class RoutineMicrocycle(
    val microcycleId: Long,
    val name: String,
    val blueprintType: String,
    val isActive: Boolean,
    val days: List<RoutineDay>
)
/*-------------------------------------------------------------------------------------*/
data class RoutineSlot(
    val slotId: Long,
    val order: Int,
    val targetSets: Int,
    val exercise: Exercise // El objeto completo del ejercicio asignado con toda su metadata
)
/*-------------------------------------------------------------------------------------*/
data class WorkoutSession(
    val sessionId: Long,
    val dayId: Long,
    val startTimestamp: Long,
    val endTimestamp: Long?,    // null = sesión actualmente en curso
    val sets: List<WorkoutSet>  // Todas las series registradas en esta sesión
)
/*-------------------------------------------------------------------------------------*/
data class WorkoutSet(
    val setId: Long,
    val sessionId: Long,
    val slotId: Long?,
    val exerciseId: String,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val rir: Int?,
    val isRirEnabled: Boolean,
    val rirFactor: Float
)
/*-------------------------------------------------------------------------------------*/
