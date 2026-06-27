package com.pdm0126.overload.data

import android.content.Context
import com.pdm0126.overload.data.local.OverloadDatabase
import com.pdm0126.overload.data.remote.ExerciseApiClient
import com.pdm0126.overload.data.repository.AnalysisRepositoryImp
import com.pdm0126.overload.data.repository.ExerciseRepositoryImp
import com.pdm0126.overload.data.repository.RoutineRepositoryImp
import com.pdm0126.overload.data.repository.WorkoutRepositoryImp
import com.pdm0126.overload.domain.repository.AnalysisRepository
import com.pdm0126.overload.domain.repository.ExerciseRepository
import com.pdm0126.overload.domain.repository.RoutineRepository
import com.pdm0126.overload.domain.repository.WorkoutRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class OverloadProvider(context: Context) {

    // Scope global necesario para el callback de población inicial de Room
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val appDatabase = OverloadDatabase.getDatabase(context, applicationScope)

    private val exerciseDao = appDatabase.exerciseDao()
    private val routineDao = appDatabase.routineDao()
    private val workoutDao = appDatabase.workoutDao()
    private val analysisDao = appDatabase.analysisDao()

    private val exerciseApiClient = ExerciseApiClient()

    private val exerciseRepository: ExerciseRepository = ExerciseRepositoryImp(exerciseDao, exerciseApiClient)
    private val routineRepository: RoutineRepository = RoutineRepositoryImp(routineDao)
    private val workoutRepository: WorkoutRepository = WorkoutRepositoryImp(workoutDao)
    private val analysisRepository: AnalysisRepository = AnalysisRepositoryImp(analysisDao)

    // Repartimos las dependencias
    fun provideExerciseRepository(): ExerciseRepository {
        return exerciseRepository
    }

    fun provideRoutineRepository(): RoutineRepository {
        return routineRepository
    }

    fun provideWorkoutRepository(): WorkoutRepository {
        return workoutRepository
    }
    fun provideAnalysisRepository(): AnalysisRepository {
        return analysisRepository
    }
}