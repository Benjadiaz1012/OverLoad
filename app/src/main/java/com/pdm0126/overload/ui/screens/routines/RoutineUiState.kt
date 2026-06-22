package com.pdm0126.overload.ui.screens.routines

import com.pdm0126.overload.domain.model.Blueprint
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import java.util.UUID

// Esta clase representa un día en memoria RAM. Usamos UUID para que Compose no se confunda
// si el usuario borra o cambia el orden de los días antes de guardarlos.
data class DraftDay(
    val tempId: String = UUID.randomUUID().toString(),
    val focus: String
)

data class RoutinesUiState(
    // Estado Consolidado (Lo que viene de Room)
    val savedMicrocycles: List<RoutineMicrocycle> = emptyList(),
    val activeMicrocycleId: Long? = null,

    // Estado de Creación (El Borrador en RAM)
    val isCreating: Boolean = false,
    val draftName: String = "",
    val selectedBlueprint: Blueprint? = null,
    val draftDays: List<DraftDay> = emptyList()
)

