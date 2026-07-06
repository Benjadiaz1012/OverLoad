package com.pdm0126.overload.Interfaz.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)
private val DividerGray = Color(0xFF2E2E2E)
private val GreenPositive = Color(0xFF4CAF50)

data class MuscleGroupVolume(
    val label: String,
    val value: Int,
    val color: Color
)

data class PeriodSummary(
    val totalVolume: String,
    val improvement: String,
    val sessions: String
)

private val fakeMuscleVolumes = listOf(
    MuscleGroupVolume("Pecho", 52430, Color(0xFFE53935)),
    MuscleGroupVolume("Espalda", 41250, Color(0xFF1E88E5)),
    MuscleGroupVolume("Piernas", 38600, Color(0xFF43A047)),
    MuscleGroupVolume("Hombros", 22180, Color(0xFFFB8C00)),
    MuscleGroupVolume("Bíceps", 18750, Color(0xFF8E24AA)),
    MuscleGroupVolume("Tríceps", 15340, Color(0xFF00ACC1)),
)

private val fakeSummary = PeriodSummary(
    totalVolume = "188,550 kg",
    improvement = "+14.2%",
    sessions = "28"
)

private val periodOptions = listOf("1M", "3M", "6M", "1A", "Todo")
private val metricTabs = listOf("Volumen Total", "Series Totales", "Repeticiones Totales")

@Composable
fun Analysis(
    muscleVolumes: List<MuscleGroupVolume> = fakeMuscleVolumes,
    summary: PeriodSummary = fakeSummary,
    dateRangeLabel: String = "15 Abr – 8 Jul 2024",
    onCalendarClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(metricTabs.first()) }
    var selectedPeriod by remember { mutableStateOf("3M") }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(onBack = onBack, onCalendarClick = onCalendarClick)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            MetricTabsRow(
                tabs = metricTabs,
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PeriodSelectorRow(
                periods = periodOptions,
                selected = selectedPeriod,
                onSelect = { selectedPeriod = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = dateRangeLabel,
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            VolumeChartCard(muscleVolumes = muscleVolumes)

            Spacer(modifier = Modifier.height(16.dp))

            InfoCard()

            Spacer(modifier = Modifier.height(16.dp))

            PeriodSummaryCard(summary = summary)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    onCalendarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
        }

        Text(
            text = "Análisis Muscular",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        IconButton(onClick = onCalendarClick) {
            Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario", tint = Color.White)
        }
    }
}

@Composable
private fun MetricTabsRow(
    tabs: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color.Transparent else CardDark)
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) GoldAccent else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelect(tab) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) GoldAccent else TextGray,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PeriodSelectorRow(
    periods: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardDark),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        periods.forEach { period ->
            val isSelected = period == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) GoldAccent else Color.Transparent)
                    .clickable { onSelect(period) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period,
                    color = if (isSelected) Color.Black else TextGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun VolumeChartCard(muscleVolumes: List<MuscleGroupVolume>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Volumen Total por Grupo Muscular (kg)",
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            SimpleBarChart(
                data = muscleVolumes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
        }
    }
}

@Composable
private fun SimpleBarChart(
    data: List<MuscleGroupVolume>,
    modifier: Modifier = Modifier
) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 1).toFloat()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "%,d".format(item.value),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                val barHeightFraction = item.value / maxValue

                Box(
                    modifier = Modifier
                        .fillMaxHeight(barHeightFraction.coerceIn(0.05f, 1f))
                        .width(28.dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(item.color)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.label,
                    color = TextGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "El volumen total se calcula como:",
                    color = Color.White,
                    fontSize = 13.sp
                )
                Text(
                    text = "Peso (kg) × Repeticiones × Series",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun PeriodSummaryCard(summary: PeriodSummary) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumen del período",
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DividerGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStat(
                    icon = Icons.Default.BarChart,
                    iconTint = GoldAccent,
                    value = summary.totalVolume,
                    label = "Volumen Total",
                    sublabel = "Todos los grupos",
                    modifier = Modifier.weight(1f)
                )
                SummaryStat(
                    icon = Icons.Default.TrendingUp,
                    iconTint = GreenPositive,
                    value = summary.improvement,
                    valueColor = GreenPositive,
                    label = "Mejora vs. período",
                    sublabel = "anterior",
                    modifier = Modifier.weight(1f)
                )
                SummaryStat(
                    icon = Icons.Default.EventAvailable,
                    iconTint = GoldAccent,
                    value = summary.sessions,
                    label = "Sesiones realizadas",
                    sublabel = "En el período",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    sublabel: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Column(modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = TextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = sublabel, color = TextGray, fontSize = 11.sp)
    }
}
