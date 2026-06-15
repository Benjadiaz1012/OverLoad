package com.pdm0126.overload.data.repository

import com.pdm0126.overload.data.local.dao.ExerciseDao
import com.pdm0126.overload.data.mapper.toDomainModel
import com.pdm0126.overload.data.mapper.toEntity
import com.pdm0126.overload.data.remote.ExerciseApiClient
import com.pdm0126.overload.data.remote.dto.ExerciseDto
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseRepositoryImp(
    private val exerciseDao: ExerciseDao,
    private val ktorClient: ExerciseApiClient
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
    override suspend fun saveRemoteExerciseToLocal(exercise: Exercise) {
        val entity = exercise.toEntity()
        exerciseDao.insertExercise(entity)
    }

    override suspend fun getExerciseById(id: String): Exercise? {
        return exerciseDao.getExerciseById(id)?.toDomainModel()
    }

    override suspend fun searchRemoteExercises(query: String): List<Exercise> {
        val remoteDtos : List<ExerciseDto> = ktorClient.fetchRemoteExercises()
        return remoteDtos
            .filter { dto -> dto.name.contains(query, ignoreCase = true) }
            .map { dto -> dto.toDomainModel() }
    }
}