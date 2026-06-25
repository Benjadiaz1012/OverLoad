package com.pdm0126.overload.ui.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.overload.domain.TechnicalDictionary
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.ui.components.BookmarkedIcon
import com.pdm0126.overload.ui.components.Error
import com.pdm0126.overload.ui.components.OverloadScaffold
import com.pdm0126.overload.ui.components.UnBookmarkedIcon

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory),
    isSelectionMode: Boolean = false,
    onExerciseClick: (String) -> Unit,
    onExerciseSelect: (Exercise) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val lastWasBookmark = remember { mutableStateOf(true) }

    OverloadScaffold(
        title = "Ejercicios",
        showBackButton = false,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (lastWasBookmark.value) UnBookmarkedIcon() else BookmarkedIcon()

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
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
            OutlinedTextField(
                value = state.query,
                onValueChange = {  viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                              },
                trailingIcon = {
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
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )

            val muscleGroups = TechnicalDictionary.mainMuscleGroupsList
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(muscleGroups) { muscle ->
                    FilterChip(
                        selected = state.selectedMuscle == muscle,
                        onClick = { viewModel.onMuscleFilterSelected(muscle) },
                        label = { Text(muscle.replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

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
                                    /*isBookmarked = true,*/
                                    isSelectionMode = isSelectionMode,
                                    /*onBookmarkClick = {
                                        lastWasBookmark.value = true
                                        viewModel.toggleBookmark(exercise)
                                        coroutineScope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            snackbarHostState.showSnackbar(
                                                message = "Eliminado de tu biblioteca",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },*/
                                    onExerciseClick = { onExerciseClick(exercise.id) },
                                    onSelectClick = { onExerciseSelect(exercise) }
                                )
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
                            Text(
                                text = "...", // Mensaje de inicio o indicación de búsqueda vacía
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.remoteState.results, key = { it.id }) { exercise ->
                                    //val isBookmarked = state.localExercisesIds.contains(exercise.id)

                                    ExerciseCard(
                                        exercise = exercise,
                                        /*isBookmarked = isBookmarked,*/
                                        isSelectionMode = isSelectionMode,
                                        /*onBookmarkClick = {
                                            lastWasBookmark.value = isBookmarked
                                            viewModel.toggleBookmark(exercise)
                                            coroutineScope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                snackbarHostState.showSnackbar(
                                                    message = if (isBookmarked) "Eliminado de tu biblioteca" else "Agregado a tu biblioteca",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        },*/
                                        onExerciseClick = { onExerciseClick(exercise.id) },
                                        onSelectClick = {
                                            viewModel.addExercise(exercise)
                                            onExerciseSelect(exercise)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    /*isBookmarked: Boolean,*/
    isSelectionMode: Boolean,
    /*onBookmarkClick: () -> Unit,*/
    onExerciseClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            /*.clickable { onExerciseClick() }*/,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = exercise.remoteImages.firstOrNull(),
                contentDescription = exercise.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
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
            } else {
                IconButton(onClick = onExerciseClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Ver más",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                /*BookmarkButton(
                    isBookmarked = isBookmarked,
                    onCheckedChange = { onBookmarkClick() }
                )*/
            }
        }
    }
}