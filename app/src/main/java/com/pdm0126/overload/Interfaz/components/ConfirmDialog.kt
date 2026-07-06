package com.pdm0126.overload.Interfaz.components

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
private val ErrorRed = Color(0xFFE53935)

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    dismissText: String = "Cancelar",
    icon: ImageVector,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) ErrorRed else GoldAccent
            )
        },
        title = { Text(text = title, color = Color.White) },
        text = { Text(text = text, color = TextGray) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText, color = if (isDestructive) ErrorRed else GoldAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText, color = TextGray)
            }
        }
    )
}