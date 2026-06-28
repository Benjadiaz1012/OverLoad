package com.pdm0126.overload.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pdm0126.overload.ui.routes.Routes
import com.pdm0126.overload.ui.components.OverloadScaffold
import com.pdm0126.overload.ui.screens.analysis.AnalysisScreen
import com.pdm0126.overload.ui.screens.analysis.AnalysisViewModel
import com.pdm0126.overload.ui.screens.dashboard.DashboardScreen
import com.pdm0126.overload.ui.screens.detail.DetailScreen
import com.pdm0126.overload.ui.screens.library.LibraryScreen
import com.pdm0126.overload.ui.screens.routines.BlueprintSelectionScreen
import com.pdm0126.overload.ui.screens.routines.RoutinesScreen
import com.pdm0126.overload.ui.screens.routines.editor.day.DayEditorScreen
import com.pdm0126.overload.ui.screens.routines.editor.day.DayEditorViewModel
import com.pdm0126.overload.ui.screens.routines.editor.routine.RoutineEditorScreen

@Composable
fun OverloadApp() {
    val backStack = rememberNavBackStack(Routes.Dashboard)
    val currentDestination = backStack.lastOrNull()

    // Scaffold global que solo maneja la barra inferior
    Scaffold(
        bottomBar = {
            OverloadBottomBar(
                currentDestination = currentDestination,
                onNavigate = { route ->
                    if (currentDestination != route) {
                        backStack.clear()
                        if (route != Routes.Dashboard) {
                            backStack.add(Routes.Dashboard)
                        }
                        backStack.add(route)
                    }
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            // Solo aplicamos el padding inferior, el padding superior lo manejara el OverloadScaffold de cada pantalla
            modifier = Modifier
                .padding(
                    bottom = innerPadding.calculateBottomPadding()
                ),
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<Routes.Dashboard> {
                    DashboardScreen()
                }
                entry<Routes.Routines> {
                    RoutinesScreen(
                        onNavigateToCreateRoutine = { backStack.add(Routes.BlueprintSelection) },
                        onNavigateToRoutineEditor = { microcycleId -> backStack.add(Routes.RoutineEditor(microcycleId)) }
                    )
                }

                entry<Routes.RoutineEditor> { entry ->
                    RoutineEditorScreen(
                        microcycleId = entry.microcycleId,
                        onBackClick = { backStack.removeLastOrNull() },
                        onNavigateToDayEditor = { dayId -> backStack.add(Routes.DayEditor(dayId)) }
                    )
                }

                entry<Routes.BlueprintSelection> {
                    BlueprintSelectionScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        onRoutineCreated = { backStack.removeLastOrNull() }
                    )
                }
                entry<Routes.Library> {
                    LibraryScreen(
                        onExerciseClick = { exerciseId -> backStack.add(Routes.Detail(exerciseId)) }
                    )
                }
                entry<Routes.Detail> { entry ->
                    DetailScreen(
                        exerciseId = entry.exerciseId,
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
                entry<Routes.DayEditor> { entry ->
                    DayEditorScreen(
                        dayId = entry.dayId,
                        onBackClick = { backStack.removeLastOrNull() },
                        onNavigateToLibrarySelection = {
                            backStack.add(Routes.LibrarySelection(entry.dayId))
                        },
                        onNavigateToExerciseDetail = { exerciseId ->
                            backStack.add(Routes.Detail(exerciseId))
                        }
                    )
                }

                entry<Routes.LibrarySelection> { entry ->
                    val dayEditorViewModel: DayEditorViewModel = viewModel(
                        factory = DayEditorViewModel.provideFactory(entry.dayId),
                        key = entry.dayId.toString()
                    )

                    LibraryScreen(
                        isSelectionMode = true,
                        onExerciseClick = { exerciseId -> backStack.add(Routes.Detail(exerciseId)) },

                        onExerciseSelect = { exercise ->
                            dayEditorViewModel.addExerciseToSlot(exercise.id)
                            backStack.removeLastOrNull()
                        }
                    )
                }
                entry<Routes.Analysis> {
                    AnalysisScreen(
                        onNavigateToLibrary = { backStack.add(Routes.LibraryAnalysisSelection) }
                    )
                }
                entry<Routes.LibraryAnalysisSelection> {
                    val analysisViewModel: AnalysisViewModel = viewModel(factory = AnalysisViewModel.Factory)
                    LibraryScreen(
                        isAnalysisMode = true,
                        onExerciseClick = { exerciseId -> backStack.add(Routes.Detail(exerciseId)) },
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

@Composable
fun PlaceholderScreen(title: String) {
    OverloadScaffold(
        title = title,
        showBackButton = false
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Contenido de $title")
        }
    }
}

