package com.pdm0126.overload.data.repository

import com.pdm0126.overload.data.local.dao.RoutineDao
import com.pdm0126.overload.data.local.entity.DayEntity
import com.pdm0126.overload.data.local.entity.MicrocycleEntity
import com.pdm0126.overload.data.local.entity.SlotEntity
import com.pdm0126.overload.data.mapper.toDomainModel
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

    override suspend fun createMicrocycle(name: String, blueprintType: String, isActive: Boolean): Long {
        return withContext(Dispatchers.IO) {
            val newMicrocycle = MicrocycleEntity(
                name = name,
                blueprintType = blueprintType,
                isActive = isActive
            )
            routineDao.insertMicrocycle(newMicrocycle) // Retorna el id generado
        }
    }

    override suspend fun addDayToMicrocycle(microcycleId: Long, order: Int, focus: String): Long {
        return withContext(Dispatchers.IO) {
            val newDay = DayEntity(
                microcycleId = microcycleId,
                order = order,
                focus = focus
            )
            routineDao.insertDay(newDay) // Retorna el id del día
        }
    }

    override suspend fun addExerciseSlot(dayId: Long, exerciseId: String, order: Int, targetSets: Int): Long {
        return withContext(Dispatchers.IO) {
            val newSlot = SlotEntity(
                dayId = dayId,
                exerciseId = exerciseId,
                order = order,
                targetSets = targetSets
            )
            routineDao.insertSlot(newSlot) // Retorna el id del slot
        }
    }

    override suspend fun removeExerciseSlot(slotId: Long) {
        withContext(Dispatchers.IO) {
            routineDao.deleteSlotById(slotId)
        }
    }
}