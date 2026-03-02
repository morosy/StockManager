package com.example.stockmanager.ui.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import com.example.stockmanager.data.db.BoardEntity

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

                        IconButton(onClick = { /* TODO */ }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "設定"
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(boards, key = { it.id }) { b ->
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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = b.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                            modifier = Modifier.align(Alignment.CenterStart)
                                        )

                                        if (editMode) {
                                            IconButton(
                                                onClick = { onRequestDeleteBoard(b) },
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
                                text = "編集を終了",
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
