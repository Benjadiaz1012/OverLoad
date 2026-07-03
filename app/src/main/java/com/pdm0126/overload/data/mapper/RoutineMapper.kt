package com.pdm0126.overload.data.mapper

import com.pdm0126.overload.data.local.relation.DayWithSlots
import com.pdm0126.overload.data.local.relation.MicrocycleWithDays
import com.pdm0126.overload.data.local.relation.SlotWithExercise
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import com.pdm0126.overload.domain.model.RoutineSlot

fun SlotWithExercise.toDomainModel(): RoutineSlot {
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
}

