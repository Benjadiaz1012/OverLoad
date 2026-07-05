package com.pdm0126.overload

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pdm0126.overload.screens.analysis.Analysis
import com.pdm0126.overload.screens.signin.SignIn
import com.pdm0126.overload.screens.system.TrainingSystem
import com.pdm0126.overload.screens.training.Training


@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Routes.SignIn)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Routes.SignIn> {
                SignIn(
                    onNext = { backStack.add(Routes.System) }
                )
            }
            entry<Routes.System> {
                TrainingSystem(
                    onNext = { backStack.add(Routes.Training) }
                )
            }
            entry<Routes.Training> {
                Training(
                    onNext = { backStack.add(Routes.Analysis) }
                )
            }
            entry<Routes.Analysis> {
                Analysis()
            }
        }
    )
}



