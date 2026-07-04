package com.pdm0126.overload.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AccessibleForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.ui.components.OverloadConfirmDialog
import com.pdm0126.overload.ui.components.OverloadScaffold

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
    onNavigateToActiveWorkout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = uiState.isLoading
    val activeMicrocycle = uiState.activeMicrocycle

    var startWorkoutDialog by rememberSaveable { mutableStateOf(false) }
    var workoutId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showEmptyWorkoutDialog by rememberSaveable { mutableStateOf(false) }

    OverloadScaffold(
        title = "Entrenamiento",
        showBackButton = false
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                activeMicrocycle == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.AccessibleForward,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Sin Rutinas activas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                                Text(
                                    text = "Plan actual",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = activeMicrocycle.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "¿Qué toca entrenar hoy?",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        itemsIndexed(activeMicrocycle.days, key = { _, day -> day.dayId }) { index, day ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { workoutId = day.dayId
                                            if (day.slots.isEmpty()) {
                                                showEmptyWorkoutDialog = true
                                            } else {
                                                startWorkoutDialog = true
                                            } }
                                        .padding(vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 12.dp)
                                    ) {
                                        Text(
                                            text = day.focus,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${day.slots.size} ejercicios programados",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Start,
                                        contentDescription = "Iniciar",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
        if (showEmptyWorkoutDialog) {
            AlertDialog(
                onDismissRequest = { showEmptyWorkoutDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                },
                title = {
                    Text(text = "Rutina sin ejercicios")
                },
                text = {
                    Text(text = "Esta rutina no tiene ejercicios programados. Debes añadir ejercicios desde el editor de rutinas")
                },
                confirmButton = {
                    TextButton(
                        onClick = { showEmptyWorkoutDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Entendido")
                    }
                }
            )
        }
        if (startWorkoutDialog) {
            OverloadConfirmDialog(
                title = "Iniciar Rutina",
                text = "¿Estás seguro de que deseas iniciar este entrenamiento?",
                confirmText = "Iniciar",
                dismissText = "Cancelar",
                icon = Icons.Default.FitnessCenter,
                onConfirm = {
                    viewModel.startWorkout(workoutId!!, onNavigateToActiveWorkout)
                    startWorkoutDialog = false },
                onDismiss = { startWorkoutDialog = false }
            )
        }
    }
}