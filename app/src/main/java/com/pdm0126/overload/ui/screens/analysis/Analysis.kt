package com.pdm0126.overload.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.pdm0126.overload.ui.components.OverloadScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
internal fun rememberToolTipMarker() = rememberDefaultCartesianMarker(
    label = rememberTextComponent(
        background = rememberShapeComponent(
            fill = Fill(MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(8.dp)
        )
    )
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
            context.model.extraStore.getOrNull(muscleListKey)?.getOrNull(x.toInt())?.replaceFirstChar { it.uppercase() } ?: ""
        }
    }

    val dateFormatter = remember(uiState.exerciseProgression) {
        CartesianValueFormatter { context, x, _ ->
            context.model.extraStore.getOrNull(dateListKey)?.getOrNull(x.toInt()) ?: ""
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // TABS
            SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
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
                    // CONTENIDO TAB 1: DISTRIBUCIÓN
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
                                                        thickness = 24.dp, // Barras más anchas para forzar el scroll si hay muchos músculos
                                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                                    )
                                                )
                                            ),
                                            startAxis = VerticalAxis.rememberStart(),
                                            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = muscleFormatter),
                                            marker = rememberToolTipMarker(), // NUEVO: Tooltip al tocar
                                        ),
                                        modelProducer = distributionModelProducer,
                                        scrollState = rememberVicoScrollState(), // NUEVO: Permite Scroll Horizontal
                                        zoomState = rememberVicoZoomState(zoomEnabled = true), // NUEVO: Permite pellizcar para alejar/acercar
                                        modifier = Modifier.fillMaxWidth().height(300.dp)
                                    )
                                }
                            }
                        }
                    }
                    // CONTENIDO TAB 2: EVOLUCIÓN (LÍNEAS)
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
                                Text("Evolución de volumen total estimado en el tiempo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(24.dp))

                                // CALL TO ACTION LIMPIO PARA BUSCAR EJERCICIO
                                OutlinedCard(
                                    onClick = onNavigateToLibrary,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            val currentExerciseName = uiState.availableExercises.find { it.id == uiState.selectedExerciseId }?.name
                                            Text(
                                                text = currentExerciseName ?: "Buscar Ejercicio a Analizar",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (currentExerciseName != null) {
                                                Text(
                                                    text = "Toca para cambiar de ejercicio",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                when {
                                    uiState.selectedExerciseId == null -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                    RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Insights,
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
                                                marker = rememberToolTipMarker()
                                            ),
                                            modelProducer = trendModelProducer,
                                            scrollState = rememberVicoScrollState(),
                                            zoomState = rememberVicoZoomState(zoomEnabled = true),
                                            modifier = Modifier.fillMaxWidth().height(250.dp)
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