package com.pdm0126.overload.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.pdm0126.overload.ui.routes.Routes

data class TopLevelDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Routes
)

val topLevelDestinations = listOf(
    TopLevelDestination("Entrenar", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, Routes.Dashboard),
    TopLevelDestination("Rutinas", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List, Routes.Routines),
    TopLevelDestination("Biblioteca", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, Routes.Library),
    TopLevelDestination("Análisis", Icons.Filled.Analytics, Icons.Outlined.Analytics, Routes.Analysis)
)

@Composable
fun OverloadBottomBar(
    currentDestination: Any?,
    onNavigate: (Routes) -> Unit
) {
    // Cuando la ruta actual no sea un "Top Level Destination", ocultamos la barra (ej. Sesión activa o personalización de microciclo, día de entrenamiento, etcétera)
    val isTopLevel = topLevelDestinations.any { destination -> destination.route == currentDestination }
    if (!isTopLevel) return

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        topLevelDestinations.forEach { destination ->
            val selected = currentDestination == destination.route

            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination.route) },
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

