package com.pdm0126.overload.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun OverloadConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    dismissText: String = "Cancelar",
    icon: ImageVector,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor =
        if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor
            )
        },
        title = { Text(text = title, color = MaterialTheme.colorScheme.onSurface) },
        text = { Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText, color = accentColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}