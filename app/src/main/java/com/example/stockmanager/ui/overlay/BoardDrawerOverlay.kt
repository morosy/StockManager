package com.example.stockmanager.ui.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.stockmanager.data.db.BoardEntity
import kotlinx.coroutines.launch

@Composable
fun BoardDrawerOverlay(
    open: Boolean,
    boards: List<BoardEntity>,
    currentBoardId: Long,
    editMode: Boolean,
    onSelectBoard: (Long) -> Unit,
    onClose: () -> Unit,
    onEnterEdit: () -> Unit,
    onExitEdit: () -> Unit,
    onAddBoard: () -> Unit,
    onRequestDeleteBoard: (BoardEntity) -> Unit,
    onExportBoardJson: () -> Unit,
    onExportBoardCsv: () -> Unit,
    onImportBoard: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenOssLicenses: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onReorderBoards: (List<Long>) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val scrim = remember { Animatable(0f) }
    val panelX = remember { Animatable(-280f) }

    LaunchedEffect(open) {
        if (open) {
            scope.launch { scrim.animateTo(0.45f, tween(220)) }
            scope.launch { panelX.animateTo(0f, tween(260, easing = FastOutSlowInEasing)) }
        } else {
            scope.launch { scrim.animateTo(0f, tween(180)) }
            scope.launch { panelX.animateTo(-280f, tween(220, easing = FastOutSlowInEasing)) }
        }
    }

    val localBoards = remember { mutableStateListOf<BoardEntity>() }
    LaunchedEffect(editMode, boards) {
        localBoards.clear()
        localBoards.addAll(boards)
    }

    val listState = rememberLazyListState()
    val onReorderBoardsLatest = rememberUpdatedState(onReorderBoards)
    val menuOpen = remember { mutableStateOf(false) }

    if (open || scrim.value > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(999f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrim.value))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClose() }
            )

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .offset(x = panelX.value.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(top = 12.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ボード",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        Box {
                            IconButton(onClick = { menuOpen.value = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "メニュー"
                                )
                            }
                            DropdownMenu(
                                expanded = menuOpen.value,
                                onDismissRequest = { menuOpen.value = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("このボードをエクスポート") },
                                    onClick = {
                                        menuOpen.value = false
                                        onExportBoardJson()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("このボードをエクスポート(CSV)") },
                                    onClick = {
                                        menuOpen.value = false
                                        onExportBoardCsv()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("ボードをインポート") },
                                    onClick = {
                                        menuOpen.value = false
                                        onImportBoard()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("About") },
                                    onClick = {
                                        menuOpen.value = false
                                        onOpenAbout()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("OSSライセンス") },
                                    onClick = {
                                        menuOpen.value = false
                                        onOpenOssLicenses()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("プライバシーポリシー") },
                                    onClick = {
                                        menuOpen.value = false
                                        onOpenPrivacyPolicy()
                                    }
                                )
                            }
                        }
                    }

                    val renderBoards = if (editMode) localBoards else boards

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = listState
                    ) {
                        items(renderBoards, key = { it.id }) { b ->
                            val selected = b.id == currentBoardId
                            val bg = if (selected) Color(0xFFF3EDF7) else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = bg,
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = {
                                        if (!editMode) {
                                            onSelectBoard(b.id)
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        if (editMode) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF3EDF7))
                                                    .pointerInput(b.id) {
                                                        detectDragGesturesAfterLongPress(
                                                            onDragEnd = {
                                                                onReorderBoardsLatest.value(localBoards.map { it.id })
                                                            },
                                                            onDrag = { change, _ ->
                                                                change.consume()

                                                                val fromIndex = localBoards.indexOfFirst { it.id == b.id }
                                                                if (fromIndex < 0) {
                                                                    return@detectDragGesturesAfterLongPress
                                                                }

                                                                val visible = listState.layoutInfo.visibleItemsInfo
                                                                if (visible.isEmpty()) {
                                                                    return@detectDragGesturesAfterLongPress
                                                                }

                                                                val pointerY = change.position.y
                                                                val targetInfo = visible.firstOrNull { info ->
                                                                    val top = info.offset
                                                                    val bottom = info.offset + info.size
                                                                    pointerY.toInt() in top..bottom
                                                                } ?: return@detectDragGesturesAfterLongPress

                                                                val toIndex = targetInfo.index
                                                                if (toIndex == fromIndex) {
                                                                    return@detectDragGesturesAfterLongPress
                                                                }

                                                                val moved = localBoards.removeAt(fromIndex)
                                                                val insertIndex = toIndex.coerceIn(0, localBoards.size)
                                                                localBoards.add(insertIndex, moved)
                                                            }
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = ":::",
                                                    color = Color(0xFF6750A4),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Box(modifier = Modifier.size(28.dp))
                                        }

                                        Text(
                                            text = b.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 8.dp)
                                        )

                                        if (editMode) {
                                            IconButton(
                                                onClick = { onRequestDeleteBoard(b) },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "削除",
                                                    tint = Color(0xFFB3261E)
                                                )
                                            }
                                        } else {
                                            Box(modifier = Modifier.size(28.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )

                    if (!editMode) {
                        TextButton(
                            onClick = { onEnterEdit() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ボードを追加・編集")
                        }
                    } else {
                        Button(
                            onClick = { onAddBoard() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6750A4),
                                contentColor = Color.White
                            )
                        ) {
                            Text("ボードを追加")
                        }

                        TextButton(
                            onClick = { onExitEdit() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "編集を完了",
                                color = Color(0xFFB3261E),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
