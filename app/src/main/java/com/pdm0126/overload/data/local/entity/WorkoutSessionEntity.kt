package com.pdm0126.overload.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions_table",
    foreignKeys = [
        ForeignKey(
            entity = RoutineDayEntity::class,
            parentColumns = ["dayId"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("dayId")]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0,
    val dayId: Long?,
    val startTimestamp: Long,
    val endTimestamp: Long? = null
)
