package com.pdm0126.overload.data.repository

import com.pdm0126.overload.data.local.dao.RoutineDao
import com.pdm0126.overload.data.local.entity.RoutineDayEntity
import com.pdm0126.overload.data.local.entity.RoutineEntity
import com.pdm0126.overload.data.local.entity.PlannedExerciseEntity
import com.pdm0126.overload.data.mapper.toDomainModel
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.Routine
import com.pdm0126.overload.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoutineRepositoryImp(
    private val routineDao: RoutineDao
) : RoutineRepository {

    override fun getActiveRoutine(): Flow<Routine?> {
        return routineDao.getActiveRoutine().map { relation ->
            relation?.toDomainModel()
        }
    }

    override fun getAllRoutines(): Flow<List<Routine>> {
        return routineDao.getAllRoutines().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getRoutineById(routineId: Long): Flow<Routine?> {
        return routineDao.getRoutineById(routineId).map { it?.toDomainModel() }
    }

    override fun getRoutineDay(dayId: Long?): Flow<RoutineDay?> {
        return routineDao.getDayWithPlannedExercises(dayId).map { it?.toDomainModel() }
    }

    override suspend fun updateTargetSets(plannedExerciseId: Long, targetSets: Int) {
        routineDao.updateTargetSets(plannedExerciseId, targetSets)
    }

    override suspend fun updateTargetReps(plannedExerciseId: Long, targetReps: Int?) {
        routineDao.updateTargetReps(plannedExerciseId, targetReps)
    }

    override suspend fun deleteRoutine(routineId: Long) {
        routineDao.deleteRoutine(routineId)
    }

    override suspend fun deleteRoutineDay(dayId: Long) {
        routineDao.deleteRoutineDay(dayId)
    }
    override suspend fun updateDayFocus(dayId: Long, newFocus: String) {
        routineDao.updateDayFocus(dayId, newFocus)
    }
    override suspend fun updateRoutineName(routineId: Long, newName: String) {
        routineDao.updateRoutineName(routineId, newName)
    }

    override suspend fun createRoutine(name: String, blueprintType: String, isActive: Boolean): Long {
        val newRoutine = RoutineEntity(
            name = name,
            blueprintType = blueprintType,
            isActive = isActive
        )
        return routineDao.insertRoutine(newRoutine)
    }

    override suspend fun addDayToRoutine(routineId: Long, order: Int, focus: String): Long {
        val newDay = RoutineDayEntity(
            routineId = routineId,
            order = order,
            focus = focus
        )
        return routineDao.insertRoutineDay(newDay)
    }

    override suspend fun addPlannedExercise(dayId: Long, exerciseId: String, order: Int, targetSets: Int, targetReps: Int?): Long {
        val newPlannedExercise = PlannedExerciseEntity(
            dayId = dayId,
            exerciseId = exerciseId,
            order = order,
            targetSets = targetSets,
            targetReps = targetReps
        )
        return routineDao.insertPlannedExercise(newPlannedExercise)
    }

    override suspend fun removePlannedExercise(plannedExerciseId: Long?) {
        routineDao.deletePlannedExercise(plannedExerciseId)
    }
    override suspend fun updateActiveRoutine(routineId: Long) {
        routineDao.updateActiveRoutine(routineId)
    }
}