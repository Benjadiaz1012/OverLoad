package com.pdm0126.overload.ui.screens.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.ui.components.BookmarkButton
import com.pdm0126.overload.ui.components.BookmarkedIcon
import com.pdm0126.overload.ui.components.Error
import com.pdm0126.overload.ui.components.OverloadScaffold
import com.pdm0126.overload.ui.components.UnBookmarkedIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    exerciseId: String,
    viewModel: DetailViewModel = viewModel(
        key = exerciseId,
        factory = DetailViewModel.provideFactory(exerciseId = exerciseId)
    ),
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    OverloadScaffold(
        title = state.exercise?.name ?: "Detalles",
        showBackButton = true,
        onBackClick = onBackClick,
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
                        if (!state.isBookmarked) UnBookmarkedIcon() else BookmarkedIcon()

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        actions = {
            if (state.exercise != null) {
                BookmarkButton(
                    isBookmarked = state.isBookmarked,
                    onCheckedChange = {
                        viewModel.toggleBookmark()
                        coroutineScope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = if (state.isBookmarked) "Eliminado de tu biblioteca" else "Agregado a tu biblioteca",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.errorMessage != null -> {
                    Error(onRetryClick = { /* Opcional: recargar */ }, error = state.errorMessage)
                }
                state.exercise != null -> {
                    ExerciseDetailContent(exercise = state.exercise!!)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailContent(exercise: Exercise) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnimatedExerciseImage(
                imageUrls = exercise.remoteImages,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val mainEquipment = exercise.equipment.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Ninguno"

                    QuickStat(
                        icon = Icons.Default.Settings,
                        label = "Mecánica",
                        value = exercise.mechanic.replaceFirstChar { it.uppercase() }
                    )
                    QuickStat(
                        icon = Icons.Default.FitnessCenter,
                        label = "Equipo Principal",
                        value = mainEquipment
                    )
                }
            }
        }

        if (exercise.targetMuscles.isNotEmpty() || exercise.secondaryMuscles.isNotEmpty()) {
            item {
                Column {
                    SectionHeader(
                        icon = Icons.Default.AccessibilityNew, title = "Músculos implicados"
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        exercise.targetMuscles.forEach { muscle ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(muscle.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    labelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                border = null
                            )
                        }
                        exercise.secondaryMuscles.forEach { muscle ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(muscle.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }
        }

        if (exercise.equipment.size > 1) {
            item {
                Column {
                    SectionHeader(icon = Icons.Default.FitnessCenter, title = "Equipamiento detallado")
                    Spacer(modifier = Modifier.height(16.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        exercise.equipment.forEach { eq ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(eq.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }
        }

        if (exercise.instructions.isNotEmpty()) {
            item {
                InstructionsCard(instructions = exercise.instructions)
            }
        }
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun QuickStat(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InstructionsCard(instructions: List<String>) {
    var isExpanded by remember { mutableStateOf(false) }
    val threshold = 3
    val showToggleButton = instructions.size > threshold

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            SectionHeader(
                icon = Icons.Default.FormatListNumbered,
                title = "Instrucciones paso a paso"
            )

            Spacer(modifier = Modifier.height(16.dp))

            val instructionsToShow = if (isExpanded || !showToggleButton) instructions else instructions.take(threshold)

            instructionsToShow.forEachIndexed { index, instruction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = instruction,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showToggleButton) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (isExpanded) "Ocultar" else "Leer todos los pasos (${instructions.size})",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
@Composable
fun AnimatedExerciseImage(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    if (imageUrls.isEmpty()) {
        Box(
            modifier = modifier, contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sin imagen",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(imageUrls) {
        if (imageUrls.size > 1) {
            while (true) {
                delay(1200) // cambia imagen cada 1.2 segundos
                currentIndex = (currentIndex + 1) % imageUrls.size
            }
        }
    }

    AsyncImage(
        model = imageUrls[currentIndex],
        contentDescription = "Ejecución del ejercicio",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}