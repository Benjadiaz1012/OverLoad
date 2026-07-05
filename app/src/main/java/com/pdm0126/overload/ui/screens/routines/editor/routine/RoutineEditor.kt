package com.pdm0126.overload.ui.screens.routines.editor.routine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.ui.components.OverloadConfirmDialog
import com.pdm0126.overload.ui.components.OverloadInputDialog
import com.pdm0126.overload.ui.components.OverloadScaffold

@Composable
fun RoutineEditorScreen(
    microcycleId: Long,
    viewModel: RoutineEditorViewModel = viewModel(
        factory = RoutineEditorViewModel.provideFactory(microcycleId),
        key = microcycleId.toString()
    ),
    onBackClick: () -> Unit,
    onNavigateToDayEditor: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val microcycle = state.microcycle

    LaunchedEffect(microcycle) {
        if (!state.isLoading && microcycle == null) {
            onBackClick()
        }
    }

    if (microcycle == null) return

    var showTopBarMenu by rememberSaveable { mutableStateOf(false) }
    var menuDayId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteRoutineDialog by rememberSaveable { mutableStateOf(false) }
    var showActivateRoutineDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDayDialog by rememberSaveable { mutableStateOf(false) }
    var dayToDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var dayToDeleteName by rememberSaveable { mutableStateOf("") }
    val isWorkoutSessionActive = state.isWorkoutSessionActive

    OverloadScaffold(
        title = "Editar Rutina",
        showBackButton = true,
        onBackClick = onBackClick,
        actions = {
            Box {
                IconButton(onClick = { showTopBarMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = showTopBarMenu,
                    onDismissRequest = { showTopBarMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Renombrar Rutina", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showTopBarMenu = false
                            showRenameDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DriveFileRenameOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                    if (!microcycle.isActive && !isWorkoutSessionActive) {
                        DropdownMenuItem(
                            text = { Text("Activar Rutina", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                showTopBarMenu = false
                                showActivateRoutineDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.StarOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Eliminar Rutina", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showTopBarMenu = false
                            showDeleteRoutineDialog = true
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (microcycle.days.size < 9) {
                FloatingActionButton(
                    onClick = { viewModel.addDay() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Día")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = microcycle.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sistema Base: ${microcycle.blueprintType}\nDuración: ${microcycle.days.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (microcycle.isActive) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Activa",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Editar Días",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            itemsIndexed(microcycle.days, key = { _, day -> day.dayId }) { index, day ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDayEditor(day.dayId) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(28.dp)
                        )

                        Column(modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)) {
                            Text(
                                text = day.focus,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${day.slots.size} ejercicios",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box {
                            IconButton(onClick = { menuDayId = day.dayId }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Opciones",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = menuDayId == day.dayId,
                                onDismissRequest = { menuDayId = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar Día", color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = { onNavigateToDayEditor(day.dayId) ; menuDayId = null },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar Día",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Borrar Día", color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        dayToDeleteId = day.dayId
                                        dayToDeleteName = day.focus
                                        showDeleteDayDialog = true
                                        menuDayId = null
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Borrar Día",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }

                                )

                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
        if (showRenameDialog) {

            OverloadInputDialog(
                title = "Renombrar Rutina",
                initialValue = microcycle.name,
                label = "Nuevo nombre",
                confirmText = "Guardar",
                dismissText = "Cancelar",
                onConfirm = { newName -> viewModel.renameRoutine(newName)
                    showRenameDialog = false },
                onDismiss = { showRenameDialog = false }
            )
        }

        if (showDeleteRoutineDialog) {
            OverloadConfirmDialog(
                title = "Eliminar Rutina",
                text = "Esta acción borrará todos sus días y ejercicios asignados. No se puede deshacer",
                confirmText = "Eliminar",
                dismissText = "Cancelar",
                isDestructive = true,
                icon = Icons.Default.DeleteOutline,
                onConfirm = { viewModel.deleteRoutine() ; showDeleteRoutineDialog = false },
                onDismiss = { showDeleteRoutineDialog = false }
            )
        }

        if (showDeleteDayDialog && dayToDeleteId != null) {
            OverloadConfirmDialog(
                title = "Eliminar Día",
                text = "Esta acción borrará todos los ejercicios asignados a ${dayToDeleteName}. No se puede deshacer",
                confirmText = "Eliminar",
                dismissText = "Cancelar",
                isDestructive = true,
                icon = Icons.Default.DeleteOutline,
                onConfirm = { viewModel.deleteDay(dayToDeleteId!!) ; showDeleteDayDialog = false },
                onDismiss = { showDeleteDayDialog = false }
            )
        }
        if (showActivateRoutineDialog) {
            OverloadConfirmDialog(
                title = "Activar Rutina",
                text = "¿Estás seguro de que deseas activar esta rutina? Solo puede haber una activa a la vez",
                confirmText = "Activar",
                dismissText = "Cancelar",
                icon = Icons.Default.Star,
                onConfirm = { viewModel.setActive() ; showActivateRoutineDialog = false },
                onDismiss = { showActivateRoutineDialog = false }
            )
        }
    }
}