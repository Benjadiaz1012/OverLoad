package com.pdm0126.overload.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

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
        containerColor = MaterialTheme.colorScheme.surface,
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = { Text(text = title, color = MaterialTheme.colorScheme.onSurface) },
        text = { Text(text = text, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText, color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}