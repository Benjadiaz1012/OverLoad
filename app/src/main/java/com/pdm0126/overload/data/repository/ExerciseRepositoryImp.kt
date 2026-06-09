package com.pdm0126.overload.data.repository

import com.pdm0126.overload.data.local.dao.ExerciseDao
import com.pdm0126.overload.data.local.entity.ExerciseEntity
import com.pdm0126.overload.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow

class ExerciseRepositoryImp(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<ExerciseEntity>> {
        return exerciseDao.getAllExercises()
    }

    override fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesByMuscleGroup(muscleGroup)
    }

    override suspend fun getExerciseById(id: String): ExerciseEntity? {
        return exerciseDao.getExerciseById(id)
    }
}

