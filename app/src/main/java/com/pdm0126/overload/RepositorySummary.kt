package com.pdm0126.overload

import com.pdm0126.overload.data.local.dao.AnalysisDao
import com.pdm0126.overload.data.local.dao.ExerciseDao
import com.pdm0126.overload.data.local.dao.RoutineDao
import com.pdm0126.overload.data.local.dao.WorkoutDao
import com.pdm0126.overload.data.local.entity.DayEntity
import com.pdm0126.overload.data.local.entity.MicrocycleEntity
import com.pdm0126.overload.data.local.entity.SlotEntity
import com.pdm0126.overload.data.local.entity.WorkoutSessionEntity
import com.pdm0126.overload.data.local.entity.WorkoutSetEntity
import com.pdm0126.overload.data.mapper.toDomainModel
import com.pdm0126.overload.data.mapper.toEntity
import com.pdm0126.overload.data.remote.ExerciseApiClient
import com.pdm0126.overload.data.remote.dto.ExerciseDto
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.model.ExerciseVolumeRecord
import com.pdm0126.overload.domain.model.MuscleDistribution
import com.pdm0126.overload.domain.model.MuscleProgressionRecord
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.model.WorkoutSession
import com.pdm0126.overload.domain.model.WorkoutSet
import com.pdm0126.overload.domain.repository.AnalysisRepository
import com.pdm0126.overload.domain.repository.ExerciseRepository
import com.pdm0126.overload.domain.repository.RoutineRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnalysisRepositoryImp(
    private val analysisDao: AnalysisDao
) : AnalysisRepository {

    override fun getVolumeProgressionForExercise(exerciseId: String): Flow<List<ExerciseVolumeRecord>> {
        return analysisDao.getVolumeProgressionForExercise(exerciseId)
    }

    override fun getEffectiveVolumeProgressionForMuscle(muscleGroup: String): Flow<List<MuscleProgressionRecord>> {
        return analysisDao.getEffectiveVolumeProgressionForMuscle(muscleGroup)
    }

    override fun getOverallMuscleDistribution(): Flow<List<MuscleDistribution>> {
        return analysisDao.getOverallMuscleDistribution()
    }
}
//----------------------------------------------------------------------------------------------------------------
class ExerciseRepositoryImp(
    private val exerciseDao: ExerciseDao,
    private val ktorClient: ExerciseApiClient
) : ExerciseRepository {

    override fun getLocalExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises().map { entities ->
            entities.map { entity -> entity.toDomainModel() }
        }
    }

    override fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByMuscleGroup(muscleGroup).map { entities ->
            entities.map { entity -> entity.toDomainModel() }
        }
    }
    override suspend fun saveRemoteExerciseToLocal(exercise: Exercise) {
        val entity = exercise.toEntity()
        exerciseDao.insertExercise(entity)
    }

    override suspend fun deleteLocalExercise(exercise: Exercise) {
        val entity = exercise.toEntity()
        exerciseDao.deleteExercise(entity)
    }

    override suspend fun getExerciseById(id: String): Exercise? {
        return exerciseDao.getExerciseById(id)?.toDomainModel()
    }

    override suspend fun getRemoteExercises(query: String): Result<List<Exercise>> {
        try {
            val remoteDtos: List<ExerciseDto> = ktorClient.fetchRemoteExercises()
            val filteredDtos = remoteDtos.filter { dto -> dto.name.contains(query, ignoreCase = true) }
            val domainModels = filteredDtos.map { dto -> dto.toDomainModel() }
            return Result.success(domainModels)
        }
        catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
//----------------------------------------------------------------------------------------------------------------
/*class RoutineRepositoryImp(
    private val routineDao: RoutineDao
) : RoutineRepository {

    override fun getActiveMicrocycle(): Flow<RoutineMicrocycle?> {
        return routineDao.getActiveMicrocycle().map { relation ->
            relation?.toDomainModel()
        }
    }

    override fun getAllMicrocycles(): Flow<List<RoutineMicrocycle>> {
        return routineDao.getAllMicrocycles().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getMicrocycleById(microcycleId: Long): Flow<RoutineMicrocycle?> {
        return routineDao.getMicrocycleById(microcycleId).map { it?.toDomainModel() }
    }

    override fun getRoutineDay(dayId: Long?): Flow<RoutineDay?> {
        return routineDao.getDayWithSlots(dayId).map { it?.toDomainModel() }
    }

    override suspend fun updateSlotTargetSets(slotId: Long, targetSets: Int) {
        routineDao.updateSlotTargetSets(slotId, targetSets)
    }

    override suspend fun deleteMicrocycle(microcycleId: Long) {
        routineDao.deleteMicrocycle(microcycleId)
    }

    override suspend fun deleteDay(dayId: Long) {
        routineDao.deleteDay(dayId)
    }
    override suspend fun updateDayFocus(dayId: Long, newFocus: String) {
        routineDao.updateDayFocus(dayId, newFocus)
    }
    override suspend fun updateMicrocycleName(microcycleId: Long, newName: String) {
        routineDao.updateMicrocycleName(microcycleId, newName)
    }

    override suspend fun createMicrocycle(name: String, blueprintType: String, isActive: Boolean): Long {
        val newMicrocycle = MicrocycleEntity(
            name = name,
            blueprintType = blueprintType,
            isActive = isActive
        )
        return routineDao.insertMicrocycle(newMicrocycle) // Retorna el id generado
    }

    override suspend fun addDayToMicrocycle(microcycleId: Long, order: Int, focus: String): Long {
        val newDay = DayEntity(
            microcycleId = microcycleId,
            order = order,
            focus = focus
        )
        return routineDao.insertDay(newDay) // Retorna el id del día
    }

    override suspend fun addExerciseSlot(dayId: Long, exerciseId: String, order: Int, targetSets: Int): Long {
        val newSlot = SlotEntity(
            dayId = dayId,
            exerciseId = exerciseId,
            order = order,
            targetSets = targetSets
        )
        return routineDao.insertSlot(newSlot) // Retorna el id del slot
    }

    override suspend fun removeExerciseSlot(slotId: Long?) {
        routineDao.deleteSlotById(slotId)
    }
    override suspend fun updateActiveMicrocycle(microcycleId: Long) {
        routineDao.updateActiveMicrocycle(microcycleId)
    }
}*/
//----------------------------------------------------------------------------------------------------------------
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

    override fun getSetsBySlotAndSession(slotId: Long, sessionId: Long): Flow<List<WorkoutSet>> {
        return workoutDao.getSetsBySlotAndSession(slotId, sessionId).map { entities ->
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
//----------------------------------------------------------------------------------------------------------------

//----------------------------------------------------------------------------------------------------------------