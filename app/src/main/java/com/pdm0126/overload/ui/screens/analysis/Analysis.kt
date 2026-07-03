package com.pdm0126.overload.ui.screens.analysis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.pdm0126.overload.R
import com.pdm0126.overload.domain.TechnicalDictionary
import com.pdm0126.overload.ui.components.OverloadScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
internal fun rememberToolTipMarker(
    valueFormatter: DefaultCartesianMarker.ValueFormatter = remember { DefaultCartesianMarker.ValueFormatter.default() }
) = rememberDefaultCartesianMarker(
    label = rememberTextComponent(
        background = rememberShapeComponent(
            fill = Fill(MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(1.dp)
        )
    ),
    valueFormatter = valueFormatter
)
@Composable
fun AnalysisScreen(
    onNavigateToLibrary : () -> Unit,
    viewModel: AnalysisViewModel = viewModel(factory = AnalysisViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Distribución", "Evolución")

    val distributionModelProducer = remember { CartesianChartModelProducer() }
    val trendModelProducer = remember { CartesianChartModelProducer() }

    val muscleListKey = remember { ExtraStore.Key<List<String>>() }
    val dateListKey = remember { ExtraStore.Key<List<String>>() }

    val muscleFormatter = remember(uiState.muscleDistribution) {
        CartesianValueFormatter { context, x, _ ->
            val fullName = context.model.extraStore.getOrNull(muscleListKey)?.getOrNull(x.toInt())?.replaceFirstChar { it.uppercase() } ?: ""
            TechnicalDictionary.muscleAbbreviationMap[fullName] ?: fullName
        }
    }

    val dateFormatter = remember(uiState.exerciseProgression) {
        CartesianValueFormatter { context, x, _ ->
            context.model.extraStore.getOrNull(dateListKey)?.getOrNull(x.toInt()) ?: ""
        }
    }

    val distributionTooltipFormatter = remember(uiState.muscleDistribution) {
        DefaultCartesianMarker.ValueFormatter { context, targets ->
            val xIndex = targets.first().x.toInt()
            val fullName = context.model.extraStore.getOrNull(muscleListKey)?.getOrNull(xIndex)?.replaceFirstChar { it.uppercase() } ?: ""
            val volume = uiState.muscleDistribution.getOrNull(xIndex)?.totalEffectiveVolume ?: 0f
            "$fullName: ${String.format(Locale.US, "%.1f", volume)} Kg"
        }
    }

    val trendTooltipFormatter = remember(uiState.exerciseProgression) {
        DefaultCartesianMarker.ValueFormatter { context, targets ->
            val xIndex = targets.first().x.toInt()
            val date = context.model.extraStore.getOrNull(dateListKey)?.getOrNull(xIndex) ?: ""
            val volume = uiState.exerciseProgression.getOrNull(xIndex)?.totalVolume ?: 0f
            "$date: ${String.format(Locale.US, "%.1f", volume)} Kg"
        }
    }

    LaunchedEffect(uiState.muscleDistribution) {
        if (uiState.muscleDistribution.isNotEmpty()) {
            val volumes = uiState.muscleDistribution.map { it.totalEffectiveVolume }
            val muscleNames = uiState.muscleDistribution.map { it.muscleGroup }
            distributionModelProducer.runTransaction {
                columnModel { series(volumes) }
                extras { it[muscleListKey] = muscleNames }
            }
        }
    }

    LaunchedEffect(uiState.exerciseProgression) {
        if (uiState.exerciseProgression.isNotEmpty()) {
            val volumes = uiState.exerciseProgression.map { it.totalVolume }
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            val dates = uiState.exerciseProgression.map { sdf.format(Date(it.timestamp)) }
            trendModelProducer.runTransaction {
                lineModel { series(volumes) }
                extras { it[dateListKey] = dates }
            }
        }
    }

    OverloadScaffold(
        title = "Análisis",
        showBackButton = false
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (selectedTabIndex == 0) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Volumen Efectivo Total",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Distribución ponderada por grupo muscular",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                if (uiState.muscleDistribution.isEmpty()) {
                                    Text(
                                        text = "Aún no hay datos suficientes. Completa sesiones de entrenamiento",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    CartesianChartHost(
                                        chart = rememberCartesianChart(
                                            rememberColumnCartesianLayer(
                                                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                                    rememberLineComponent(
                                                        fill = Fill(MaterialTheme.colorScheme.primary),
                                                        thickness = 38.dp,
                                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                                    )
                                                ),
                                            ),
                                            startAxis = VerticalAxis.rememberStart(),
                                            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = muscleFormatter),
                                            marker = rememberToolTipMarker(valueFormatter = distributionTooltipFormatter),
                                        ),
                                        modelProducer = distributionModelProducer,
                                        scrollState = rememberVicoScrollState(),
                                        zoomState = rememberVicoZoomState(zoomEnabled = true),
                                        modifier = Modifier
                                            .width(950.dp)
                                            .height(350.dp)
                                    )
                                }
                            }
                        }
                    }
                    else {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Sobrecarga Progresiva",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Evolución de volumen total en el tiempo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                val selectedExercise = uiState.availableExercises.find { it.id == uiState.selectedExerciseId }
                                ExerciseSelectorCard(
                                    selectedExerciseName = selectedExercise?.name,
                                    onClick = { onNavigateToLibrary() }
                                )
                                /*Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    TextField(
                                        value = selectedExercise?.name ?: "",
                                        onValueChange = {},
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        maxLines = 1,
                                        readOnly = true,
                                        //enabled = false,
                                        placeholder = {
                                            Text(
                                                text = "Ejercicio a analizar",
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
                                            if (selectedExercise != null) {
                                                IconButton(onClick = { viewModel.selectExercise(null) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Cancel,
                                                        contentDescription = "Limpiar selección",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = TextFieldDefaults.colors(
                                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledIndicatorColor = if (selectedExercise != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .padding(end = 48.dp)
                                            .clickable { onNavigateToLibrary() }
                                    )
                                }*/
                                /*Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, if (selectedExercise == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary)
                                ) {
                                    ListItem(
                                        modifier = Modifier.clickable { onNavigateToLibrary() },
                                        headlineContent = {
                                            Text(
                                                text = selectedExercise?.name ?: "Seleccionar ejercicio",
                                                maxLines = 1,
                                                overflow = Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.search_insights_24px),
                                                contentDescription = "Analizar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        trailingContent = {
                                            if (selectedExercise != null) {
                                                IconButton(onClick = { viewModel.selectExercise(null) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Quitar selección",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        },
                                        colors = ListItemDefaults.colors(containerColor = Transparent)
                                    )
                                }*/

                                Spacer(modifier = Modifier.height(24.dp))

                                when {
                                    uiState.selectedExerciseId == null -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(350.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(
                                                        alpha = 0.3f
                                                    ),
                                                    RoundedCornerShape(12.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QueryStats,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                    uiState.exerciseProgression.isEmpty() -> {
                                        Text(
                                            text = "No hay suficientes series completadas de este ejercicio para trazar una tendencia",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    else -> {
                                        CartesianChartHost(
                                            chart = rememberCartesianChart(
                                                rememberLineCartesianLayer(),
                                                startAxis = VerticalAxis.rememberStart(),
                                                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = dateFormatter),
                                                marker = rememberToolTipMarker(valueFormatter = trendTooltipFormatter)
                                            ),
                                            modelProducer = trendModelProducer,
                                            scrollState = rememberVicoScrollState(),
                                            zoomState = rememberVicoZoomState(zoomEnabled = true),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(350.dp)
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
}

@Composable
fun ExerciseSelectorCard(
    selectedExerciseName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick, // Hace que toda la tarjeta sea clickeable
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selectedExerciseName == null)
                MaterialTheme.colorScheme.outlineVariant
            else
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // El ícono cambia dependiendo de si hay algo seleccionado
                Icon(
                    imageVector = if (selectedExerciseName == null) Icons.Default.Search else Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = if (selectedExerciseName == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Ejercicio a analizar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedExerciseName ?: "Toca para buscar en tu biblioteca",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selectedExerciseName == null) FontWeight.Normal else FontWeight.Bold,
                        color = if (selectedExerciseName == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Un pequeño ícono de "intercambio" o flecha para indicar que se puede cambiar
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Cambiar ejercicio",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

