package com.pdm0126.overload.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pdm0126.overload.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    // Inserta la lista completa del JSON cuando la app se abre por primera vez
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    // Trae todos los ejercicios del catálogo
    @Query("SELECT * FROM exercises_table")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    // Filtra los ejercicios por grupo muscular
    @Query("SELECT * FROM exercises_table WHERE mainMuscleGroup = :muscleGroup")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>

    // Busca un ejercicio específico por su id
    @Query("SELECT * FROM exercises_table WHERE exerciseId = :id LIMIT 1")
    suspend fun getExerciseById(id: String): ExerciseEntity?
}