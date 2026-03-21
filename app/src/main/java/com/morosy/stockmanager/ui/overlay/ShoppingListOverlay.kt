package com.morosy.stockmanager.ui.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.morosy.stockmanager.data.db.BoardEntity
import com.morosy.stockmanager.ui.components.ShoppingListItemCard
import com.morosy.stockmanager.ui.shopping.ShoppingListBoardSection

enum class ShoppingListOverlayStep {
    BoardSelection,
    Result
}

@Composable
fun ShoppingListOverlay(
    open: Boolean,
    step: ShoppingListOverlayStep,
    boards: List<BoardEntity>,
    selectedBoardIds: Set<Long>,
    sections: List<ShoppingListBoardSection>,
    onToggleBoard: (Long) -> Unit,
    onShowResults: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!open) {
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .let { base ->
                        if (step == ShoppingListOverlayStep.BoardSelection) {
                            base.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onDismiss() }
                        } else {
                            base
                        }
                    }
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .fillMaxHeight(0.82f),
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    OverlayHeader(
                        title = if (step == ShoppingListOverlayStep.BoardSelection) {
                            "表示するボードを選択"
                        } else {
                            "欠品リスト"
                        },
                        onDismiss = onDismiss
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (step) {
                        ShoppingListOverlayStep.BoardSelection -> {
                            BoardSelectionContent(
                                boards = boards,
                                selectedBoardIds = selectedBoardIds,
                                onToggleBoard = onToggleBoard,
                                onShowResults = onShowResults
                            )
                        }

                        ShoppingListOverlayStep.Result -> {
                            ShoppingListResultContent(
                                sections = sections,
                                onDismiss = onDismiss
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayHeader(
    title: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "閉じる"
            )
        }
    }
}

@Composable
private fun ColumnScope.BoardSelectionContent(
    boards: List<BoardEntity>,
    selectedBoardIds: Set<Long>,
    onToggleBoard: (Long) -> Unit,
    onShowResults: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Text(
        text = "複数選択できます",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    if (boards.isEmpty()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ボードがありません",
                color = colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(boards, key = { it.id }) { board ->
                val selected = board.id in selectedBoardIds
                Surface(
                    onClick = { onToggleBoard(board.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) {
                        colorScheme.primaryContainer
                    } else {
                        colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (selected) {
                            colorScheme.primary.copy(alpha = 0.5f)
                        } else {
                            colorScheme.outline.copy(alpha = 0.25f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.size(20.dp))
                        Text(
                            text = board.name,
                            modifier = Modifier.weight(1f),
                            color = if (selected) {
                                colorScheme.onPrimaryContainer
                            } else {
                                colorScheme.onSurface
                            },
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier.size(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "選択中",
                                    tint = colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onShowResults,
        enabled = selectedBoardIds.isNotEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text("表示")
    }
}

@Composable
private fun ColumnScope.ShoppingListResultContent(
    sections: List<ShoppingListBoardSection>,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    if (sections.isEmpty()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "対象のアイテムはありません",
                color = colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            itemsIndexed(sections, key = { _, section -> section.boardId }) { index, section ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = section.boardName,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    section.items.chunked(2).forEachIndexed { rowIndex, rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ShoppingListItemCard(
                                item = rowItems[0],
                                modifier = Modifier.weight(1f)
                            )

                            if (rowItems.size > 1) {
                                ShoppingListItemCard(
                                    item = rowItems[1],
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        if (rowIndex < section.items.chunked(2).lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    if (index < sections.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text("閉じる")
    }
}








