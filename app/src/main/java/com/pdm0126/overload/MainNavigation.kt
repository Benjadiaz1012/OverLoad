package com.pdm0126.overload

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pdm0126.overload.Interfaz.components.BottomBar
import com.pdm0126.overload.Interfaz.screens.analysis.Analysis
import com.pdm0126.overload.Interfaz.screens.analysis.AnalysisViewModel
import com.pdm0126.overload.Interfaz.screens.library.Library
import com.pdm0126.overload.Interfaz.screens.routines.Routines
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
                    val systemViewModel: SystemViewModel = viewModel(factory = SystemViewModel.Factory)

                    TrainingSystem(
                        onConfirm = { blueprint ->
                            blueprint?.let { systemViewModel.createMicrocycleFromBlueprint(it) }
                        },
                        onNext = { backStack.removeLastOrNull() }
                    )
                }

                entry<Routes.Training> {
                    Training(
                        onSessionStarted = {
                        }
                    )
                }

                entry<Routes.Routines> {
                    Routines(
                        onCreateRoutine = { backStack.add(Routes.System) },
                        onOpenRoutine = { microcycle ->
                        }
                    )
                }

                entry<Routes.Library> {
                    Library(
                        onExerciseClick = { exercise ->
                        }
                    )
                }

                entry<Routes.Analysis> {
                    Analysis(
                        onNavigateToLibrary = { backStack.add(Routes.LibraryAnalysisSelection) }
                    )
                }

                entry<Routes.LibraryAnalysisSelection> {
                    val analysisViewModel: AnalysisViewModel = viewModel(factory = AnalysisViewModel.Factory)

                    Library(
                        isAnalysisMode = true,
                        onBackClick = { backStack.removeLastOrNull() },
                        onExerciseClick = { exercise ->
                        },
                        onExerciseAnalysisSelect = { exercise ->
                            analysisViewModel.selectExercise(exercise.id)
                            backStack.removeLastOrNull()
                        }
                    )
                }
            }
        )
    }
}


