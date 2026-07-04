package com.pdm0126.overload.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            { Icon(
                imageVector = it,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
            ) }
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
                Text(dismissText, color = MaterialTheme.colorScheme.onPrimary)
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
    maxLength: Int = 40,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputValue by remember { mutableStateOf(initialValue) }
    val isInputValid = inputValue.isNotBlank() && inputValue.length <= maxLength

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = {
                        if (it.length <= maxLength) inputValue = it
                    },
                    label = { Text(label, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = inputValue.length == maxLength,
                    supportingText = {
                        Text(
                            text = "${inputValue.length} / $maxLength",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(inputValue.trim()) },
                enabled = isInputValid
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}