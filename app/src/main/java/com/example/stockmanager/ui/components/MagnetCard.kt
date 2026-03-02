package com.example.stockmanager.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

import com.example.stockmanager.data.db.StockItemEntity

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
    val scope = rememberCoroutineScope()

    val flipRotation = remember(item.id) { Animatable(if (item.inStock) 0f else 180f) }

    LaunchedEffect(item.inStock) {
        val target = if (item.inStock) 0f else 180f
        if (abs(flipRotation.value - target) > 1f) {
            flipRotation.snapTo(target)
        }
    }

    val drawFront = flipRotation.value <= 90f
    val bg = if (drawFront) stockBg else outBg
    val textColor = if (drawFront) stockText else outText
    val border = if (drawFront) BorderStroke(1.dp, stockBorder) else null

    val wobbleZ = remember(item.id) { Animatable(0f) }

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
                .height(48.dp)
                .graphicsLayer { rotationZ = wobbleZ.value }
        ) {
            Surface(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        rotationY = flipRotation.value
                        cameraDistance = 16f * density
                    }
                    .clickable {
                        if (editMode) {
                            return@clickable
                        }
                        scope.launch {
                            val goingToBack = flipRotation.value < 90f
                            val target = if (goingToBack) 180f else 0f

                            flipRotation.animateTo(
                                targetValue = target,
                                animationSpec = tween(
                                    durationMillis = 700,
                                    easing = FastOutSlowInEasing
                                )
                            )
                            onToggle()
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                color = bg,
                border = border
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (!drawFront) {
                                rotationY = 180f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.name,
                        color = textColor,
                        fontWeight = FontWeight.Medium
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
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "削除",
                        tint = Color(0xFFB3261E)
                    )
                }
            }
        }
    }
}
