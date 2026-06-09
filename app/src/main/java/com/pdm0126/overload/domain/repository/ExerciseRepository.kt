package com.pdm0126.overload.domain.repository

import com.pdm0126.overload.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>
    suspend fun getExerciseById(id: String): ExerciseEntity?
}