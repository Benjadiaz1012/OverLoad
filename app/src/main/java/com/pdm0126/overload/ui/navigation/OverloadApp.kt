package com.pdm0126.overload.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.google.firebase.auth.FirebaseAuth
import com.pdm0126.overload.ui.components.OverloadBottomBar
import com.pdm0126.overload.ui.components.OverloadConfirmDialog
import com.pdm0126.overload.ui.screens.activeWorkout.ActiveWorkout
import com.pdm0126.overload.ui.screens.activeWorkout.ActiveWorkoutViewModel
import com.pdm0126.overload.ui.screens.analysis.Analysis
import com.pdm0126.overload.ui.screens.analysis.AnalysisViewModel
import com.pdm0126.overload.ui.screens.detail.Detail
import com.pdm0126.overload.ui.screens.library.Library
import com.pdm0126.overload.ui.screens.routines.Routines
import com.pdm0126.overload.ui.screens.routines.dayEditor.DayEditor
import com.pdm0126.overload.ui.screens.routines.dayEditor.DayEditorViewModel
import com.pdm0126.overload.ui.screens.routines.routineEditor.RoutineEditor
import com.pdm0126.overload.ui.screens.signin.SignIn
import com.pdm0126.overload.ui.screens.system.SystemViewModel
import com.pdm0126.overload.ui.screens.system.TrainingSystem
import com.pdm0126.overload.ui.screens.training.Training

@Composable
fun OverloadApp() {
    val startDestination = remember {
        if (FirebaseAuth.getInstance().currentUser != null) {
            Routes.Training
        } else {
            Routes.SignIn
        }
    }

    val backStack = rememberNavBackStack(startDestination)
    val currentDestination = backStack.lastOrNull()

    val context = LocalContext.current
    val activity = context as? Activity

    var showExitDialog by remember { mutableStateOf(false) }

    val isMainDestination =
        currentDestination == Routes.Training ||
                currentDestination == Routes.Routines ||
                currentDestination == Routes.Library ||
                currentDestination == Routes.Analysis

    BackHandler(enabled = isMainDestination) {
        showExitDialog = true
    }

    Scaffold(
        bottomBar = {
            OverloadBottomBar(
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
                .consumeWindowInsets(
                    PaddingValues(bottom = innerPadding.calculateBottomPadding())
                ),
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                } else {
                    showExitDialog = true
                }
            },
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
                            blueprint?.let {
                                systemViewModel.createRoutineFromBlueprint(it)
                            }
                        },
                        onNext = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                entry<Routes.Training> {
                    Training(
                        onSessionStarted = {
                            backStack.add(Routes.ActiveWorkout)
                        },
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            backStack.clear()
                            backStack.add(Routes.SignIn)
                        }
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
                        onCreateRoutine = {
                            backStack.add(Routes.System)
                        },
                        onOpenRoutine = { routine ->
                            backStack.add(Routes.RoutineEditor(routine.routineId))
                        }
                    )
                }

                entry<Routes.RoutineEditor> { route ->
                    RoutineEditor(
                        microcycleId = route.microcycleId,
                        onBack = {
                            backStack.removeLastOrNull()
                        },
                        onOpenDay = { dayId ->
                            backStack.add(Routes.DayEditor(dayId))
                        },
                        onRoutineDeleted = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                entry<Routes.DayEditor> { route ->
                    DayEditor(
                        dayId = route.dayId,
                        onBack = {
                            backStack.removeLastOrNull()
                        },
                        onNavigateToLibrarySelection = {
                            backStack.add(Routes.LibrarySelection(route.dayId))
                        },
                        onNavigateToExerciseDetail = { exerciseId ->
                            backStack.add(Routes.Detail(exerciseId))
                        },
                        onDayDeleted = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                entry<Routes.LibrarySelection> { route ->
                    val dayEditorViewModel: DayEditorViewModel = viewModel(
                        factory = DayEditorViewModel.provideFactory(route.dayId),
                        key = route.dayId.toString()
                    )

                    Library(
                        isSelectionMode = true,
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },
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
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                entry<Routes.Analysis> {
                    Analysis(
                        onNavigateToLibrary = {
                            backStack.add(Routes.LibraryAnalysisSelection)
                        }
                    )
                }

                entry<Routes.LibraryAnalysisSelection> {
                    val analysisViewModel: AnalysisViewModel =
                        viewModel(factory = AnalysisViewModel.Factory)

                    Library(
                        isAnalysisMode = true,
                        onBackClick = {
                            backStack.removeLastOrNull()
                        },
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
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            }
        )
    }

    if (showExitDialog) {
        OverloadConfirmDialog(
            title = "Salir de Overload",
            text = "¿Seguro que quieres salir?",
            confirmText = "Salir",
            dismissText = "Cancelar",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            onConfirm = {
                showExitDialog = false
                activity?.finish()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }
}