package com.pdm0126.overload.Interfaz.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pdm0126.overload.Interfaz.components.ConfirmDialog
import com.pdm0126.overload.Interfaz.components.TopBar
import com.pdm0126.overload.domain.model.Exercise
import kotlinx.coroutines.launch

private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val FieldDark = Color(0xFF222222)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)

@Composable
fun Detail(
    exerciseId: String,
    onBack: () -> Unit = {}
) {
    val viewModel: DetailViewModel = viewModel(
        factory = DetailViewModel.provideFactory(exerciseId),
        key = exerciseId
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exercise = uiState.exercise

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showUnbookmarkDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = exercise?.name ?: "Detalle",
                showBackButton = true,
                onBackClick = onBack,
                trailingContent = {
                    if (exercise != null) {
                        IconButton(
                            onClick = {
                                if (uiState.isBookmarked) {
                                    if (uiState.isWorkoutSessionActive) {
                                        coroutineScope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            snackbarHostState.showSnackbar(
                                                message = "No puedes eliminar ejercicios mientras entrenas",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        showUnbookmarkDialog = true
                                    }
                                } else {
                                    viewModel.toggleBookmark()
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(
                                            message = "Agregado a tu biblioteca",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (uiState.isBookmarked) "Quitar de biblioteca" else "Guardar en biblioteca",
                                tint = GoldAccent
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = uiState.errorMessage ?: "", color = TextGray, fontSize = 14.sp)
                }
            }

            exercise != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    HeroImage(exercise = exercise)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoTag(text = exercise.muscleGroup)
                        InfoTag(text = exercise.mechanic)
                        InfoTag(text = exercise.equipment)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SectionTitle(text = "Músculos trabajados")
                    Spacer(modifier = Modifier.height(10.dp))
                    MuscleTagsRow(
                        primary = exercise.targetMuscles,
                        secondary = exercise.secondaryMuscles
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SectionTitle(text = "Instrucciones")
                    Spacer(modifier = Modifier.height(10.dp))
                    if (exercise.instructions.isEmpty()) {
                        Text(
                            text = "Este ejercicio aún no tiene instrucciones.",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    } else {
                        exercise.instructions.forEachIndexed { index, step ->
                            InstructionStep(number = index + 1, text = step)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showUnbookmarkDialog) {
        ConfirmDialog(
            title = "Eliminar de la biblioteca",
            text = "Si eliminas este ejercicio de tu biblioteca, desaparecerá de tus rutinas. ¿Quieres continuar?",
            confirmText = "Eliminar",
            dismissText = "Cancelar",
            isDestructive = true,
            icon = Icons.Default.DeleteOutline,
            onConfirm = {
                viewModel.toggleBookmark()
                showUnbookmarkDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Eliminado de tu biblioteca")
                }
            },
            onDismiss = { showUnbookmarkDialog = false }
        )
    }
}

@Composable
private fun HeroImage(exercise: Exercise) {
    val imageUrl = exercise.remoteImages.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FieldDark),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = exercise.name,
                tint = GoldAccent,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
private fun InfoTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CardDark)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text.replaceFirstChar { it.uppercase() },
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 17.sp)
}

@Composable
private fun MuscleTagsRow(primary: List<String>, secondary: List<String>) {
    Column {
        if (primary.isNotEmpty()) {
            TagsFlow(items = primary, isPrimary = true)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (secondary.isNotEmpty()) {
            TagsFlow(items = secondary, isPrimary = false)
        }
    }
}

@Composable
private fun TagsFlow(items: List<String>, isPrimary: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { muscle ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isPrimary) GoldAccent.copy(alpha = 0.15f) else FieldDark)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = muscle,
                    color = if (isPrimary) GoldAccent else TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun InstructionStep(number: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(50))
                .background(GoldAccent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}