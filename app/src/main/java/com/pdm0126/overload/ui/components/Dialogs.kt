package com.pdm0126.overload.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun OverloadConfirmDialog(
    title: String,
    text: String,
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar",
    isDestructive: Boolean = false,
    icon: ImageVector? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        icon = icon?.let {
            { Icon(it, contentDescription = null, tint = if (isDestructive) MaterialTheme.colorScheme.error else LocalContentColor.current) }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (isDestructive) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.buttonColors()
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

@Composable
fun OverloadInputDialog(
    title: String,
    initialValue: String,
    label: String,
    confirmText: String = "Guardar",
    dismissText: String = "Cancelar",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputValue by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text(label) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(inputValue) }) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}