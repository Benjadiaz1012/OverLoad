package com.pdm0126.overload.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.overload.ui.components.ExerciseNavCache
import com.pdm0126.overload.ui.components.OverloadTopBar
import com.pdm0126.overload.domain.TechnicalDictionary
import com.pdm0126.overload.domain.model.Exercise

enum class ExerciseCardMode { DEFAULT, SELECTION, ANALYSIS }

private val muscleGroupOptions = TechnicalDictionary.mainMuscleGroupsList
private val mechanicOptions = listOf("Compuesto", "Aislamiento")

private const val MAX_SEARCH_QUERY_LENGTH = 40

@Composable
fun Library(
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory),
    isSelectionMode: Boolean = false,
    isAnalysisMode: Boolean = false,
    onBackClick: () -> Unit = {},
    onExerciseClick: (Exercise) -> Unit = {},
    onExerciseSelect: (Exercise) -> Unit = {},
    onExerciseAnalysisSelect: (Exercise) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showFilterSheet by remember { mutableStateOf(false) }

    LibraryContent(
        state = uiState,
        isSelectionMode = isSelectionMode,
        isAnalysisMode = isAnalysisMode,
        showFilterSheet = showFilterSheet,
        onBackClick = onBackClick,
        onTabSelected = viewModel::onTabSelected,
        onQueryChange = viewModel::onSearchQueryChanged,
        onSearchSubmit = {
            viewModel.searchRemoteExercises()
            focusManager.clearFocus()
        },
        onOpenFilterSheet = { showFilterSheet = true },
        onDismissFilterSheet = { showFilterSheet = false },
        onMuscleFilterToggle = viewModel::onMuscleFilterSelected,
        onMechanicFilterToggle = viewModel::onMechanicFilterSelected,
        onExerciseClick = { exercise ->
            ExerciseNavCache.put(exercise)
            onExerciseClick(exercise)
        },
        onExerciseSelect = { exercise ->
            if (uiState.selectedTabIndex == 1) viewModel.addExercise(exercise)
            onExerciseSelect(exercise)
        },
        onExerciseAnalysisSelect = { exercise ->
            if (uiState.selectedTabIndex == 1) viewModel.addExercise(exercise)
            onExerciseAnalysisSelect(exercise)
        },
        onRetrySearch = { viewModel.searchRemoteExercises() }
    )
}

@Composable
private fun LibraryContent(
    state: LibraryUiState,
    isSelectionMode: Boolean,
    isAnalysisMode: Boolean,
    showFilterSheet: Boolean,
    onBackClick: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onOpenFilterSheet: () -> Unit,
    onDismissFilterSheet: () -> Unit,
    onMuscleFilterToggle: (String?) -> Unit,
    onMechanicFilterToggle: (String?) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseSelect: (Exercise) -> Unit,
    onExerciseAnalysisSelect: (Exercise) -> Unit,
    onRetrySearch: () -> Unit
) {
    val cardMode = when {
        isSelectionMode -> ExerciseCardMode.SELECTION
        isAnalysisMode -> ExerciseCardMode.ANALYSIS
        else -> ExerciseCardMode.DEFAULT
    }
    val isLocal = state.selectedTabIndex == 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            OverloadTopBar(
                title = "Ejercicios",
                showBackButton = isSelectionMode || isAnalysisMode,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PillTabSelector(
                selectedIndex = state.selectedTabIndex,
                onSelect = onTabSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchField(
                query = state.query,
                onQueryChange = onQueryChange,
                onSearchSubmit = onSearchSubmit,
                placeholder = if (isLocal) "Tu biblioteca" else "Buscar ejercicio",
                isFilterActive = state.selectedMuscles.isNotEmpty() || state.selectedMechanic != null,
                onFilterClick = onOpenFilterSheet
            )

            ActiveFiltersRow(
                selectedMuscles = state.selectedMuscles,
                selectedMechanic = state.selectedMechanic,
                onRemoveMuscle = { muscle -> onMuscleFilterToggle(muscle) },
                onRemoveMechanic = { onMechanicFilterToggle(null) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLocal) {
                    SavedExercisesContent(
                        exercises = state.localExercises,
                        cardMode = cardMode,
                        onExerciseClick = onExerciseClick,
                        onExerciseSelect = onExerciseSelect,
                        onExerciseAnalysisSelect = onExerciseAnalysisSelect
                    )
                } else {
                    ExploreContent(
                        remoteState = state.remoteState,
                        query = state.query,
                        cardMode = cardMode,
                        onExerciseClick = onExerciseClick,
                        onExerciseSelect = onExerciseSelect,
                        onExerciseAnalysisSelect = onExerciseAnalysisSelect,
                        onRetry = onRetrySearch
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        ExerciseFilterSheet(
            selectedMuscles = state.selectedMuscles,
            selectedMechanic = state.selectedMechanic,
            onMuscleToggle = onMuscleFilterToggle,
            onMechanicToggle = onMechanicFilterToggle,
            onClearAll = {
                onMuscleFilterToggle(null)
                onMechanicFilterToggle(null)
            },
            onDismiss = onDismissFilterSheet
        )
    }
}

@Composable
private fun PillTabSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val tabs = listOf("Guardados", "Explorar")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    placeholder: String,
    isFilterActive: Boolean,
    onFilterClick: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = { newValue ->
            if (newValue.length <= MAX_SEARCH_QUERY_LENGTH) {
                onQueryChange(newValue)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(14.dp)),
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filtros",
                        tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Limpiar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun ActiveFiltersRow(
    selectedMuscles: List<String>,
    selectedMechanic: String?,
    onRemoveMuscle: (String) -> Unit,
    onRemoveMechanic: () -> Unit
) {
    val hasFilters = selectedMuscles.isNotEmpty() || selectedMechanic != null
    if (!hasFilters) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(selectedMuscles) { muscle ->
            ActiveFilterChip(label = muscle, onRemove = { onRemoveMuscle(muscle) })
        }
        selectedMechanic?.let { mechanic ->
            item {
                ActiveFilterChip(label = mechanic, onRemove = onRemoveMechanic)
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(label: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Quitar filtro $label",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(14.dp)
                .clickable { onRemove() }
        )
    }
}

@Composable
private fun SavedExercisesContent(
    exercises: List<Exercise>,
    cardMode: ExerciseCardMode,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseSelect: (Exercise) -> Unit,
    onExerciseAnalysisSelect: (Exercise) -> Unit
) {
    if (exercises.isEmpty()) {
        Text(
            text = "No tienes ejercicios guardados",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    } else {
        ExerciseList(
            exercises = exercises,
            cardMode = cardMode,
            onExerciseClick = onExerciseClick,
            onExerciseSelect = onExerciseSelect,
            onExerciseAnalysisSelect = onExerciseAnalysisSelect
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreContent(
    remoteState: RemoteState,
    query: String,
    cardMode: ExerciseCardMode,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseSelect: (Exercise) -> Unit,
    onExerciseAnalysisSelect: (Exercise) -> Unit,
    onRetry: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = onRetry,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            remoteState.isLoading && remoteState.results.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            remoteState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = remoteState.errorMessage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Reintentar",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { onRetry() }
                    )
                }
            }

            remoteState.results.isEmpty() && query.isBlank() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.TravelExplore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Busca un ejercicio",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp)
                    )
                }
            }

            else -> {
                ExerciseList(
                    exercises = remoteState.results,
                    cardMode = cardMode,
                    onExerciseClick = onExerciseClick,
                    onExerciseSelect = onExerciseSelect,
                    onExerciseAnalysisSelect = onExerciseAnalysisSelect
                )
            }
        }
    }
}

@Composable
private fun ExerciseList(
    exercises: List<Exercise>,
    cardMode: ExerciseCardMode,
    onExerciseClick: (Exercise) -> Unit,
    onExerciseSelect: (Exercise) -> Unit,
    onExerciseAnalysisSelect: (Exercise) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(exercises, key = { it.id }) { exercise ->
            ExerciseCard(
                exercise = exercise,
                mode = cardMode,
                onClick = { onExerciseClick(exercise) },
                onSelectClick = { onExerciseSelect(exercise) },
                onAnalysisClick = { onExerciseAnalysisSelect(exercise) }
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    mode: ExerciseCardMode,
    onClick: () -> Unit,
    onSelectClick: () -> Unit,
    onAnalysisClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = exercise.remoteImages.firstOrNull()
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = exercise.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = exercise.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MuscleTag(text = exercise.muscleGroup)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = exercise.mechanic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                }
            }

            when (mode) {
                ExerciseCardMode.SELECTION -> {
                    IconButton(onClick = onSelectClick) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Seleccionar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                ExerciseCardMode.ANALYSIS -> {
                    IconButton(onClick = onAnalysisClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Analizar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                ExerciseCardMode.DEFAULT -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Ver más",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MuscleTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.replaceFirstChar { it.uppercase() },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExerciseFilterSheet(
    selectedMuscles: List<String>,
    selectedMechanic: String?,
    onMuscleToggle: (String?) -> Unit,
    onMechanicToggle: (String?) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp)
                )
                if (selectedMuscles.isNotEmpty() || selectedMechanic != null) {
                    Text(
                        text = "Limpiar todo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { onClearAll() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Grupo Muscular",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                muscleGroupOptions.forEach { option ->
                    val selected = option in selectedMuscles
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onMuscleToggle(option) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = option,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mecánica",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                mechanicOptions.forEach { mechanic ->
                    val isSelected = mechanic == selectedMechanic
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onMechanicToggle(mechanic) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mechanic,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}