package com.pdm0126.overload.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import com.pdm0126.overload.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.overload.domain.TechnicalDictionary
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.ui.components.Error
import com.pdm0126.overload.ui.components.OverloadScaffold

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory),
    isSelectionMode: Boolean = false,
    isAnalysisMode: Boolean = false,
    onBackClick: () -> Unit = {},
    onExerciseClick: (String) -> Unit,
    onExerciseSelect: (Exercise) -> Unit = {},
    onExerciseAnalysisSelect: (Exercise) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var showFilterSheet by remember { mutableStateOf(false) }

    OverloadScaffold(
        title = "Ejercicios",
        showBackButton = isSelectionMode || isAnalysisMode,
        onBackClick = { onBackClick() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SecondaryTabRow(
                selectedTabIndex = state.selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Tab(
                    selected = state.selectedTabIndex == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("Guardados") }

                )
                Tab(
                    selected = state.selectedTabIndex == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("Explorar") }
                )
            }

            val isLocal = state.selectedTabIndex == 0
            TextField(
                value = state.query,
                onValueChange = {  viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp ),
                placeholder = {
                    Text(
                        text = if (isLocal) "Tu biblioteca" else "Buscar ejercicio",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                              },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                              },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { showFilterSheet = true }) {
                            val isFilterActive = state.selectedMuscles != emptyList<String?>() || state.selectedMechanic != null
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtros",
                                tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (state.query.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onSearchQueryChanged(""); focusManager.clearFocus() }
                            ) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = "Limpiar búsqueda",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (!isLocal) {
                            viewModel.searchRemoteExercises()
                            focusManager.clearFocus()
                        }
                    },
                    onDone = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                )
            )

            ActiveFiltersRow(
                selectedMuscles = state.selectedMuscles,
                selectedMechanic = state.selectedMechanic,
                onRemoveMuscle = { muscle ->
                    viewModel.onMuscleFilterSelected(muscle)
                },
                onRemoveMechanic = {
                    viewModel.onMechanicFilterSelected(null)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))


            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isLocal) {
                    if (state.localExercises.isEmpty()) {
                        Text(
                            text = "No tienes ejercicios guardados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.localExercises, key = { it.id }) { exercise ->
                                ExerciseCard(
                                    exercise = exercise,
                                    isSelectionMode = isSelectionMode,
                                    isAnalysisMode = isAnalysisMode,
                                    onExerciseClick = { onExerciseClick(exercise.id) },
                                    onSelectClick = { onExerciseSelect(exercise) },
                                    onAnalysisClick = { onExerciseAnalysisSelect(exercise) }
                                )
                                ItemDivider()
                            }
                        }
                    }
                } else {
                    when {
                        state.remoteState.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        state.remoteState.errorMessage != null -> {
                            Error(
                                onRetryClick = { viewModel.searchRemoteExercises() },
                                error = state.remoteState.errorMessage
                            )
                        }

                        state.remoteState.results.isEmpty() && state.query.isBlank() -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Busca un ejercicio",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.remoteState.results, key = { it.id }) { exercise ->
                                    ExerciseCard(
                                        exercise = exercise,
                                        isSelectionMode = isSelectionMode,
                                        isAnalysisMode = isAnalysisMode,
                                        onExerciseClick = { onExerciseClick(exercise.id) },
                                        onSelectClick = {
                                            viewModel.addExercise(exercise)
                                            onExerciseSelect(exercise)
                                        },
                                        onAnalysisClick = {
                                            viewModel.addExercise(exercise)
                                            onExerciseAnalysisSelect(exercise)
                                        }
                                    )
                                    ItemDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showFilterSheet) {
            ExerciseFilterBottomSheet(
                selectedMuscles = state.selectedMuscles,
                selectedMechanic = state.selectedMechanic,
                onMuscleSelect = { viewModel.onMuscleFilterSelected(it) },
                onMechanicSelect = { viewModel.onMechanicFilterSelected(it) },
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseFilterBottomSheet(
    selectedMuscles: List<String>,
    selectedMechanic: String?,
    onMuscleSelect: (String?) -> Unit,
    onMechanicSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (selectedMuscles.isNotEmpty() || selectedMechanic != null) {
                    TextButton(onClick = {
                        onMuscleSelect(null)
                        onMechanicSelect(null)
                    }) {
                        Text("Limpiar todo", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Grupo Muscular",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val muscles = TechnicalDictionary.mainMuscleGroupsList
                muscles.forEach { muscle ->
                    FilterChip(
                        selected = selectedMuscles.contains(muscle),
                        onClick = { onMuscleSelect(muscle) },
                        label = { Text(muscle) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mecánica",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mechanics = listOf("Compuesto", "Aislamiento")
                mechanics.forEach { mechanic ->
                    FilterChip(
                        selected = selectedMechanic == mechanic,
                        onClick = { onMechanicSelect(if (selectedMechanic == mechanic) null else mechanic) },
                        label = {
                            Text(
                                text = mechanic,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    isSelectionMode: Boolean,
    isAnalysisMode: Boolean,
    onExerciseClick: () -> Unit,
    onSelectClick: () -> Unit,
    onAnalysisClick : (Exercise) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExerciseClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = exercise.remoteImages.firstOrNull(),
            contentDescription = exercise.name,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${exercise.muscleGroup.replaceFirstChar { it.uppercase() }} • ${exercise.mechanic}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelectionMode) {
            IconButton(onClick = onSelectClick) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = "Seleccionar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else if (isAnalysisMode) {
            IconButton(onClick = { onAnalysisClick(exercise) }) {
                Icon(
                    painter = painterResource(id = R.drawable.search_insights_24px),
                    contentDescription = "Analizar",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        else {
            IconButton(onClick = onExerciseClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Ver más",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveFiltersRow(
    selectedMuscles: List<String>,
    selectedMechanic: String?,
    onRemoveMuscle: (String) -> Unit,
    onRemoveMechanic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasFilters = selectedMuscles.isNotEmpty() || selectedMechanic != null

    AnimatedVisibility(
        visible = hasFilters,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(selectedMuscles) { muscle ->
                InputChip(
                    selected = true,
                    onClick = { onRemoveMuscle(muscle) },
                    label = {
                        Text(
                            text = muscle,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quitar filtro $muscle",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = null
                )
            }

            selectedMechanic?.let { mechanic ->
                item {
                    InputChip(
                        selected = true,
                        onClick = { onRemoveMechanic() },
                        label = {
                            Text(
                                text = mechanic,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Quitar filtro $mechanic",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null
                    )
                }
            }
        }
    }
}

@Composable
fun ItemDivider() {
    Spacer(modifier = Modifier.height(4.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    Spacer(modifier = Modifier.height(4.dp))
}