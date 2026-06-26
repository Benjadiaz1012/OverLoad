package com.pdm0126.overload.data.repository

import com.pdm0126.overload.data.local.dao.RoutineDao
import com.pdm0126.overload.data.local.entity.DayEntity
import com.pdm0126.overload.data.local.entity.MicrocycleEntity
import com.pdm0126.overload.data.local.entity.SlotEntity
import com.pdm0126.overload.data.mapper.toDomainModel
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoutineRepositoryImp(
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

    override fun getRoutineDay(dayId: Long): Flow<RoutineDay?> {
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

    override suspend fun removeExerciseSlot(slotId: Long) {
        routineDao.deleteSlotById(slotId)
    }
    override suspend fun updateActiveMicrocycle(microcycleId: Long) {
        routineDao.updateActiveMicrocycle(microcycleId)
    }
}