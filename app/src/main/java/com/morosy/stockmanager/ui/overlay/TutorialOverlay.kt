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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.morosy.stockmanager.ui.tutorial.TutorialStep

@Composable
fun TutorialOverlay(
    step: TutorialStep,
    targetRect: Rect?,
    progressIndex: Int,
    progressTotal: Int,
    canGoBack: Boolean,
    isLastStep: Boolean,
    canAdvance: Boolean,
    supportingMessage: String? = null,
    onTargetTap: () -> Unit,
    onBack: () -> Unit,
    onAdvance: () -> Unit,
    onSkip: () -> Unit
) {
    val cardAlignment = if (targetRect != null && targetRect.center.y > 900f) {
        Alignment.TopCenter
    } else {
        Alignment.BottomCenter
    }
    val highlightRadius = targetRect?.let { (maxOf(it.width, it.height) / 2f) + 24f }

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
                            val insideHighlight = targetRect != null && highlightRadius != null &&
                                (position - targetRect.center).getDistance() <= highlightRadius
                            if (isUp && insideHighlight) {
                                onTargetTap()
                            }
                            change.consume()
                        }
                    }
                }
                .drawWithContent {
                    drawContent()
                    drawRect(Color.Black.copy(alpha = 0.68f))
                    if (targetRect != null && highlightRadius != null) {
                        drawCircle(
                            color = Color.Transparent,
                            radius = highlightRadius,
                            center = targetRect.center,
                            blendMode = BlendMode.Clear
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
                    text = "$progressIndex / $progressTotal",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                LinearProgressIndicator(
                    progress = { progressIndex / progressTotal.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = step.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (supportingMessage != null) {
                    Text(
                        text = supportingMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = onBack,
                        enabled = canGoBack,
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text("戻る")
                    }
                    Button(
                        onClick = onAdvance,
                        enabled = canAdvance,
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text(if (isLastStep) "完了" else "次へ")
                    }
                }
                if (!isLastStep) {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("スキップ")
                    }
                }
            }
        }
    }
}
