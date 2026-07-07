package com.pdm0126.overload.ui.screens.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.pdm0126.overload.ui.components.ConfirmDialog
import com.pdm0126.overload.ui.components.OverloadInfoDialog
import com.pdm0126.overload.ui.components.TopBar
import com.pdm0126.overload.domain.model.Exercise
import com.pdm0126.overload.domain.model.RoutineDay
import kotlinx.coroutines.launch

private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)
private val DividerGray = Color(0xFF2E2E2E)

@Composable
fun Training(
    viewModel: TrainingViewModel = viewModel(factory = TrainingViewModel.Factory),
    onCalendarClick: () -> Unit = {},
    onExerciseClick: (ExerciseDisplayItem) -> Unit = {},
    onSessionStarted: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDaySelector by remember { mutableStateOf(false) }
    var showStartConfirmDialog by remember { mutableStateOf(false) }
    var showEmptyWorkoutDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val hasAnyActiveSession = uiState.activeSessionDayId != null
    val isSessionActiveForThisDay = hasAnyActiveSession &&
            uiState.activeSessionDayId == uiState.day?.dayId

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            TrainingDrawerContent(
                onProfileClick = {
                    coroutineScope.launch { drawerState.close() }
                    onNavigateToProfile()
                }
            )
        }
    ) {
        Scaffold(
            containerColor = BackgroundDark,
            topBar = {
                TopBar(
                    title = "Entrenamiento",
                    leadingIcon = Icons.Default.Menu,
                    onLeadingClick = { coroutineScope.launch { drawerState.open() } },
                    leadingContentDescription = "Menú",
                    trailingIcon = Icons.Default.CalendarMonth,
                    onTrailingClick = onCalendarClick,
                    trailingContentDescription = "Calendario"
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
                ) {
                    Button(
                        onClick = {
                            when {
                                isSessionActiveForThisDay -> {
                                    onSessionStarted()
                                }

                                hasAnyActiveSession -> {
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(
                                            message = "Ya tienes otro entrenamiento en curso",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }

                                uiState.exercises.isEmpty() -> {
                                    showEmptyWorkoutDialog = true
                                }

                                else -> {
                                    showStartConfirmDialog = true
                                }
                            }
                        },
                        enabled = uiState.day != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                isSessionActiveForThisDay -> "Reanudar sesión"
                                hasAnyActiveSession -> "Otro entrenamiento en curso"
                                else -> "Iniciar Sesión de entrenamiento"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
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

                uiState.day == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes una rutina activa. Crea una desde el tab \"Rutinas\".",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            DaySelectorSection(
                                dayTitle = uiState.day?.focus.orEmpty(),
                                exerciseCount = uiState.exercises.size,
                                onClick = { showDaySelector = true }
                            )
                        }

                        items(uiState.exercises, key = { it.slot.slotId }) { item ->
                            ExerciseCard(
                                item = item,
                                onClick = { onExerciseClick(item) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }

    if (showDaySelector) {
        DaySelectorSheet(
            days = uiState.activeMicrocycle?.days ?: emptyList(),
            selectedDayId = uiState.day?.dayId,
            onSelectDay = { dayId ->
                viewModel.selectDay(dayId)
                showDaySelector = false
            },
            onDismiss = { showDaySelector = false }
        )
    }

    if (showStartConfirmDialog) {
        ConfirmDialog(
            title = "Iniciar Rutina",
            text = "¿Estás seguro de que deseas iniciar este entrenamiento?",
            confirmText = "Iniciar",
            dismissText = "Cancelar",
            icon = Icons.Default.FitnessCenter,
            onConfirm = {
                viewModel.startWorkout(onSessionStarted)
                showStartConfirmDialog = false
            },
            onDismiss = { showStartConfirmDialog = false }
        )
    }

    if (showEmptyWorkoutDialog) {
        OverloadInfoDialog(
            title = "Rutina sin ejercicios",
            text = "Esta rutina no tiene ejercicios programados. Debes añadir ejercicios desde el editor de rutinas.",
            icon = Icons.Default.FitnessCenter,
            onDismiss = { showEmptyWorkoutDialog = false }
        )
    }
}

@Composable
private fun TrainingDrawerContent(
    onProfileClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = CardDark
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "OVERLOAD",
            color = GoldAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = DividerGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text(text = "Perfil", color = Color.White) },
            icon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextGray) },
            selected = false,
            onClick = onProfileClick,
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
    }
}

@Composable
private fun DaySelectorSection(
    dayTitle: String,
    exerciseCount: Int,
    onClick: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Text(
                text = dayTitle,
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Cambiar día",
                tint = GoldAccent
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$exerciseCount ejercicios",
            color = TextGray,
            fontSize = 15.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DaySelectorSheet(
    days: List<RoutineDay>,
    selectedDayId: Long?,
    onSelectDay: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Elige un día",
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            days.forEach { day ->
                val isSelected = day.dayId == selectedDayId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectDay(day.dayId) }
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = day.focus,
                            color = if (isSelected) GoldAccent else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${day.slots.size} ejercicios",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = GoldAccent
                        )
                    }
                }

                if (day != days.last()) {
                    HorizontalDivider(color = DividerGray, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    item: ExerciseDisplayItem,
    onClick: () -> Unit
) {
    val exercise = item.slot.exercise
    val lastSet = item.lastSets.lastOrNull()

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                ExerciseThumbnail(exercise = exercise)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = exercise.muscleGroup,
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            if (lastSet != null) {
                Text(text = "Lo último realizado", color = TextGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ExerciseStat(
                        icon = Icons.Default.FitnessCenter,
                        value = "${lastSet.weightKg} kg",
                        label = "Peso",
                        modifier = Modifier.weight(1f)
                    )
                    ExerciseStat(
                        icon = Icons.Default.Repeat,
                        value = "${lastSet.reps} reps",
                        label = "Repeticiones",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(text = "Objetivo (sin registros previos)", color = TextGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ExerciseStat(
                        icon = Icons.Default.Repeat,
                        value = "${item.slot.targetSets} series",
                        label = "Series objetivo",
                        modifier = Modifier.weight(1f)
                    )
                    ExerciseStat(
                        icon = Icons.Default.FitnessCenter,
                        value = item.slot.targetReps?.let { "$it reps" } ?: "Al fallo",
                        label = "Repeticiones objetivo",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseThumbnail(exercise: Exercise) {
    val imageUrl = exercise.remoteImages.firstOrNull()

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2A2A)),
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
                imageVector = Icons.Default.Accessibility,
                contentDescription = exercise.name,
                tint = GoldAccent,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun ExerciseStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = value, color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = TextGray, fontSize = 12.sp)
    }
}