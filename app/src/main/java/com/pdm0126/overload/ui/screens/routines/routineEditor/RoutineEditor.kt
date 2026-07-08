package com.pdm0126.overload.ui.screens.routines.routineEditor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.ui.components.OverloadConfirmDialog
import com.pdm0126.overload.ui.components.OverloadInputDialog
import com.pdm0126.overload.ui.components.OverloadTopBar
import com.pdm0126.overload.domain.model.RoutineDay

@Composable
fun RoutineEditor(
    microcycleId: Long,
    onBack: () -> Unit = {},
    onOpenDay: (Long) -> Unit = {},
    onRoutineDeleted: () -> Unit = {}
) {
    val viewModel: RoutineEditorViewModel = viewModel(
        factory = RoutineEditorViewModel.provideFactory(microcycleId),
        key = microcycleId.toString()
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var dayToDelete by remember { mutableStateOf<RoutineDay?>(null) }

    val microcycle = uiState.routine

    LaunchedEffect(uiState.isLoading, microcycle) {
        if (!uiState.isLoading && microcycle == null) {
            onBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OverloadTopBar(
                title = microcycle?.name ?: "Editar rutina",
                showBackButton = true,
                onBackClick = onBack,
                trailingContent = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Renombrar rutina") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showRenameDialog = true
                                }
                            )
                            if (microcycle != null && !microcycle.isActive && !uiState.isWorkoutSessionActive) {
                                DropdownMenuItem(
                                    text = { Text("Marcar como activa") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.setActive()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Eliminar rutina") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            microcycle == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontró la rutina.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        RoutineNameField(
                            name = microcycle.name,
                            isActive = microcycle.isActive
                        )
                    }

                    if (microcycle.isActive && uiState.isWorkoutSessionActive) {
                        item {
                            Text(
                                text = "Tienes un entrenamiento en curso con esta rutina.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    items(microcycle.days, key = { it.dayId }) { day ->
                        DayRow(
                            day = day,
                            enabled = true,
                            onClick = { onOpenDay(day.dayId) },
                            onDelete = { dayToDelete = day }
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = viewModel::addDay,
                            enabled = microcycle.days.size < 9,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Agregar día",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        OverloadInputDialog(
            title = "Renombrar rutina",
            initialValue = microcycle?.name ?: "",
            label = "Nombre",
            onConfirm = { newName ->
                viewModel.renameRoutine(newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showDeleteDialog) {
        OverloadConfirmDialog(
            title = "Eliminar rutina",
            text = "Se eliminará esta rutina y todos sus días. Esta acción no se puede deshacer.",
            confirmText = "Eliminar",
            dismissText = "Cancelar",
            isDestructive = true,
            icon = Icons.Default.DeleteOutline,
            onConfirm = {
                viewModel.deleteRoutine()
                showDeleteDialog = false
                onRoutineDeleted()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    dayToDelete?.let { day ->
        OverloadConfirmDialog(
            title = "Eliminar día",
            text = "Esta acción borrará todos los ejercicios asignados a \"${day.focus}\". No se puede deshacer.",
            confirmText = "Eliminar",
            dismissText = "Cancelar",
            isDestructive = true,
            icon = Icons.Default.DeleteOutline,
            onConfirm = {
                viewModel.deleteDay(day.dayId)
                dayToDelete = null
            },
            onDismiss = { dayToDelete = null }
        )
    }
}

@Composable
private fun RoutineNameField(
    name: String,
    isActive: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isActive) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rutina activa",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp)
        )
    }
}

@Composable
private fun DayRow(
    day: RoutineDay,
    enabled: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.focus,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${day.plannedExercises.size} ejercicios",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            }

            IconButton(onClick = onDelete, enabled = enabled) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar día",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Editar día",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}