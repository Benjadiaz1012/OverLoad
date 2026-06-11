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
import com.pdm0126.overload.data.local.relation.MicrocycleWithDays
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    // Inserciones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMicrocycle(microcycle: MicrocycleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDay(day: DayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSlot(slot: SlotEntity): Long

    // Actualizaciones/Borrados
    @Update
    fun updateMicrocycle(microcycle: MicrocycleEntity)

    @Update
    fun updateDay(day: DayEntity)

    @Query("DELETE FROM slots_table WHERE slotId = :slotId")
    fun deleteSlotById(slotId: Long)

    // Consultas principales
    // Con @Transaction room lee nuestras clases de relación y arma el arbol completo
    @Transaction
    @Query("SELECT * FROM microcycles_table WHERE isActive = 1 LIMIT 1")
    fun getActiveMicrocycle(): Flow<MicrocycleWithDays?>

    @Transaction
    @Query("SELECT * FROM microcycles_table")
    fun getAllMicrocycles(): Flow<List<MicrocycleWithDays>>
}