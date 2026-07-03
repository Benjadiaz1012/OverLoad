package com.pdm0126.overload

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.pdm0126.overload.data.local.entity.DayEntity
import com.pdm0126.overload.data.local.entity.ExerciseEntity
import com.pdm0126.overload.data.local.entity.MicrocycleEntity
import com.pdm0126.overload.data.local.entity.SlotEntity
import com.pdm0126.overload.data.local.entity.WorkoutSessionEntity
import com.pdm0126.overload.data.local.entity.WorkoutSetEntity
import com.pdm0126.overload.data.local.relation.SlotWithExercise
import kotlinx.serialization.Serializable

// Entities / tablas

@Entity(
    tableName = "days_table",
    foreignKeys = [
        ForeignKey(
            entity = MicrocycleEntity::class,
            parentColumns = ["microcycleId"],
            childColumns = ["microcycleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("microcycleId")]
)
data class DayEntity(
    @PrimaryKey(autoGenerate = true)
    val dayId: Long = 0,
    val microcycleId: Long,
    val order: Int,         // Dato posicional (Día 1, Día 2... Máximo 6)
    val focus: String       // Etiqueta visual elegida por el usuarip: "Push", "Pull", "Legs", "Arm Day", etc.
)

/*-------------------------------------------------------------------------------------*/

@Serializable
@Entity(tableName = "exercises_table")
data class ExerciseEntity(
    @PrimaryKey
    val exerciseId: String,
    val name: String,
    val mainMuscleGroup: String,
    val mechanic: String,
    val targetMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val equipment: String,
    val instructions: List<String>,
    val remoteImagesUrls: List<String>
)



/*-------------------------------------------------------------------------------------*/

@Entity(
    tableName = "slots_table",
    foreignKeys = [
        ForeignKey(
            entity = DayEntity::class,
            parentColumns = ["dayId"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE // Si se remueve un ejercicio, se limpia el slot
        )
    ],
    indices = [Index("dayId"), Index("exerciseId")]
)
data class SlotEntity(
    @PrimaryKey(autoGenerate = true)
    val slotId: Long = 0,
    val dayId: Long,
    val exerciseId: String,   // Referencia directa a cualquier ejercicio del catálogo
    val order: Int,           // Orden secuencial dentro del día (1 a 12)
    val targetSets: Int       // Objetivo de series (entre 1 y 6)
)

/*-------------------------------------------------------------------------------------*/


@Entity(tableName = "microcycles_table")
data class MicrocycleEntity(
    @PrimaryKey(autoGenerate = true)
    val microcycleId: Long = 0,
    val name: String,           // ej. "Rutina de Fuerza - 2026"
    val blueprintType: String,  // ej. "PPL", "Arnold Split" (Solo informativo para la UI)
    val isActive: Boolean = false
)

/*-------------------------------------------------------------------------------------*/

@Entity(
    tableName = "workout_sessions_table",
    foreignKeys = [
        ForeignKey(
            entity = DayEntity::class,
            parentColumns = ["dayId"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayId")]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Long = 0,
    val dayId: Long,                // El día del microciclo que se está ejecutando
    val startTimestamp: Long,       // Momento de inicio de la sesión (epoch ms)
    val endTimestamp: Long? = null  // Momento de fin; null si la sesión sigue activa
)


/*-------------------------------------------------------------------------------------*/


@Entity(
    tableName = "workout_sets_table",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE // Si borras la sesión, sí se borran sus series
        ),
        ForeignKey(
            entity = SlotEntity::class,
            parentColumns = ["slotId"],
            childColumns = ["slotId"],
            onDelete = ForeignKey.SET_NULL // Si borran el slot, la serie sobrevive
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE // Protege la integridad del ejercicio
        )
    ],
    indices = [Index("sessionId"), Index("slotId"), Index("exerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val setId: Long = 0,
    val sessionId: Long,        // Sesión a la que pertenece esta serie
    val slotId: Long?,          // Slot de rutina que se está ejecutando
    val exerciseId: String,       // Ejercicio único al que pertenece esta serie
    val setNumber: Int,         // Número ordinal de la serie dentro del slot (1, 2, 3…)
    val weightKg: Float,        // Peso utilizado en kilogramos
    val reps: Int,              // Repeticiones completadas
    val rir: Int? = null,       // Repeticiones en reserva (0–5); null si RIR está desactivado
    val isRirEnabled: Boolean,  // true = el usuario midió el RIR; false = reps fijas
    val rirFactor: Float        // Factor calculado y persistido para uso directo en el Módulo 4
)


/*-------------------------------------------------------------------------------------*/
// Relations

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


/*-------------------------------------------------------------------------------------*/
data class SessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val sets: List<WorkoutSetEntity>
)