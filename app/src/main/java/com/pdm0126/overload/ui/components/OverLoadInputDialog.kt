package com.pdm0126.overload.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private val NAME_INPUT_REGEX = Regex("^[\\p{L}\\p{N} .,'()/-]*$")
private const val DEFAULT_MAX_LENGTH = 30

@Composable
fun OverloadInputDialog(
    title: String,
    initialValue: String,
    label: String,
    confirmText: String = "Guardar",
    dismissText: String = "Cancelar",
    maxLength: Int = DEFAULT_MAX_LENGTH,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(text = title, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { newValue ->
                    if (newValue.length <= maxLength && newValue.matches(NAME_INPUT_REGEX)) {
                        text = newValue
                    }
                },
                label = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                supportingText = {
                    Text(
                        text = "${text.length}/$maxLength",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank()
            ) {
                Text(text = confirmText, color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}