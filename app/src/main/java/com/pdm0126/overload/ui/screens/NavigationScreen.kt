package com.pdm0126.overload.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pdm0126.overload.routes.Routes

data class TopLevelDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Routes
)

@Composable
fun MainScreen() {
    val backStack = rememberNavBackStack(Routes.Dashboard)

    val destinations = listOf(
        TopLevelDestination("Entrenar", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, Routes.Dashboard),
        TopLevelDestination("Rutinas", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List, Routes.Routines),
        TopLevelDestination("Librería", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, Routes.Library),
        TopLevelDestination("Análisis", Icons.Filled.Analytics, Icons.Outlined.Analytics, Routes.Analysis)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                // Leemos el destino superior actual de la pila
                val currentDestination = backStack.lastOrNull()

                destinations.forEach { destination ->
                    val selected = currentDestination == destination.route

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                // Limpiamos la pila para no acumular vistas infinitamente
                                backStack.clear()
                                // Si no vamos al inicio, agregamos el inicio como base, y luego la nueva ruta
                                // Esto asegura que si el usuario presiona el botón "atrás" del celular, regrese al Dashboard sin dar vueltas
                                if (destination.route != Routes.Dashboard) {
                                    backStack.add(Routes.Dashboard)
                                }
                                backStack.add(destination.route)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title
                            )
                        },
                        label = { Text(destination.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<Routes.Dashboard> {
                    PlaceholderScreen("Dashboard (Entrenamiento / Sesión Activa)")
                }
                entry<Routes.Routines> {
                    PlaceholderScreen("Mis Rutinas (Configuración)")
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

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}

