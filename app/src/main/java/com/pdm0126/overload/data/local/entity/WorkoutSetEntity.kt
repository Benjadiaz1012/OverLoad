package com.pdm0126.overload.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets_table",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlannedExerciseEntity::class,
            parentColumns = ["plannedExerciseId"],
            childColumns = ["plannedExerciseId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("plannedExerciseId"), Index("exerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val setId: Long = 0,
    val sessionId: Long,
    val plannedExerciseId: Long?,
    val exerciseId: String,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val rir: Int? = null,
    val isRirEnabled: Boolean,
    val rirFactor: Float
)
