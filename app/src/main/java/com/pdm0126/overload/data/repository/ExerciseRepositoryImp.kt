package com.pdm0126.overload.data.repository

import com.pdm0126.overload.data.local.dao.ExerciseDao
import com.pdm0126.overload.data.mapper.toDomainModel
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.repository.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExerciseRepositoryImp(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercises().map { entities ->
            entities.map { entity -> entity.toDomainModel() }
        }
    }

    override fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByMuscleGroup(muscleGroup).map { entities ->
            entities.map { entity -> entity.toDomainModel() }
        }
    }

    override suspend fun getExerciseById(id: String): Exercise? {
        return withContext(Dispatchers.IO) {
            exerciseDao.getExerciseById(id)?.toDomainModel()
        }
    }
}