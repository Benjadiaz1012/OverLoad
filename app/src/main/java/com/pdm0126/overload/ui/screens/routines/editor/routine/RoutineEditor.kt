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

    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var newMicrocycleName by rememberSaveable { mutableStateOf("") }
    var showDeleteRoutineDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDayDialog by rememberSaveable { mutableStateOf(false) }
    var dayToDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var dayToDeleteName by rememberSaveable { mutableStateOf("") }

    OverloadScaffold(
        title = "Editar Rutina",
        showBackButton = true,
        onBackClick = onBackClick
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = microcycle.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                IconButton(
                                    onClick = {
                                        newMicrocycleName = microcycle.name
                                        showRenameDialog = true
                                    },
                                    modifier = Modifier.size(32.dp).padding(start = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Renombrar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
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
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Activa",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!microcycle.isActive) {
                            Button(
                                onClick = { viewModel.setActive() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Activar", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showDeleteRoutineDialog = true },
                            modifier = if (microcycle.isActive) Modifier.fillMaxWidth() else Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Eliminar", fontWeight = FontWeight.Bold)
                        }
                    }

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

                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
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
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Renombrar Rutina") },
                text = {
                    OutlinedTextField(
                        value = newMicrocycleName,
                        onValueChange = { newMicrocycleName = it },
                        label = { Text("Nuevo nombre") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.renameRoutine(newMicrocycleName)
                        showRenameDialog = false
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) { Text("Cancelar") }
                }
            )
        }

        if (showDeleteRoutineDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteRoutineDialog = false },
                title = { Text("Eliminar Rutina") },
                text = { Text("¿Estás seguro de que deseas eliminar \"${microcycle.name}\"? Esta acción borrará todos sus días y ejercicios asignados. No se puede deshacer.") },
                icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteRoutine()
                            showDeleteRoutineDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteRoutineDialog = false }) { Text("Cancelar") }
                }
            )
        }

        if (showDeleteDayDialog && dayToDeleteId != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDayDialog = false
                    dayToDeleteId = null
                },
                title = { Text("Eliminar Día") },
                text = { Text("¿Estás seguro de que deseas eliminar el día \"$dayToDeleteName\"? Todos los ejercicios asignados a este día se perderán de tu rutina.") },
                icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteDay(dayToDeleteId!!)
                            showDeleteDayDialog = false
                            dayToDeleteId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDayDialog = false
                        dayToDeleteId = null
                    }) { Text("Cancelar") }
                }
            )
        }
    }
}