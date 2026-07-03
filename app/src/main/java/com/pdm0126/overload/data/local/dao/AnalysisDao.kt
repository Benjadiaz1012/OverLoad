package com.pdm0126.overload.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.pdm0126.overload.domain.model.ExerciseVolumeRecord
import com.pdm0126.overload.domain.model.MuscleDistribution
import com.pdm0126.overload.domain.model.MuscleProgressionRecord
import kotlinx.coroutines.flow.Flow

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