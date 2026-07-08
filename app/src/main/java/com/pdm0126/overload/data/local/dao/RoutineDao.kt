package com.pdm0126.overload.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pdm0126.overload.data.local.entity.RoutineDayEntity
import com.pdm0126.overload.data.local.entity.RoutineEntity
import com.pdm0126.overload.data.local.entity.PlannedExerciseEntity
import com.pdm0126.overload.data.local.relation.RoutineDayWithExercises
import com.pdm0126.overload.data.local.relation.RoutineWithDays
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineDay(day: RoutineDayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedExercise(exercise: PlannedExerciseEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Update
    suspend fun updateRoutineDay(day: RoutineDayEntity)

    @Query("UPDATE planned_exercises_table SET targetReps = :targetReps WHERE plannedExerciseId = :plannedExerciseId")
    suspend fun updateTargetReps(plannedExerciseId: Long, targetReps: Int?)

    @Query("DELETE FROM planned_exercises_table WHERE plannedExerciseId = :plannedExerciseId")
    suspend fun deletePlannedExercise(plannedExerciseId: Long?)

    @Query("UPDATE planned_exercises_table SET targetSets = :targetSets WHERE plannedExerciseId = :plannedExerciseId")
    suspend fun updateTargetSets(plannedExerciseId: Long, targetSets: Int)

    @Query("DELETE FROM routine_days_table WHERE dayId = :dayId")
    suspend fun deleteRoutineDay(dayId: Long)

    @Query("UPDATE routine_days_table SET focus = :newFocus WHERE dayId = :dayId")
    suspend fun updateDayFocus(dayId: Long, newFocus: String)
    @Query("UPDATE routines_table SET name = :newName WHERE routineId = :routineId")
    suspend fun updateRoutineName(routineId: Long, newName: String)
    @Query("DELETE FROM routines_table WHERE routineId = :routineId")
    suspend fun deleteRoutine(routineId: Long)

    @Transaction
    @Query("SELECT * FROM routines_table WHERE isActive = 1 LIMIT 1")
    fun getActiveRoutine(): Flow<RoutineWithDays?>

    @Transaction
    @Query("SELECT * FROM routines_table")
    fun getAllRoutines(): Flow<List<RoutineWithDays>>

    @Transaction
    @Query("SELECT * FROM routines_table WHERE routineId = :routineId")
    fun getRoutineById(routineId: Long): Flow<RoutineWithDays?>

    @Transaction
    @Query("SELECT * FROM routine_days_table WHERE dayId = :dayId LIMIT 1")
    fun getDayWithPlannedExercises(dayId: Long?): Flow<RoutineDayWithExercises?>

    @Transaction
    suspend fun updateActiveRoutine(routineId: Long) {
        clearAllActiveRoutines()
        setActiveRoutineById(routineId)
    }

    @Query("UPDATE routines_table SET isActive = 0")
    suspend fun clearAllActiveRoutines()

    @Query("UPDATE routines_table SET isActive = 1 WHERE routineId = :routineId")
    suspend fun setActiveRoutineById(routineId: Long)
}