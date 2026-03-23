package com.morosy.stockmanager.ui.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.morosy.stockmanager.data.db.BoardEntity
import kotlinx.coroutines.launch

@Composable
fun BoardDrawerOverlay(
    open: Boolean,
    boards: List<BoardEntity>,
    currentBoardId: Long,
    editMode: Boolean,
    boardEditButtonModifier: Modifier = Modifier,
    addBoardButtonModifier: Modifier = Modifier,
    boardListModifier: Modifier = Modifier,
    currentBoardItemModifier: Modifier = Modifier,
    howToUseMenuItemModifier: Modifier = Modifier,
    forceMenuExpanded: Boolean = false,
    onSelectBoard: (Long) -> Unit,
    onClose: () -> Unit,
    onEnterEdit: () -> Unit,
    onExitEdit: () -> Unit,
    onAddBoard: () -> Unit,
    onRequestDeleteBoard: (BoardEntity) -> Unit,
    onExportBoardJson: () -> Unit,
    onExportBoardCsv: () -> Unit,
    onImportBoard: () -> Unit,
    onCreateBoardFromTool: () -> Unit,
    onOpenHowToUse: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenOssLicenses: () -> Unit,
    onOpenContact: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onRequestResetData: () -> Unit,
    onReorderBoards: (List<Long>) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

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
    val itemHeights = remember { mutableStateMapOf<Long, Int>() }
    var draggingBoardId by remember { mutableStateOf<Long?>(null) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var draggingOffsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(forceMenuExpanded) {
        menuOpen.value = forceMenuExpanded
    }

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
                color = colorScheme.surface,
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
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
                                    text = { Text("外部ツールからボードを作成") },
                                    onClick = {
                                        menuOpen.value = false
                                        onCreateBoardFromTool()
                                    }
                                )
                                DropdownMenuItem(
                                    modifier = howToUseMenuItemModifier,
                                    text = { Text("使い方") },
                                    onClick = {
                                        menuOpen.value = false
                                        onOpenHowToUse()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("お問い合わせ") },
                                    onClick = {
                                        menuOpen.value = false
                                        onOpenContact()
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
                                    text = { Text("利用規約") },
                                    onClick = {
                                        menuOpen.value = false
                                        onOpenTerms()
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
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "データを削除",
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    onClick = {
                                        menuOpen.value = false
                                        onRequestResetData()
                                    }
                                )
                            }
                        }
                    }

                    val renderBoards = if (editMode) localBoards else boards

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .then(boardListModifier),
                        state = listState
                    ) {
                        items(renderBoards, key = { it.id }) { b ->
                            val selected = b.id == currentBoardId
                            val isDragging = draggingBoardId == b.id
                            val bg = if (selected) {
                                colorScheme.primary
                            } else {
                                colorScheme.surfaceVariant.copy(alpha = 0.72f)
                            }
                            val contentColor = if (selected) {
                                colorScheme.onPrimary
                            } else {
                                colorScheme.onSurface
                            }
                            val translatedY by animateFloatAsState(
                                targetValue = if (isDragging) draggingOffsetY else 0f,
                                animationSpec = if (isDragging) {
                                    spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                } else {
                                    tween(
                                        durationMillis = 260,
                                        easing = FastOutSlowInEasing
                                    )
                                },
                                label = "boardDragTranslation"
                            )
                            val liftScale by animateFloatAsState(
                                targetValue = if (isDragging) 1.06f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "boardLiftScale"
                            )
                            val liftElevation by animateFloatAsState(
                                targetValue = if (isDragging) 42f else if (selected) 2f else 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "boardLiftElevation"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .onSizeChanged { itemHeights[b.id] = it.height }
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .animateItem(
                                            fadeInSpec = null,
                                            fadeOutSpec = null,
                                            placementSpec = if (isDragging) {
                                                null
                                            } else {
                                                tween(
                                                    durationMillis = 320,
                                                    easing = FastOutSlowInEasing
                                                )
                                            }
                                        )
                                        .fillMaxWidth()
                                        .pointerInput(editMode, b.id) {
                                            if (!editMode) {
                                                return@pointerInput
                                            }
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggingBoardId = b.id
                                                    draggingIndex = localBoards.indexOfFirst { it.id == b.id }
                                                    draggingOffsetY = 0f
                                                },
                                                onDragEnd = {
                                                    draggingBoardId = null
                                                    draggingIndex = -1
                                                    draggingOffsetY = 0f
                                                    onReorderBoardsLatest.value(localBoards.map { it.id })
                                                },
                                                onDragCancel = {
                                                    draggingBoardId = null
                                                    draggingIndex = -1
                                                    draggingOffsetY = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()

                                                    val currentIndex = draggingIndex
                                                    if (currentIndex < 0) {
                                                        return@detectDragGesturesAfterLongPress
                                                    }

                                                    val currentHeight = itemHeights[b.id]?.toFloat() ?: return@detectDragGesturesAfterLongPress
                                                    draggingOffsetY += dragAmount.y

                                                    while (
                                                        draggingOffsetY > currentHeight / 2f &&
                                                        draggingIndex < localBoards.lastIndex
                                                    ) {
                                                        val moved = localBoards.removeAt(draggingIndex)
                                                        localBoards.add(draggingIndex + 1, moved)
                                                        draggingIndex += 1
                                                        draggingOffsetY -= currentHeight
                                                    }

                                                    while (
                                                        draggingOffsetY < -(currentHeight / 2f) &&
                                                        draggingIndex > 0
                                                    ) {
                                                        val moved = localBoards.removeAt(draggingIndex)
                                                        localBoards.add(draggingIndex - 1, moved)
                                                        draggingIndex -= 1
                                                        draggingOffsetY += currentHeight
                                                    }
                                                }
                                            )
                                        }
                                        .graphicsLayer {
                                            translationY = translatedY
                                            scaleX = liftScale
                                            scaleY = liftScale
                                            shadowElevation = liftElevation
                                            ambientShadowColor = Color.Black.copy(alpha = 0.24f)
                                            spotShadowColor = Color.Black.copy(alpha = 0.32f)
                                        }
                                        .zIndex(if (isDragging) 2f else 0f)
                                        .then(if (selected && !editMode) currentBoardItemModifier else Modifier),
                                    color = bg,
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = if (selected && !isDragging) 2.dp else 0.dp,
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
                                                    .background(colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = ":::",
                                                    color = colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Box(modifier = Modifier.size(28.dp))
                                        }

                                        Text(
                                            text = b.name,
                                            color = contentColor,
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
                                                    .background(colorScheme.surface)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "削除",
                                                    tint = colorScheme.onErrorContainer
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
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .then(boardEditButtonModifier),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ボードを追加・編集")
                        }
                    } else {
                        Button(
                            onClick = { onAddBoard() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .then(addBoardButtonModifier),
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

