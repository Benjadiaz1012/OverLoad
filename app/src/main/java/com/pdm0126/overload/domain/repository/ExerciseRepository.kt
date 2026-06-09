package com.pdm0126.overload.domain.repository

import com.pdm0126.overload.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>>
    suspend fun getExerciseById(id: String): Exercise?
}