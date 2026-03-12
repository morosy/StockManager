package com.morosy.stockmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morosy.stockmanager.data.db.StockItemStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

import com.morosy.stockmanager.data.db.StockItemEntity
import com.morosy.stockmanager.ui.theme.WarningContainerDark
import com.morosy.stockmanager.ui.theme.WarningContainerLight
import com.morosy.stockmanager.ui.theme.WarningOnContainerDark
import com.morosy.stockmanager.ui.theme.WarningOnContainerLight
import com.morosy.stockmanager.ui.theme.WarningOutlineDark
import com.morosy.stockmanager.ui.theme.WarningOutlineLight

@Composable
fun MagnetCard(
    item: StockItemEntity,
    stockBg: Color,
    stockText: Color,
    stockBorder: Color,
    outBg: Color,
    outText: Color,
    editMode: Boolean,
    isDeleting: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val yellowBg = if (colorScheme.background.luminance() < 0.5f) WarningContainerDark else WarningContainerLight
    val yellowText = if (colorScheme.background.luminance() < 0.5f) WarningOnContainerDark else WarningOnContainerLight
    val yellowBorder = if (colorScheme.background.luminance() < 0.5f) WarningOutlineDark else WarningOutlineLight

    val wobbleZ = remember(item.id) { Animatable(0f) }
    val flipY = remember(item.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val currentStatus = StockItemStatus.normalize(item.status)
    var displayedStatus by remember(item.id) { mutableIntStateOf(currentStatus) }
    var isFlipping by remember(item.id) { mutableStateOf(false) }

    LaunchedEffect(currentStatus, isFlipping) {
        if (!isFlipping) {
            displayedStatus = currentStatus
        }
    }

    val (bg, textColor, border) = when (displayedStatus) {
        StockItemStatus.IN_STOCK -> Triple(stockBg, stockText, BorderStroke(1.dp, stockBorder))
        StockItemStatus.HIGHLIGHTED -> Triple(yellowBg, yellowText, BorderStroke(1.dp, yellowBorder))
        else -> Triple(outBg, outText, null)
    }

    LaunchedEffect(editMode, isDeleting) {
        if (editMode && !isDeleting) {
            while (isActive) {
                wobbleZ.animateTo(-0.8f, tween(durationMillis = 140, easing = LinearEasing))
                wobbleZ.animateTo(0.8f, tween(durationMillis = 140, easing = LinearEasing))
            }
        } else {
            wobbleZ.snapTo(0f)
        }
    }

    AnimatedVisibility(
        visible = !isDeleting,
        exit = fadeOut(animationSpec = tween(180)) + shrinkOut(animationSpec = tween(180))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    rotationZ = wobbleZ.value
                    rotationY = flipY.value
                    cameraDistance = 12f * density
                }
        ) {
            Surface(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        if (editMode || isFlipping) {
                            return@clickable
                        }
                        val nextStatus = StockItemStatus.next(displayedStatus)
                        scope.launch {
                            isFlipping = true
                            flipY.animateTo(
                                90f,
                                animationSpec = tween(durationMillis = 170, easing = FastOutSlowInEasing)
                            )
                            displayedStatus = nextStatus
                            flipY.animateTo(
                                180f,
                                animationSpec = tween(durationMillis = 170, easing = FastOutSlowInEasing)
                            )
                            flipY.snapTo(0f)
                            onToggle()
                            isFlipping = false
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = bg,
                border = border
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .graphicsLayer {
                            if (flipY.value > 90f) {
                                rotationY = 180f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.name,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (editMode) {
                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .offset(x = 6.dp, y = (-6).dp)
                        .clip(CircleShape)
                        .background(colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "削除",
                        tint = colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
