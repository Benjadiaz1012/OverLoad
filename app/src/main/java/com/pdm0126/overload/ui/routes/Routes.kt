package com.pdm0126.overload.ui.routes

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Routes : NavKey {
    @Serializable
    data object Dashboard : Routes()
    @Serializable
    data object Routines : Routes()
    @Serializable
    data object Library : Routes()
    @Serializable
    data object Analysis : Routes()
}