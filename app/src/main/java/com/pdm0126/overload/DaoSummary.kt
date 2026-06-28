package com.pdm0126.overload

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pdm0126.overload.data.local.entity.DayEntity
import com.pdm0126.overload.data.local.entity.ExerciseEntity
import com.pdm0126.overload.data.local.entity.MicrocycleEntity
import com.pdm0126.overload.data.local.entity.SlotEntity
import com.pdm0126.overload.data.local.entity.WorkoutSessionEntity
import com.pdm0126.overload.data.local.entity.WorkoutSetEntity
import com.pdm0126.overload.data.local.relation.DayWithSlots
import com.pdm0126.overload.data.local.relation.MicrocycleWithDays
import com.pdm0126.overload.data.local.relation.SessionWithSets
import com.pdm0126.overload.domain.model.ExerciseVolumeRecord
import com.pdm0126.overload.domain.model.MuscleDistribution
import com.pdm0126.overload.domain.model.MuscleProgressionRecord
import kotlinx.coroutines.flow.Flow

//--------------------------------------------------------------------------------------------------
@Dao
interface AnalysisDao {

    @Query("""
        SELECT wss.startTimestamp AS timestamp, 
               SUM(ws.weightKg * ws.reps) AS totalVolume
        FROM workout_sets_table ws
        INNER JOIN workout_sessions_table wss ON ws.sessionId = wss.sessionId
        WHERE ws.exerciseId = :exerciseId 
          AND wss.endTimestamp IS NOT NULL
        GROUP BY wss.sessionId
        ORDER BY wss.startTimestamp ASC
    """)
    fun getVolumeProgressionForExercise(exerciseId: String): Flow<List<ExerciseVolumeRecord>>

    @Query("""
        SELECT wss.startTimestamp AS timestamp, 
               e.mainMuscleGroup AS muscleGroup, 
               SUM(ws.weightKg * ws.reps * ws.rirFactor) AS effectiveVolume
        FROM workout_sets_table ws
        INNER JOIN exercises_table e ON ws.exerciseId = e.exerciseId
        INNER JOIN workout_sessions_table wss ON ws.sessionId = wss.sessionId
        WHERE e.mainMuscleGroup = :muscleGroup 
          AND wss.endTimestamp IS NOT NULL
        GROUP BY wss.sessionId, e.mainMuscleGroup
        ORDER BY wss.startTimestamp ASC
    """)
    fun getEffectiveVolumeProgressionForMuscle(muscleGroup: String): Flow<List<MuscleProgressionRecord>>

    @Query("""
        SELECT e.mainMuscleGroup AS muscleGroup, 
               SUM(ws.weightKg * ws.reps * ws.rirFactor) AS totalEffectiveVolume
        FROM workout_sets_table ws
        INNER JOIN exercises_table e ON ws.exerciseId = e.exerciseId
        INNER JOIN workout_sessions_table wss ON ws.sessionId = wss.sessionId
        WHERE wss.endTimestamp IS NOT NULL
        GROUP BY e.mainMuscleGroup
        ORDER BY totalEffectiveVolume DESC
    """)
    fun getOverallMuscleDistribution(): Flow<List<MuscleDistribution>>
}
//--------------------------------------------------------------------------------------------------
@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<com.pdm0126.overload.data.local.entity.ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: com.pdm0126.overload.data.local.entity.ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: com.pdm0126.overload.data.local.entity.ExerciseEntity)

    @Query("SELECT * FROM exercises_table")
    fun getAllExercises(): Flow<List<com.pdm0126.overload.data.local.entity.ExerciseEntity>>

    @Query("SELECT * FROM exercises_table WHERE mainMuscleGroup = :muscleGroup")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<com.pdm0126.overload.data.local.entity.ExerciseEntity>>

    @Query("SELECT * FROM exercises_table WHERE exerciseId = :id LIMIT 1")
    suspend fun getExerciseById(id: String): ExerciseEntity?
}
//--------------------------------------------------------------------------------------------------
@Dao
interface RoutineDao {

    // Inserciones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMicrocycle(microcycle: com.pdm0126.overload.data.local.entity.MicrocycleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: com.pdm0126.overload.data.local.entity.DayEntity): Long

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
    fun getActiveMicrocycle(): Flow<com.pdm0126.overload.data.local.relation.MicrocycleWithDays?>

    @Transaction
    @Query("SELECT * FROM microcycles_table")
    fun getAllMicrocycles(): Flow<List<com.pdm0126.overload.data.local.relation.MicrocycleWithDays>>

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
//--------------------------------------------------------------------------------------------------
@Dao
interface WorkoutDao {

    // Sesiones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: com.pdm0126.overload.data.local.entity.WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: com.pdm0126.overload.data.local.entity.WorkoutSessionEntity)

    // Sesión activa = aquella que aún no tiene endTimestamp (null)
    @Transaction
    @Query("SELECT * FROM workout_sessions_table WHERE endTimestamp IS NULL LIMIT 1")
    fun getActiveSession(): Flow<SessionWithSets?>

    // Historial de sesiones para un día específico (para la referencia del Dashboard)
    @Query("SELECT * FROM workout_sessions_table WHERE dayId = :dayId ORDER BY startTimestamp DESC")
    fun getSessionsByDayId(dayId: Long): Flow<List<com.pdm0126.overload.data.local.entity.WorkoutSessionEntity>>

    // Última sesión completada de un día específico (para el panel de referencia histórica)
    @Query("SELECT * FROM workout_sessions_table WHERE dayId = :dayId AND endTimestamp IS NOT NULL ORDER BY startTimestamp DESC LIMIT 1")
    suspend fun getLastCompletedSessionForDay(dayId: Long): com.pdm0126.overload.data.local.entity.WorkoutSessionEntity?

    // Series

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: com.pdm0126.overload.data.local.entity.WorkoutSetEntity): Long

    @Query("DELETE FROM workout_sets_table WHERE setId = :setId")
    suspend fun deleteSetById(setId: Long)

    // Todas las series de una sesión (usado para el resumen final)
    @Query("SELECT * FROM workout_sets_table WHERE sessionId = :sessionId ORDER BY slotId, setNumber")
    fun getSetsForSession(sessionId: Long): Flow<List<com.pdm0126.overload.data.local.entity.WorkoutSetEntity>>

    // Series de un slot específico dentro de la sesión activa (para expandir una tarjeta de ejercicio)
    @Query("SELECT * FROM workout_sets_table WHERE slotId = :slotId AND sessionId = :sessionId ORDER BY setNumber")
    fun getSetsBySlotAndSession(slotId: Long, sessionId: Long): Flow<List<com.pdm0126.overload.data.local.entity.WorkoutSetEntity>>

    // Series del último slot completado en una sesión anterior (para la referencia histórica por ejercicio)
    @Query("""
        SELECT ws.* FROM workout_sets_table ws
        INNER JOIN workout_sessions_table wss ON ws.sessionId = wss.sessionId
        WHERE ws.slotId = :slotId
        AND wss.dayId = :dayId
        AND wss.endTimestamp IS NOT NULL
        ORDER BY wss.startTimestamp DESC, ws.setNumber ASC
        LIMIT :targetSets
    """)
    suspend fun getLastSetsForSlot(slotId: Long, dayId: Long, targetSets: Int): List<WorkoutSetEntity>

    // Snapshot directo de una sesión por ID (uso interno del repositorio para actualizar endTimestamp)
    @Query("SELECT * FROM workout_sessions_table WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionSnapshot(sessionId: Long): WorkoutSessionEntity?
}

