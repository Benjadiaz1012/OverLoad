package com.pdm0126.overload.domain.repository

import com.pdm0126.overload.domain.model.RoutineMicrocycle
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {

    fun getActiveMicrocycle(): Flow<RoutineMicrocycle?>
    fun getAllMicrocycles(): Flow<List<RoutineMicrocycle>>
    suspend fun createMicrocycle(name: String, blueprintType: String, isActive: Boolean = true): Long
    suspend fun addDayToMicrocycle(microcycleId: Long, order: Int, focus: String): Long
    suspend fun addExerciseSlot(dayId: Long, exerciseId: String, order: Int, targetSets: Int): Long
    suspend fun removeExerciseSlot(slotId: Long)
}