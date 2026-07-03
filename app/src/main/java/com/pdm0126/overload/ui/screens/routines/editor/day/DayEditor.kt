package com.pdm0126.overload.ui.screens.routines.editor.day

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pdm0126.overload.ui.components.OverloadConfirmDialog
import com.pdm0126.overload.ui.components.OverloadInputDialog
import com.pdm0126.overload.ui.components.OverloadScaffold

@Composable
fun DayEditorScreen(
    dayId: Long,
    viewModel: DayEditorViewModel = viewModel(
        factory = DayEditorViewModel.provideFactory(dayId),
        key = dayId.toString()
    ),
    onBackClick: () -> Unit,
    onNavigateToLibrarySelection: () -> Unit,
    onNavigateToExerciseDetail: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val day = state.day
    LaunchedEffect(day) {
        if (!state.isLoading && day == null) {
            onBackClick()
        }
    }

    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteExerciseDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDayDialog by rememberSaveable { mutableStateOf(false) }
    var slotToDeleteId by rememberSaveable { mutableLongStateOf(0) }
    var newDayName by rememberSaveable { mutableStateOf("") }
    var showMenu by rememberSaveable { mutableStateOf(false) }

    OverloadScaffold(
        title = state.day?.focus ?: "Cargando...",
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
                        text = { Text("Eliminar Dia", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showMenu = false
                            showDeleteDayDialog = true
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
                        text = { Text("Renombrar Día", color = MaterialTheme.colorScheme.onSurface) },
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
                }
            }
        },
        floatingActionButton = {
            if (state.day != null && state.day!!.slots.size < 12) {
                FloatingActionButton(
                    onClick = onNavigateToLibrarySelection,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.day?.slots.isNullOrEmpty()) {
                Text(
                    text = "Agrega tu primer ejercicio.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.day!!.slots, key = { _, slot -> slot.slotId }) { index, slot ->
                        var expandedDropdown by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onNavigateToExerciseDetail(slot.exercise.id) }
                                        .padding(end = 12.dp, top = 4.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(28.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = slot.exercise.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${slot.exercise.muscleGroup.replaceFirstChar { it.uppercase() }} • ${slot.exercise.mechanic}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    Box {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(shape = CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .clickable { expandedDropdown = true }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "x${slot.targetSets}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Cambiar series",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = expandedDropdown,
                                            onDismissRequest = { expandedDropdown = false }
                                        ) {
                                            (1..6).forEach { setAmount ->
                                                DropdownMenuItem(
                                                    text = { Text("$setAmount Series") },
                                                    onClick = {
                                                        viewModel.updateTargetSets(slot.slotId, setAmount)
                                                        expandedDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { showDeleteExerciseDialog = true ; slotToDeleteId = slot.slotId },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Borrar",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        if (showRenameDialog) {
            OverloadInputDialog(
                title = "Renombrar Día",
                initialValue = state.day?.focus ?: "",
                label = "Nuevo nombre",
                onConfirm = { newName ->
                    viewModel.updateDayName(newName)
                    showRenameDialog = false
                },
                onDismiss = { showRenameDialog = false },
            )
        }
        if (showDeleteExerciseDialog) {
            OverloadConfirmDialog(
                title = "Eliminar Ejercicio",
                text = "¿Estás seguro de que deseas eliminar este ejercicio?",
                confirmText = "Eliminar",
                dismissText = "Cancelar",
                isDestructive = true,
                icon = Icons.Default.DeleteOutline,
                onConfirm = { viewModel.removeSlot(slotToDeleteId) ; showDeleteExerciseDialog = false },
                onDismiss = { showDeleteExerciseDialog = false }
            )
        }
        if (showDeleteDayDialog) {
            OverloadConfirmDialog(
                title = "Eliminar Día",
                text = "¿Estás seguro de que deseas eliminar este día?",
                confirmText = "Eliminar",
                dismissText = "Cancelar",
                isDestructive = true,
                icon = Icons.Default.DeleteOutline,
                onConfirm = { viewModel.deleteDay() ; showDeleteDayDialog = false },
                onDismiss = { showDeleteDayDialog = false }
            )
        }
    }
}
