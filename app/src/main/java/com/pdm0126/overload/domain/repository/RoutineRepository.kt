package com.pdm0126.overload.domain.repository

import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {

    fun getActiveRoutine(): Flow<Routine?>
    fun getAllRoutines(): Flow<List<Routine>>
    fun getRoutineById(routineId: Long): Flow<Routine?>
    fun getRoutineDay(dayId: Long?): Flow<RoutineDay?>
    suspend fun updateTargetSets(plannedExerciseId: Long, targetSets: Int)
    suspend fun updateTargetReps(plannedExerciseId: Long, targetReps: Int?)
    suspend fun deleteRoutine(routineId: Long)
    suspend fun deleteRoutineDay(dayId: Long)
    suspend fun updateDayFocus(dayId: Long, newFocus: String)
    suspend fun updateRoutineName(routineId: Long, newName: String)
    suspend fun createRoutine(name: String, blueprintType: String, isActive: Boolean = true): Long
    suspend fun addDayToRoutine(routineId: Long, order: Int, focus: String): Long
    suspend fun addPlannedExercise(dayId: Long, exerciseId: String, order: Int, targetSets: Int, targetReps: Int?): Long
    suspend fun removePlannedExercise(plannedExerciseId: Long?)
    suspend fun updateActiveRoutine(routineId: Long)
}