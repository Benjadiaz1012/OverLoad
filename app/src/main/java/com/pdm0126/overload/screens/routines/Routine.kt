package com.pdm0126.overload.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdm0126.overload.domain.model.RoutineDay
import com.pdm0126.overload.domain.model.RoutineMicrocycle
import kotlinx.coroutines.launch

private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)
private val DividerGray = Color(0xFF2E2E2E)
private val previewMicrocycles = listOf(
    RoutineMicrocycle(
        microcycleId = 1L,
        name = "Nuevo: Push / Pull / Legs",
        blueprintType = "Push / Pull / Legs",
        isActive = true,
        days = listOf(
            RoutineDay(dayId = 1L, order = 0, focus = "Push", slots = emptyList()),
            RoutineDay(dayId = 2L, order = 1, focus = "Pull", slots = emptyList()),
            RoutineDay(dayId = 3L, order = 2, focus = "Legs", slots = emptyList())
        )
    ),
    RoutineMicrocycle(
        microcycleId = 2L,
        name = "Arnold Split Verano",
        blueprintType = "Arnold Split",
        isActive = false,
        days = listOf(
            RoutineDay(dayId = 4L, order = 0, focus = "Pecho & Espalda", slots = emptyList()),
            RoutineDay(dayId = 5L, order = 1, focus = "Hombros & Brazos", slots = emptyList()),
            RoutineDay(dayId = 6L, order = 2, focus = "Piernas", slots = emptyList())
        )
    )
)

@Composable
fun Routines(
    microcycles: List<RoutineMicrocycle> = previewMicrocycles,
    isWorkoutSessionActive: Boolean = false,
    onCreateRoutine: () -> Unit = {},
    onOpenRoutine: (RoutineMicrocycle) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRoutine,
                containerColor = GoldAccent,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nueva rutina")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (microcycles.isEmpty()) {
                EmptyState(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(microcycles, key = { it.microcycleId }) { microcycle ->
                        val isEditorBlocked = microcycle.isActive && isWorkoutSessionActive

                        MicrocycleCard(
                            microcycle = microcycle,
                            isEditorBlocked = isEditorBlocked,
                            onClick = {
                                if (isEditorBlocked) {
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(
                                            message = "No puedes modificar tu rutina mientras entrenas",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else {
                                    onOpenRoutine(microcycle)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(BackgroundDark)
            .padding(bottom = 18.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = "Mis Rutinas",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ListAlt,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tienes rutinas",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Crea una desde el botón +",
            color = TextGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MicrocycleCard(
    microcycle: RoutineMicrocycle,
    isEditorBlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = microcycle.name,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sistema base: ${microcycle.blueprintType}",
                    color = TextGray,
                    fontSize = 13.sp
                )
                Text(
                    text = "Duración: ${microcycle.days.size} días",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (microcycle.isActive) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Activa",
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                if (!isEditorBlocked) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Administrar rutina",
                        tint = TextGray
                    )
                }
            }
        }
    }
}
