package com.pdm0126.overload.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun OverloadTopBar(
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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .defaultMinSize(minHeight = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            showBackButton -> {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            leadingIcon != null -> {
                IconButton(onClick = onLeadingClick) {
                    Icon(
                        leadingIcon,
                        contentDescription = leadingContentDescription,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )

        when {
            trailingContent != null -> trailingContent()
            trailingIcon != null -> {
                IconButton(onClick = onTrailingClick) {
                    Icon(
                        trailingIcon,
                        contentDescription = trailingContentDescription,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}