package com.pdm0126.overload

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pdm0126.overload.Interfaz.components.BottomBar
import com.pdm0126.overload.Interfaz.screens.activeWorkout.ActiveWorkout
import com.pdm0126.overload.Interfaz.screens.activeWorkout.ActiveWorkoutViewModel
import com.pdm0126.overload.Interfaz.screens.analysis.Analysis
import com.pdm0126.overload.Interfaz.screens.analysis.AnalysisViewModel
import com.pdm0126.overload.Interfaz.screens.detail.Detail
import com.pdm0126.overload.Interfaz.screens.library.Library
import com.pdm0126.overload.Interfaz.screens.routines.Routines
import com.pdm0126.overload.Interfaz.screens.dayEditor.DayEditor
import com.pdm0126.overload.Interfaz.screens.dayEditor.DayEditorViewModel
import com.pdm0126.overload.Interfaz.screens.routineEditor.RoutineEditor
import com.pdm0126.overload.Interfaz.screens.signin.SignIn
import com.pdm0126.overload.Interfaz.screens.system.SystemViewModel
import com.pdm0126.overload.Interfaz.screens.system.TrainingSystem
import com.pdm0126.overload.Interfaz.screens.training.Training

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Routes.SignIn)
    val currentDestination = backStack.lastOrNull()

    Scaffold(
        bottomBar = {
            BottomBar(
                currentDestination = currentDestination,
                onNavigate = { route ->
                    if (currentDestination != route) {
                        backStack.clear()
                        backStack.add(route)
                    }
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(PaddingValues(bottom = innerPadding.calculateBottomPadding())),
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {

                entry<Routes.SignIn> {
                    SignIn(
                        onNext = {
                            backStack.clear()
                            backStack.add(Routes.Training)
                        }
                    )
                }

                entry<Routes.System> {
                    val systemViewModel: SystemViewModel =
                        viewModel(factory = SystemViewModel.Factory)

                    TrainingSystem(
                        onConfirm = { blueprint ->
                            blueprint?.let { systemViewModel.createMicrocycleFromBlueprint(it) }
                        },
                        onNext = { backStack.removeLastOrNull() }
                    )
                }

                entry<Routes.Training> {
                    Training(
                        onSessionStarted = { backStack.add(Routes.ActiveWorkout) }
                    )
                }

                entry<Routes.ActiveWorkout> {
                    val viewModel: ActiveWorkoutViewModel =
                        viewModel(factory = ActiveWorkoutViewModel.Factory)
                    val isFinished by viewModel.isWorkoutFinished.collectAsStateWithLifecycle()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(isFinished) {
                        if (isFinished) {
                            viewModel.resetNavigation()
                            backStack.removeLastOrNull()
                        }
                    }

                    ActiveWorkout(
                        day = uiState.activeDay,
                        sessionSets = uiState.sessionSets,
                        lastSets = uiState.lastSets,
                        isLoading = uiState.isLoading,
                        onLogSet = viewModel::logSet,
                        onDeleteSet = viewModel::deleteSet,
                        onEndWorkout = viewModel::endWorkout,
                        onCancelWorkout = viewModel::cancelWorkout
                    )
                }

                entry<Routes.Routines> {
                    Routines(
                        onCreateRoutine = { backStack.add(Routes.System) },
                        onOpenRoutine = { microcycle ->
                            backStack.add(Routes.RoutineEditor(microcycle.microcycleId))
                        }
                    )
                }

                entry<Routes.RoutineEditor> { route ->
                    RoutineEditor(
                        microcycleId = route.microcycleId,
                        onBack = { backStack.removeLastOrNull() },
                        onOpenDay = { dayId -> backStack.add(Routes.DayEditor(dayId)) },
                        onRoutineDeleted = { backStack.removeLastOrNull() }
                    )
                }

                entry<Routes.DayEditor> { route ->
                    DayEditor(
                        dayId = route.dayId,
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToLibrarySelection = {
                            backStack.add(Routes.LibrarySelection(route.dayId))
                        },
                        onDayDeleted = { backStack.removeLastOrNull() }
                    )
                }

                entry<Routes.LibrarySelection> { route ->
                    val dayEditorViewModel: DayEditorViewModel = viewModel(
                        factory = DayEditorViewModel.provideFactory(route.dayId),
                        key = route.dayId.toString()
                    )

                    Library(
                        isSelectionMode = true,
                        onBackClick = { backStack.removeLastOrNull() },
                        onExerciseSelect = { exercise ->
                            dayEditorViewModel.addExerciseToSlot(exercise.id)
                            backStack.removeLastOrNull()
                        }
                    )
                }

                entry<Routes.Library> {
                    Library(
                        onExerciseClick = { exercise ->
                            backStack.add(Routes.Detail(exercise.id))
                        }
                    )
                }

                entry<Routes.Detail> { route ->
                    Detail(
                        exerciseId = route.exerciseId,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }

                entry<Routes.Analysis> {
                    Analysis(
                        onNavigateToLibrary = { backStack.add(Routes.LibraryAnalysisSelection) }
                    )
                }

                entry<Routes.LibraryAnalysisSelection> {
                    val analysisViewModel: AnalysisViewModel =
                        viewModel(factory = AnalysisViewModel.Factory)

                    Library(
                        isAnalysisMode = true,
                        onBackClick = { backStack.removeLastOrNull() },
                        onExerciseClick = { exercise ->
                            backStack.add(Routes.Detail(exercise.id))
                        },
                        onExerciseAnalysisSelect = { exercise ->
                            analysisViewModel.selectExercise(exercise.id)
                            backStack.removeLastOrNull()
                        }
                    )
                }
            },
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        )
    }
}
