package com.pdm0126.overload.data.repository

import com.pdm0126.overload.data.local.dao.ExerciseDao
import com.pdm0126.overload.data.mapper.toDomainModel
import com.pdm0126.overload.data.remote.ExerciseApiClient
import com.pdm0126.overload.data.remote.dto.ExerciseDto
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.repository.ExerciseRepository
import io.ktor.client.call.body
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import io.ktor.client.request.get

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

    override suspend fun getExerciseById(id: String): Exercise? {
        return withContext(Dispatchers.IO) {
            exerciseDao.getExerciseById(id)?.toDomainModel()
        }
    }

    override suspend fun searchRemoteExercises(query: String): List<Exercise> {
        return withContext(Dispatchers.IO) {
            // Realiza la búsqueda en la API remota
            val remoteDtos = ktorClient.fetchRemoteExercises()
            remoteDtos
                .filter { dto -> dto.name.contains(query, ignoreCase = true) }
                .map { dto -> dto.toDomainModel() }
        }
    }
}