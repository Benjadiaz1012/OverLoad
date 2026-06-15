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

    // Lógica central de negocio

    /**
     * Calcula el Factor_RIR según la tabla de ponderación definida en la arquitectura funcional.
     *
     * | Estado             | Factor |
     * |--------------------|--------|
     * | RIR desactivado    |  0.5   |
     * | RIR 4 o 5          |  0.4   |
     * | RIR 2 o 3          |  0.8   |
     * | RIR 0 o 1          |  1.0   |
     */
    private fun computeRirFactor(rir: Int?, isRirEnabled: Boolean): Float {
        if (!isRirEnabled) return 0.5f
        return when (rir) {
            0, 1 -> 1.0f
            2, 3 -> 0.8f
            4, 5 -> 0.4f
            else -> 0.5f
        }
    }

    // Flujos reactivos
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

    override fun getSetsBySlotAndSession(slotId: Long, sessionId: Long): Flow<List<WorkoutSet>> {
        return workoutDao.getSetsBySlotAndSession(slotId, sessionId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // Operaciones suspendidas

    override suspend fun getLastSetsForSlot(
        slotId: Long,
        dayId: Long,
        targetSets: Int
    ): List<WorkoutSet> {
        return workoutDao.getLastSetsForSlot(slotId, dayId, targetSets)
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
        // Recuperamos la entidad actual para actualizar sólo el endTimestamp
        // Usamos una query directa al DAO para no exponer un método innecesario en la interfaz
        val currentSession = workoutDao.getSessionSnapshot(sessionId) ?: return
        workoutDao.updateSession(
            currentSession.copy(endTimestamp = System.currentTimeMillis())
        )
    }

    override suspend fun logSet(
        sessionId: Long,
        slotId: Long?,
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
            slotId = slotId,
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
