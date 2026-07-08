package com.pdm0126.overload.data.repository

import com.pdm0126.overload.data.local.dao.WorkoutDao
import com.pdm0126.overload.data.local.entity.WorkoutSessionEntity
import com.pdm0126.overload.data.local.entity.WorkoutSetEntity
import com.pdm0126.overload.data.mapper.toDomainModel
import com.pdm0126.overload.domain.model.WorkoutSession
import com.pdm0126.overload.domain.model.WorkoutSet
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImp(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    private fun computeRirFactor(rir: Int?, isRirEnabled: Boolean): Float {
        if (!isRirEnabled) return 0.5f
        return when (rir) {
            0, 1 -> 1.0f
            2, 3 -> 0.8f
            4, 5 -> 0.4f
            else -> 0.5f
        }
    }

    override fun getActiveSession(): Flow<WorkoutSession?> {
        return workoutDao.getActiveSession().map { relation ->
            relation?.toDomainModel()
        }
    }

    override fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSet>> {
        return workoutDao.getSetsForSession(sessionId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getSetsByExerciseAndSession(plannedExerciseId: Long, sessionId: Long): Flow<List<WorkoutSet>> {
        return workoutDao.getSetsByExerciseAndSession(plannedExerciseId, sessionId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }


    override suspend fun getLastSetsForExercise(
        exerciseId: String
    ): List<WorkoutSet> {
        return workoutDao.getLastSetsForExercise(exerciseId)
            .map { it.toDomainModel() }
    }

    override suspend fun startSession(dayId: Long): Long {
        val newSession = WorkoutSessionEntity(
            dayId = dayId,
            startTimestamp = System.currentTimeMillis()
        )
        return workoutDao.insertSession(newSession)
    }

    override suspend fun endSession(sessionId: Long) {
        val currentSession = workoutDao.getSessionSnapshot(sessionId) ?: return
        workoutDao.updateSession(
            currentSession.copy(endTimestamp = System.currentTimeMillis())
        )
    }
    override suspend fun cancelSession(sessionId: Long) {
        workoutDao.deleteSessionById(sessionId)
    }

    override suspend fun logSet(
        sessionId: Long,
        plannedExerciseId: Long?,
        exerciseId: String,
        setNumber: Int,
        weightKg: Float,
        reps: Int,
        rir: Int?,
        isRirEnabled: Boolean
    ): Long {
        val factor = computeRirFactor(rir, isRirEnabled)
        val newSet = WorkoutSetEntity(
            sessionId = sessionId,
            plannedExerciseId = plannedExerciseId,
            exerciseId = exerciseId,
            setNumber = setNumber,
            weightKg = weightKg,
            reps = reps,
            rir = rir,
            isRirEnabled = isRirEnabled,
            rirFactor = factor
        )
        return workoutDao.insertSet(newSet)
    }

    override suspend fun deleteSet(setId: Long) {
        workoutDao.deleteSetById(setId)
    }
}
