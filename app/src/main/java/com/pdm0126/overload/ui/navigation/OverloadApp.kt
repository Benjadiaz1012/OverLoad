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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pdm0126.overload.ui.routes.Routes
import com.pdm0126.overload.ui.components.OverloadScaffold

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
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<Routes.Dashboard> {
                    PlaceholderScreen("Dashboard (Entrenar)")
                }
                entry<Routes.Routines> {
                    PlaceholderScreen("Mis Rutinas")
                }
                entry<Routes.Library> {
                    PlaceholderScreen("Librería de Ejercicios")
                }
                entry<Routes.Analysis> {
                    PlaceholderScreen("Análisis y Progreso")
                }
            },
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        )
    }
}

// ejemplo de cómo se ve una pantalla usando el OverloadScaffold
@Composable
fun PlaceholderScreen(title: String) {
    OverloadScaffold(
        title = title,
        showBackButton = false // Sin boton de go back en los top level destinations
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

