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
import com.pdm0126.overload.data.local.relation.MicrocycleWithDays
import com.pdm0126.overload.data.local.relation.SessionWithSets
import kotlinx.coroutines.flow.Flow

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

/*-------------------------------------------------------------------------------------*/
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

    // Consultas principales
    // Con @Transaction room lee nuestras clases de relación y arma el arbol completo
    @Transaction
    @Query("SELECT * FROM microcycles_table WHERE isActive = 1 LIMIT 1")
    fun getActiveMicrocycle(): Flow<com.pdm0126.overload.data.local.relation.MicrocycleWithDays?>

    @Transaction
    @Query("SELECT * FROM microcycles_table")
    fun getAllMicrocycles(): Flow<List<MicrocycleWithDays>>
}
/*-------------------------------------------------------------------------------------*/
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
