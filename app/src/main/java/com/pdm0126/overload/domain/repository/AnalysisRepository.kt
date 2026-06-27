package com.pdm0126.overload.domain.repository

import com.pdm0126.overload.domain.model.ExerciseVolumeRecord
import com.pdm0126.overload.domain.model.MuscleDistribution
import com.pdm0126.overload.domain.model.MuscleProgressionRecord
import kotlinx.coroutines.flow.Flow

interface AnalysisRepository {
    fun getVolumeProgressionForExercise(exerciseId: String): Flow<List<ExerciseVolumeRecord>>
    fun getEffectiveVolumeProgressionForMuscle(muscleGroup: String): Flow<List<MuscleProgressionRecord>>
    fun getOverallMuscleDistribution(): Flow<List<MuscleDistribution>>
}