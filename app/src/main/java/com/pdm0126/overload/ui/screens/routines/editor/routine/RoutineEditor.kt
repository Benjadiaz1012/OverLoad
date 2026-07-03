package com.pdm0126.overload.ui.screens.routines.editor.routine

import androidx.compose.foundation.BorderStroke
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

    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteRoutineDialog by rememberSaveable { mutableStateOf(false) }
    var showActivateRoutineDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDayDialog by rememberSaveable { mutableStateOf(false) }
    var dayToDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var dayToDeleteName by rememberSaveable { mutableStateOf("") }

    OverloadScaffold(
        title = "Editar Rutina",
        showBackButton = true,
        onBackClick = onBackClick,
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Eliminar Rutina", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showMenu = false
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
                    DropdownMenuItem(
                        text = { Text("Renombrar Rutina", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showMenu = false
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
                    if (!microcycle.isActive) {
                        DropdownMenuItem(
                            text = { Text("Activar Rutina", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                showMenu = false
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
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)) {
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
                                text = "Sistema Base: ${microcycle.blueprintType}\nLongitud: ${microcycle.days.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (microcycle.isActive) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.StarOutline,
                                contentDescription = "Activa",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    /*Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                    }
*/
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
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToDayEditor(day.dayId) }
                            .padding(vertical = 12.dp),
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

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onNavigateToDayEditor(day.dayId) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Día",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    dayToDeleteId = day.dayId
                                    dayToDeleteName = day.focus
                                    showDeleteDayDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Borrar Día",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }

            if (microcycle.days.size < 9) {
                item {
                    OutlinedButton(
                        onClick = { viewModel.addDay() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir Día")
                    }
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