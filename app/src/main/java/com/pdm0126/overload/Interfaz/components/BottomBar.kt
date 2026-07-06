package com.pdm0126.overload.Interfaz.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.pdm0126.overload.Routes


private val BarBackground = Color(0xFF161616)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)

data class TopLevelDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Routes
)

val topLevelDestinations = listOf(
    TopLevelDestination(
        "Entrenar",
        Icons.Filled.FitnessCenter,
        Icons.Outlined.FitnessCenter,
        Routes.Training
    ),
    TopLevelDestination(
        "Rutinas",
        Icons.AutoMirrored.Filled.List,
        Icons.AutoMirrored.Outlined.List,
        Routes.Routine
    ),
    TopLevelDestination(
        "Biblioteca",
        Icons.AutoMirrored.Filled.MenuBook,
        Icons.AutoMirrored.Outlined.MenuBook,
        Routes.Library
    ),
    TopLevelDestination(
        "Análisis",
        Icons.Filled.Analytics,
        Icons.Outlined.Analytics,
        Routes.Analysis
    )
)

@Composable
fun BottomBar(
    currentDestination: Any?,
    onNavigate: (Routes) -> Unit
) {
    val isTopLevel =
        topLevelDestinations.any { destination -> destination.route == currentDestination }
    if (!isTopLevel) return

    NavigationBar(
        containerColor = BarBackground,
        contentColor = Color.White
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
                    selectedIconColor = Color.Black,
                    selectedTextColor = GoldAccent,
                    indicatorColor = GoldAccent,
                    unselectedIconColor = TextGray,
                    unselectedTextColor = TextGray
                )
            )
        }
    }
}