package com.pdm0126.overload.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.pdm0126.overload.data.local.entity.WorkoutSessionEntity
import com.pdm0126.overload.data.local.entity.WorkoutSetEntity

// Una sesión de entrenamiento con todas sus series registradas
data class WorkoutSessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val sets: List<WorkoutSetEntity>
)
