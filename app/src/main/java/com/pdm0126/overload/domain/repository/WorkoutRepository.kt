package com.pdm0126.overload.domain.repository

import com.pdm0126.overload.domain.model.WorkoutSession
import com.pdm0126.overload.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    fun getActiveSession(): Flow<WorkoutSession?>

    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>>

    fun getSetsByExerciseAndSession(plannedExerciseId: Long, sessionId: Long): Flow<List<WorkoutSet>>

    suspend fun getLastSetsForExercise(exerciseId: String): List<WorkoutSet>

    suspend fun startSession(dayId: Long): Long

    suspend fun endSession(sessionId: Long)

    suspend fun cancelSession(sessionId: Long)

    suspend fun logSet(
        sessionId: Long,
        plannedExerciseId: Long?,
        exerciseId: String,
        setNumber: Int,
        weightKg: Float,
        reps: Int,
        rir: Int?,
        isRirEnabled: Boolean
    ): Long

    suspend fun deleteSet(setId: Long)
}
