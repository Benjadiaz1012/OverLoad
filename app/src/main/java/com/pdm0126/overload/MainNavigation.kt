package com.pdm0126.overload

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
import com.pdm0126.overload.Interfaz.screens.library.Library
import com.pdm0126.overload.Interfaz.screens.routines.Routines
import com.pdm0126.overload.Interfaz.screens.signin.SignIn
import com.pdm0126.overload.Interfaz.screens.system.SystemViewModel
import com.pdm0126.overload.Interfaz.screens.system.TrainingSystem
import com.pdm0126.overload.Interfaz.screens.training.Training


@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Routes.SignIn)
    Scaffold(
        bottomBar = {
            BottomBar(
                currentDestination = backStack.lastOrNull(),
                onNavigate = { route ->
                    backStack.add(route)
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<Routes.SignIn> {
                    SignIn(
                        onNext = { backStack.add(Routes.Training) }
                    )
                }
                entry<Routes.System> {
                    val viewModel: SystemViewModel = viewModel(factory = SystemViewModel.Factory)

                    TrainingSystem(
                        onConfirm = { blueprint ->
                            blueprint?.let { viewModel.createMicrocycleFromBlueprint(it) }
                        },
                    )
                }
                entry<Routes.Training> {
                    Training()
                }
                entry<Routes.Analysis> {
                    Analysis()
                }
                entry<Routes.Routine> {
                    Routines()
                }
                entry<Routes.Library> {
                    Library()
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}



