package com.pdm0126.overload

import com.pdm0126.overload.data.local.entity.ExerciseEntity
import com.pdm0126.overload.data.local.entity.WorkoutSessionEntity
import com.pdm0126.overload.data.local.entity.WorkoutSetEntity
import com.pdm0126.overload.data.local.relation.DayWithSlots
import com.pdm0126.overload.data.local.relation.MicrocycleWithDays
import com.pdm0126.overload.data.local.relation.SessionWithSets
import com.pdm0126.overload.data.local.relation.SlotWithExercise
import com.pdm0126.overload.data.mapper.toDomainModel
import com.pdm0126.overload.data.remote.dto.ExerciseDto
import com.pdm0126.overload.domain.TechnicalDictionary
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.model.RoutineSlot
import com.pdm0126.overload.domain.model.WorkoutSession
import com.pdm0126.overload.domain.model.WorkoutSet

fun com.pdm0126.overload.data.local.entity.ExerciseEntity.toDomainModel() : Exercise {
    return Exercise(
        id = exerciseId,
        name = name,
        muscleGroup = mainMuscleGroup,
        mechanic = mechanic,
        targetMuscles = targetMuscles,
        secondaryMuscles = secondaryMuscles,
        equipment = equipment,
        instructions = instructions,
        remoteImages = remoteImagesUrls
    )
}

// Para guardar ejercicios externos
fun Exercise.toEntity() : com.pdm0126.overload.data.local.entity.ExerciseEntity {
    return ExerciseEntity(
        exerciseId = id,
        name = name,
        mainMuscleGroup = muscleGroup,
        mechanic = mechanic,
        targetMuscles = targetMuscles,
        secondaryMuscles = secondaryMuscles,
        equipment = equipment,
        instructions = instructions,
        remoteImagesUrls = remoteImages
    )
}

fun ExerciseDto.toDomainModel(): Exercise {
    val baseUrl = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"
    val generatedId = "ex_${name.trim().lowercase().replace(" ", "_").replace("-", "_")}"

    return Exercise(
        id = generatedId,
        name = name,
        muscleGroup = TechnicalDictionary.getGeneralGroup(primaryMuscles.first()),
        mechanic = TechnicalDictionary.getMechanic(mechanic),
        targetMuscles = primaryMuscles.map { muscle -> TechnicalDictionary.getSpecificMuscle(muscle) },
        secondaryMuscles = secondaryMuscles.map {muscle -> TechnicalDictionary.getSpecificMuscle(muscle) },
        equipment = TechnicalDictionary.getEquipment(equipment),
        instructions = instructions,
        remoteImages = images.map { "$baseUrl$it" }
    )
}

/*-------------------------------------------------------------------------------------*/


/*fun SlotWithExercise.toDomainModel(): RoutineSlot {
    return RoutineSlot(
        slotId = slot.slotId,
        order = slot.order,
        targetSets = slot.targetSets,
        exercise = exercise.toDomainModel()
    )
}

fun DayWithSlots.toDomainModel(): RoutineDay {
    return RoutineDay(
        dayId = day.dayId,
        order = day.order,
        focus = day.focus,
        slots = slots.map { it.toDomainModel() }.sortedBy { it.order }
    )
}

fun MicrocycleWithDays.toDomainModel(): RoutineMicrocycle {
    return RoutineMicrocycle(
        microcycleId = microcycle.microcycleId,
        name = microcycle.name,
        blueprintType = microcycle.blueprintType,
        isActive = microcycle.isActive,
        // Ordenamos los días cronológicamente
        days = days.map { it.toDomainModel() }.sortedBy { it.order }
    )
}*/


/*-------------------------------------------------------------------------------------*/

fun WorkoutSetEntity.toDomainModel(): WorkoutSet {
    return WorkoutSet(
        setId = setId,
        sessionId = sessionId,
        slotId = slotId,
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

fun SessionWithSets.toDomainModel(): WorkoutSession {
    return WorkoutSession(
        sessionId = session.sessionId,
        dayId = session.dayId,
        startTimestamp = session.startTimestamp,
        endTimestamp = session.endTimestamp,
        // Ordenamos por slot y luego por número de serie para una lectura lógica
        sets = sets.map { it.toDomainModel() }.sortedWith(compareBy({ it.slotId }, { it.setNumber }))
    )
}