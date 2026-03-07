package com.morosy.stockmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp

@Composable
private fun EqualWidthRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val count = measurables.size.coerceAtLeast(1)
        val totalWidth = constraints.maxWidth
        val childWidth = totalWidth / count

        val childConstraints = constraints.copy(
            minWidth = childWidth,
            maxWidth = childWidth,
        )

        val placeables = measurables.map { it.measure(childConstraints) }
        val height = placeables.maxOfOrNull { it.height } ?: constraints.minHeight

        layout(totalWidth, height) {
            var x = 0
            placeables.forEach { p ->
                p.placeRelative(x, (height - p.height) / 2)
                x += childWidth
            }
        }
    }
}

@Composable
fun FilterSegmentedRow(
    modifier: Modifier,
    showStock: Boolean,
    showOut: Boolean,
    onStockClick: () -> Unit,
    onOutClick: () -> Unit,
) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        EqualWidthRow(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            SegItem(
                label = "在庫",
                selected = showStock,
                onClick = onStockClick,
            )
            SegItem(
                label = "出庫",
                selected = showOut,
                onClick = onOutClick,
            )
        }
    }
}

@Composable
private fun SegItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxHeight(),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

