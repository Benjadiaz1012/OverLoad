package com.pdm0126.overload.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.BookmarkRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun BookmarkedIcon() {
    Icon(
        imageVector = Icons.Outlined.BookmarkAdd,
        contentDescription = "Bookmark",
        tint = MaterialTheme.colorScheme.tertiary
    )
}

@Composable
fun UnBookmarkedIcon() {
    Icon(
        imageVector = Icons.Outlined.BookmarkRemove,
        contentDescription = "Bookmark",
        tint = MaterialTheme.colorScheme.tertiary
    )
}

@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    IconToggleButton(
        checked = isBookmarked,
        onCheckedChange = onCheckedChange
    ) {
        Icon(
            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkAdd,
            contentDescription = "Bookmark",
            tint = if (isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}