package com.pdm0126.overload.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pdm0126.overload.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(exercises: List<ExerciseEntity>)
    @Query("SELECT * FROM exercises_table")
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    @Query("SELECT * FROM exercises_table WHERE mainMuscleGroup = :muscleGroup")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>
    @Query("SELECT * FROM exercises_table WHERE exerciseId = :id LIMIT 1")
    fun getExerciseById(id: String): ExerciseEntity?
}