package com.pdm0126.overload.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pdm0126.overload.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExerciseIgnore(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Transaction
    suspend fun saveExerciseSafely(exercise: ExerciseEntity) {
        val id = insertExerciseIgnore(exercise)
        if (id == -1L) {
            // Si retornó -1, significa que el ejercicio ya existía (quizás oculto)
            // Hacemos una simple actualización para poner isHidden = false y refrescar sus datos
            updateExercise(exercise)
        }
    }

    @Query("UPDATE exercises_table SET isHidden = 1 WHERE exerciseId = :exerciseId")
    suspend fun softDeleteExercise(exerciseId: String)

    @Query("SELECT * FROM exercises_table WHERE isHidden = 0")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises_table WHERE mainMuscleGroup = :muscleGroup")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises_table WHERE exerciseId = :id LIMIT 1")
    suspend fun getExerciseById(id: String): ExerciseEntity?
}