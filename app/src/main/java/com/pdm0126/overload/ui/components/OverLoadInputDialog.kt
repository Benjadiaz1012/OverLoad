package com.pdm0126.overload.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

private val CardDark = Color(0xFF1A1A1A)
private val GoldAccent = Color(0xFFE8A317)
private val TextGray = Color(0xFFA0A0A0)
private val ErrorRed = Color(0xFFE53935)

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
        containerColor = CardDark,
        title = { Text(text = title, color = Color.White) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { newValue ->
                    if (newValue.length <= maxLength && newValue.matches(NAME_INPUT_REGEX)) {
                        text = newValue
                    }
                },
                label = { Text(label, color = TextGray) },
                supportingText = {
                    Text(
                        text = "${text.length}/$maxLength",
                        color = TextGray
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = TextGray,
                    cursorColor = GoldAccent
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank()
            ) {
                Text(text = confirmText, color = GoldAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText, color = TextGray)
            }
        }
    )
}