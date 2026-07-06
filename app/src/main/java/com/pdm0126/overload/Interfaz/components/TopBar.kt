package com.pdm0126.overload.Interfaz.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BackgroundDark = Color(0xFF0E0E0E)
private val TitleSize = 22.sp

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    leadingIcon: ImageVector? = null,
    onLeadingClick: () -> Unit = {},
    leadingContentDescription: String? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: () -> Unit = {},
    trailingContentDescription: String? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .defaultMinSize(minHeight = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            showBackButton -> {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            leadingIcon != null -> {
                IconButton(onClick = onLeadingClick) {
                    Icon(
                        leadingIcon,
                        contentDescription = leadingContentDescription,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = TitleSize,
            modifier = Modifier.weight(1f)
        )

        when {
            trailingContent != null -> trailingContent()
            trailingIcon != null -> {
                IconButton(onClick = onTrailingClick) {
                    Icon(
                        trailingIcon,
                        contentDescription = trailingContentDescription,
                        tint = Color.White
                    )
                }
            }
        }
    }
}