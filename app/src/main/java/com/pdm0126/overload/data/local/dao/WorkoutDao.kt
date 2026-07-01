package com.pdm0126.overload.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pdm0126.overload.data.local.entity.WorkoutSessionEntity
import com.pdm0126.overload.data.local.entity.WorkoutSetEntity
import com.pdm0126.overload.data.local.relation.SessionWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // Sesiones
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    // Sesión activa = aquella que aún no tiene endTimestamp (null)
    @Transaction
    @Query("SELECT * FROM workout_sessions_table WHERE endTimestamp IS NULL LIMIT 1")
    fun getActiveSession(): Flow<SessionWithSets?>

    // Historial de sesiones para un día específico (para la referencia del Dashboard)
    @Query("SELECT * FROM workout_sessions_table WHERE dayId = :dayId ORDER BY startTimestamp DESC")
    fun getSessionsByDayId(dayId: Long): Flow<List<WorkoutSessionEntity>>

    // Última sesión completada de un día específico (para el panel de referencia histórica)
    @Query("SELECT * FROM workout_sessions_table WHERE dayId = :dayId AND endTimestamp IS NOT NULL ORDER BY startTimestamp DESC LIMIT 1")
    suspend fun getLastCompletedSessionForDay(dayId: Long): WorkoutSessionEntity?

    // Series

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Query("DELETE FROM workout_sets_table WHERE setId = :setId")
    suspend fun deleteSetById(setId: Long)

    @Query("DELETE FROM workout_sessions_table WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    // Todas las series de una sesión (usado para el resumen final)
    @Query("SELECT * FROM workout_sets_table WHERE sessionId = :sessionId ORDER BY slotId, setNumber")
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetEntity>>

    // Series de un slot específico dentro de la sesión activa (para expandir una tarjeta de ejercicio)
    @Query("SELECT * FROM workout_sets_table WHERE slotId = :slotId AND sessionId = :sessionId ORDER BY setNumber")
    fun getSetsBySlotAndSession(slotId: Long, sessionId: Long): Flow<List<WorkoutSetEntity>>

    // Series del último slot completado en una sesión anterior (para la referencia histórica por ejercicio)
    @Query("""
        SELECT ws.* FROM workout_sets_table ws
        INNER JOIN workout_sessions_table wss ON ws.sessionId = wss.sessionId
        WHERE ws.exerciseId = :exerciseId
          AND wss.endTimestamp IS NOT NULL
          AND wss.sessionId = (
              SELECT wss2.sessionId FROM workout_sets_table ws2
              INNER JOIN workout_sessions_table wss2 ON ws2.sessionId = wss2.sessionId
              WHERE ws2.exerciseId = :exerciseId AND wss2.endTimestamp IS NOT NULL
              ORDER BY wss2.startTimestamp DESC LIMIT 1
          )
        ORDER BY ws.setNumber ASC
    """)
    suspend fun getLastSetsForExercise(exerciseId: String): List<WorkoutSetEntity>

    // Snapshot directo de una sesión por ID (uso interno del repositorio para actualizar endTimestamp)
    @Query("SELECT * FROM workout_sessions_table WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionSnapshot(sessionId: Long): WorkoutSessionEntity?
}
