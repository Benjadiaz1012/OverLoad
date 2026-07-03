package com.pdm0126.overload.ui.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Routes : NavKey {
    @Serializable
    data object Dashboard : Routes()
    @Serializable
    data object Routines : Routes()
    @Serializable
    data class RoutineEditor(val microcycleId: Long) : Routes()
    @Serializable
    data object Library : Routes()
    @Serializable
    data object BlueprintSelection : Routes()
    @Serializable
    data class Detail(val exerciseId: String) : Routes()

    @Serializable
    data class DayEditor(val dayId: Long) : Routes()

    @Serializable
    data class LibrarySelection(val dayId: Long) : Routes()
    @Serializable
    data object LibraryAnalysisSelection : Routes()
    @Serializable
    data object Analysis : Routes()
}