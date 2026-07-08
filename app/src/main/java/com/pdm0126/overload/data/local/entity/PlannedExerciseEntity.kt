package com.pdm0126.overload.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "planned_exercises_table",
    foreignKeys = [
        ForeignKey(
            entity = RoutineDayEntity::class,
            parentColumns = ["dayId"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayId"), Index("exerciseId")]
)
data class PlannedExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val plannedExerciseId: Long = 0,
    val dayId: Long,
    val exerciseId: String,
    val order: Int,
    val targetSets: Int,
    val targetReps: Int? = null
)

