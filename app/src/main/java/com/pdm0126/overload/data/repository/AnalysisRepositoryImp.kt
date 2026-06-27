package com.pdm0126.overload.data.repository

import com.pdm0126.overload.data.local.dao.AnalysisDao
import com.pdm0126.overload.domain.model.ExerciseVolumeRecord
import com.pdm0126.overload.domain.model.MuscleDistribution
import com.pdm0126.overload.domain.model.MuscleProgressionRecord
import com.pdm0126.overload.domain.repository.AnalysisRepository
import kotlinx.coroutines.flow.Flow

class AnalysisRepositoryImp(
    private val analysisDao: AnalysisDao
) : AnalysisRepository {

    override fun getVolumeProgressionForExercise(exerciseId: String): Flow<List<ExerciseVolumeRecord>> {
        return analysisDao.getVolumeProgressionForExercise(exerciseId)
    }

    override fun getEffectiveVolumeProgressionForMuscle(muscleGroup: String): Flow<List<MuscleProgressionRecord>> {
        return analysisDao.getEffectiveVolumeProgressionForMuscle(muscleGroup)
    }

    override fun getOverallMuscleDistribution(): Flow<List<MuscleDistribution>> {
        return analysisDao.getOverallMuscleDistribution()
    }
}