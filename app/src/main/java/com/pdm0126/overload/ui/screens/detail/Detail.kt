package com.pdm0126.overload.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.ui.components.Error
import com.pdm0126.overload.ui.components.OverloadScaffold
import kotlinx.coroutines.delay

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

    OverloadScaffold(
        title = state.exercise?.name ?: "Detalles",
        showBackButton = true,
        onBackClick = onBackClick
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

@Composable
fun ExerciseDetailContent(exercise: Exercise) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. El "Motor GIF" Eficiente
        item {
            AnimatedExerciseImage(
                imageUrls = exercise.remoteImages,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        // Metadata Principal: Grupo muscular general, mecánica y músculos sinergistas
        item {
            Column {
                Text(
                    text = "Clasificación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(text = exercise.muscleGroup.replaceFirstChar { it.uppercase() }) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(exercise.mechanic.replaceFirstChar { it.uppercase() }) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
        if (exercise.targetMuscles.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Musculos objetivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exercise.targetMuscles.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (exercise.secondaryMuscles.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Musculos sinergistas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exercise.secondaryMuscles.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (exercise.equipments.isNotEmpty()) {
            item {
                Column {
                    Text(text = "Equipamiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exercise.equipments.joinToString(", ") { it.replaceFirstChar { char -> char.uppercase() } },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (exercise.instructions.isNotEmpty()) {
            item {
                Text(
                    text = "Instrucciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            itemsIndexed(exercise.instructions) { index, instruction ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        color = MaterialTheme.colorScheme.onSurface
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

    // Estado que guarda qué imagen mostrar (0 o 1)
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