package com.morosy.stockmanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morosy.stockmanager.data.db.StockItemEntity
import com.morosy.stockmanager.data.db.StockItemStatus
import com.morosy.stockmanager.ui.theme.WarningContainerDark
import com.morosy.stockmanager.ui.theme.WarningContainerLight
import com.morosy.stockmanager.ui.theme.WarningOnContainerDark
import com.morosy.stockmanager.ui.theme.WarningOnContainerLight
import com.morosy.stockmanager.ui.theme.WarningOutlineDark
import com.morosy.stockmanager.ui.theme.WarningOutlineLight

@Composable
fun ShoppingListItemCard(
    item: StockItemEntity,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val yellowBg = if (colorScheme.background.luminance() < 0.5f) WarningContainerDark else WarningContainerLight
    val yellowText = if (colorScheme.background.luminance() < 0.5f) WarningOnContainerDark else WarningOnContainerLight
    val yellowBorder = if (colorScheme.background.luminance() < 0.5f) WarningOutlineDark else WarningOutlineLight

    val stockBg = colorScheme.surface
    val stockText = colorScheme.onSurface
    val stockBorder = colorScheme.outline.copy(alpha = 0.35f)
    val outBg = colorScheme.errorContainer
    val outText = colorScheme.onErrorContainer

    val (bg, textColor, border) = when (StockItemStatus.normalize(item.status)) {
        StockItemStatus.IN_STOCK -> Triple(stockBg, stockText, BorderStroke(1.dp, stockBorder))
        StockItemStatus.HIGHLIGHTED -> Triple(yellowBg, yellowText, BorderStroke(1.dp, yellowBorder))
        else -> Triple(outBg, outText, null)
    }

    val surfaceModifier = modifier
        .fillMaxWidth()
        .height(56.dp)

    if (onClick == null) {
        Surface(
            modifier = surfaceModifier,
            shape = RoundedCornerShape(16.dp),
            color = bg,
            border = border
        ) {
            ShoppingListItemCardContent(item = item, textColor = textColor)
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = surfaceModifier,
            shape = RoundedCornerShape(16.dp),
            color = bg,
            border = border
        ) {
            ShoppingListItemCardContent(item = item, textColor = textColor)
        }
    }
}

@Composable
private fun ShoppingListItemCardContent(
    item: StockItemEntity,
    textColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.name,
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
}
