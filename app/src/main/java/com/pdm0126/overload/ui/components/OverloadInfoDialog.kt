package com.pdm0126.overload.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

private val CardDark = Color(0xFF1A1A1A)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)

@Composable
fun OverloadInfoDialog(
    title: String,
    text: String,
    icon: ImageVector? = null,
    dismissText: String = "Entendido",
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        icon = icon?.let {
            {
                Icon(imageVector = it, contentDescription = null, tint = GoldAccent)
            }
        },
        title = { Text(text = title, color = Color.White) },
        text = { Text(text = text, color = TextGray) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText, color = GoldAccent)
            }
        }
    )
}