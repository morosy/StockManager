package com.morosy.stockmanager.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.morosy.stockmanager.ui.tutorial.TutorialStep

@Composable
fun TutorialOverlay(
    step: TutorialStep,
    targetRect: Rect?,
    canAdvance: Boolean,
    supportingMessage: String? = null,
    onTargetTap: () -> Unit,
    onAdvance: () -> Unit,
    onSkip: () -> Unit
) {
    val cardAlignment = if (targetRect != null && targetRect.center.y > 900f) {
        Alignment.TopCenter
    } else {
        Alignment.BottomCenter
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(2000f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
                .background(Color.Transparent)
                .pointerInput(step, targetRect) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: continue
                            val position = change.position
                            val isUp = !change.pressed
                            if (isUp && targetRect != null && targetRect.contains(position)) {
                                onTargetTap()
                            }
                            change.consume()
                        }
                    }
                }
                .drawWithContent {
                    drawContent()
                    drawRect(Color.Black.copy(alpha = 0.68f))
                    if (targetRect != null) {
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = targetRect.topLeft,
                            size = targetRect.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                            blendMode = BlendMode.Clear
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = targetRect.topLeft,
                            size = targetRect.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                            style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f)))
                        )
                    }
                }
        )

        Surface(
            modifier = Modifier
                .align(cardAlignment)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 12.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = step.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (supportingMessage != null) {
                    Text(
                        text = supportingMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (!step.requiresTargetTap) {
                    Button(
                        onClick = onAdvance,
                        enabled = canAdvance,
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        Text(step.actionLabel)
                    }
                } else if (!canAdvance || targetRect == null) {
                    Text(
                        text = if (targetRect == null) "表示を準備しています..." else step.actionLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = step.actionLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("スキップ")
                }
            }
        }
    }
}
