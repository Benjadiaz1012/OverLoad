package com.pdm0126.overload.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.pdm0126.overload.data.local.entity.DayEntity
import com.pdm0126.overload.data.local.entity.ExerciseEntity
import com.pdm0126.overload.data.local.entity.MicrocycleEntity
import com.pdm0126.overload.data.local.entity.SlotEntity

// Un Slot con la metadata completa de su ejercicio
data class SlotWithExercise(
    @Embedded val slot: SlotEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "exerciseId"
    )
    val exercise: ExerciseEntity
)

// Un día con todos sus Slots para ejercicios ordenados
data class DayWithSlots(
    @Embedded val day: DayEntity,
    @Relation(
        entity = SlotEntity::class,
        parentColumn = "dayId",
        entityColumn = "dayId"
    )
    val slots: List<SlotWithExercise>
)

// El microciclo final que contiene todos sus días ordenados
data class MicrocycleWithDays(
    @Embedded val microcycle: MicrocycleEntity,
    @Relation(
        entity = DayEntity::class,
        parentColumn = "microcycleId",
        entityColumn = "microcycleId"
    )
    val days: List<DayWithSlots>
)

