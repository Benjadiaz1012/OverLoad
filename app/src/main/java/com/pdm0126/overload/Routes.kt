package com.pdm0126.overload

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Routes : NavKey{
    @Serializable
    data object SignIn: Routes()
@Serializable
    data object System : Routes()
@Serializable
    data object Training : Routes(){

    }
@Serializable
    data object Analysis : Routes(){

    }
@Serializable
    data object Routine : Routes(){}
@Serializable
    data object  Library : Routes(){}}