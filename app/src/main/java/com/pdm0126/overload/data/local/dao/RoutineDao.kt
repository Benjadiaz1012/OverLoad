package com.pdm0126.overload.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pdm0126.overload.data.local.entity.DayEntity
import com.pdm0126.overload.data.local.entity.MicrocycleEntity
import com.pdm0126.overload.data.local.entity.SlotEntity
import com.pdm0126.overload.data.local.relation.DayWithSlots
import com.pdm0126.overload.data.local.relation.MicrocycleWithDays
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    // Inserciones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMicrocycle(microcycle: MicrocycleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: DayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: SlotEntity): Long

    // Actualizaciones/Borrados
    @Update
    suspend fun updateMicrocycle(microcycle: MicrocycleEntity)

    @Update
    suspend fun updateDay(day: DayEntity)

    @Query("DELETE FROM slots_table WHERE slotId = :slotId")
    suspend fun deleteSlotById(slotId: Long)

    @Query("UPDATE slots_table SET targetSets = :targetSets WHERE slotId = :slotId")
    suspend fun updateSlotTargetSets(slotId: Long, targetSets: Int)

    @Query("DELETE FROM days_table WHERE dayId = :dayId")
    suspend fun deleteDay(dayId: Long)

    @Query("UPDATE days_table SET focus = :newFocus WHERE dayId = :dayId")
    suspend fun updateDayFocus(dayId: Long, newFocus: String)
    @Query("UPDATE microcycles_table SET name = :newName WHERE microcycleId = :microcycleId")
    suspend fun updateMicrocycleName(microcycleId: Long, newName: String)
    @Query("DELETE FROM microcycles_table WHERE microcycleId = :microcycleId")
    suspend fun deleteMicrocycle(microcycleId: Long)

    // Consultas principales
    // Con @Transaction room lee nuestras clases de relación y arma el arbol completo
    @Transaction
    @Query("SELECT * FROM microcycles_table WHERE isActive = 1 LIMIT 1")
    fun getActiveMicrocycle(): Flow<MicrocycleWithDays?>

    @Transaction
    @Query("SELECT * FROM microcycles_table")
    fun getAllMicrocycles(): Flow<List<MicrocycleWithDays>>

    @Transaction
    @Query("SELECT * FROM microcycles_table WHERE microcycleId = :microcycleId")
    fun getMicrocycleById(microcycleId: Long): Flow<MicrocycleWithDays?>

    @Transaction
    @Query("SELECT * FROM days_table WHERE dayId = :dayId LIMIT 1")
    fun getDayWithSlots(dayId: Long): Flow<DayWithSlots?>

    @Transaction
    suspend fun updateActiveMicrocycle(microcycleId: Long) {
        clearAllActiveMicrocycles()
        setActiveMicrocycleById(microcycleId)
    }

    @Query("UPDATE microcycles_table SET isActive = 0")
    suspend fun clearAllActiveMicrocycles()

    @Query("UPDATE microcycles_table SET isActive = 1 WHERE microcycleId = :microcycleId")
    suspend fun setActiveMicrocycleById(microcycleId: Long)
}