package com.pdm0126.overload.domain.repository

import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {

    fun getActiveMicrocycle(): Flow<RoutineMicrocycle?>
    fun getAllMicrocycles(): Flow<List<RoutineMicrocycle>>
    fun getMicrocycleById(microcycleId: Long): Flow<RoutineMicrocycle?>
    fun getRoutineDay(dayId: Long): Flow<RoutineDay?>
    suspend fun updateSlotTargetSets(slotId: Long, targetSets: Int)
    suspend fun deleteMicrocycle(microcycleId: Long)
    suspend fun deleteDay(dayId: Long)
    suspend fun updateDayFocus(dayId: Long, newFocus: String)
    suspend fun updateMicrocycleName(microcycleId: Long, newName: String)
    suspend fun createMicrocycle(name: String, blueprintType: String, isActive: Boolean = true): Long
    suspend fun addDayToMicrocycle(microcycleId: Long, order: Int, focus: String): Long
    suspend fun addExerciseSlot(dayId: Long, exerciseId: String, order: Int, targetSets: Int): Long
    suspend fun removeExerciseSlot(slotId: Long)
    suspend fun updateActiveMicrocycle(microcycleId: Long)
}