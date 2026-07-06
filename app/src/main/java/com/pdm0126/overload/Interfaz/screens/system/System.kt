package com.pdm0126.overload.Interfaz.screens.system


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.pdm0126.overload.R
import com.pdm0126.overload.domain.model.Blueprint
import com.pdm0126.overload.domain.model.BlueprintCatalog

private val BackgroundDark = Color(0xFF0E0E0E)
private val CardDark = Color(0xFF1A1A1A)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)


private fun iconForBlueprint(id: String): ImageVector = when (id) {
    "ppl" -> Icons.Default.FitnessCenter
    "arnold" -> Icons.Default.EmojiEvents
    "upper_lower" -> Icons.Default.Schedule
    "full_body" -> Icons.Default.Person
    "heavy_duty" -> Icons.Default.Bolt
    "blank" -> Icons.Default.Edit
    else -> Icons.Default.FitnessCenter
}

private fun subtitleForBlueprint(blueprint: Blueprint): String =
    if (blueprint.defaultDays.isNotEmpty()) blueprint.defaultDays.joinToString(" / ")
    else "Personalizado"

@Composable
fun TrainingSystem(
    systems: List<Blueprint> = BlueprintCatalog.systems,
    onSystemSelected: (Blueprint) -> Unit = {},
    onConfirm: (Blueprint?) -> Unit = {},
    onNext: () -> Unit,

    ) {
    var selectedId by remember { mutableStateOf(systems.firstOrNull()?.id) }

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 56.dp)
            ) {
                Button(
                    onClick = {
                        onConfirm(systems.find { it.id == selectedId })

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Seleccionar sistema", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeaderSection()
            }

            items(systems) { blueprint ->
                SystemCard(
                    blueprint = blueprint,
                    isSelected = blueprint.id == selectedId,
                    onClick = {
                        selectedId = blueprint.id
                        onSystemSelected(blueprint)
                    }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo_yellow),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text("OVER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("LOAD", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Selecciona tu sistema",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Elige la estructura que mejor\nse adapte a ti.",
            color = TextGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SystemCard(
    blueprint: Blueprint,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) GoldAccent else Color(0xFF2E2E2E)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 190.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = iconForBlueprint(blueprint.id),
                contentDescription = blueprint.name,
                tint = GoldAccent,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = blueprint.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitleForBlueprint(blueprint),
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = blueprint.formattedMicrocycle,
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}
