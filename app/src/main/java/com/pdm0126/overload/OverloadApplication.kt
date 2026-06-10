package com.pdm0126.overload

import android.app.Application
import com.pdm0126.overload.data.local.OverloadDatabase
import com.pdm0126.overload.data.remote.ExerciseApiClient
import com.pdm0126.overload.data.repository.ExerciseRepositoryImp
import com.pdm0126.overload.domain.repository.ExerciseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class OverloadApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { OverloadDatabase.getDatabase(this, applicationScope) }

    val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepositoryImp(database.exerciseDao(), ExerciseApiClient())
    }
}

