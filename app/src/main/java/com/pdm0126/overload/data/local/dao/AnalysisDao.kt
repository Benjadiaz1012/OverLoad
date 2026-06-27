package com.pdm0126.overload.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.pdm0126.overload.domain.model.ExerciseVolumeRecord
import com.pdm0126.overload.domain.model.MuscleDistribution
import com.pdm0126.overload.domain.model.MuscleProgressionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {

    // 1. Progreso de Volumen Crudo por Ejercicio a través del tiempo
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

    // 2. Progreso de Volumen Efectivo (con RIR Factor) por Grupo Muscular a través del tiempo
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

    // 3. Distribución global de esfuerzo (Ideal para gráfica de pastel/barras)
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