package com.pdm0126.overload.data.mapper

import com.pdm0126.overload.data.local.entity.WorkoutSessionEntity
import com.pdm0126.overload.data.local.entity.WorkoutSetEntity
import com.pdm0126.overload.data.local.relation.WorkoutSessionWithSets
import com.pdm0126.overload.domain.model.WorkoutSession
import com.pdm0126.overload.domain.model.WorkoutSet

fun WorkoutSetEntity.toDomainModel(): WorkoutSet {
    return WorkoutSet(
        setId = setId,
        sessionId = sessionId,
        plannedExerciseId = plannedExerciseId,
        exerciseId = exerciseId,
        setNumber = setNumber,
        weightKg = weightKg,
        reps = reps,
        rir = rir,
        isRirEnabled = isRirEnabled,
        rirFactor = rirFactor
    )
}

fun WorkoutSessionEntity.toDomainModel(sets: List<WorkoutSet> = emptyList()): WorkoutSession {
    return WorkoutSession(
        sessionId = sessionId,
        dayId = dayId,
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        sets = sets
    )
}

fun WorkoutSessionWithSets.toDomainModel(): WorkoutSession {
    return WorkoutSession(
        sessionId = session.sessionId,
        dayId = session.dayId,
        startTimestamp = session.startTimestamp,
        endTimestamp = session.endTimestamp,
        sets = sets.map { it.toDomainModel() }.sortedWith(compareBy({ it.plannedExerciseId }, { it.setNumber }))
    )
}