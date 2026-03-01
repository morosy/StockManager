package com.example.stockmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

data class StockItem(
    val id: Long,
    val name: String,
    val inStock: Boolean,
    val createdAt: Long,
)

data class Board(
    val id: Long,
    val name: String,
    val items: List<StockItem>,
)

enum class SortMode(val label: String) {
    OLDEST("古い順"),
    NEWEST("新しい順"),
    NAME("名前順"),
    STOCK_FIRST("在庫順"),
    OUT_FIRST("欠品順"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                StockManagerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockManagerApp() {

    val appBg = Color(0xFFF5F5F5)
    val stockBg = Color.White
    val stockText = Color(0xFF1C1B1F)
    val stockBorder = Color(0xFFE7E0EC)
    val outBg = Color(0xFFF9DEDC)
    val outText = Color(0xFFB3261E)

    var boards by remember {
        mutableStateOf(
            listOf(
                Board(
                    id = 1L,
                    name = "Home",
                    items = listOf(
                        StockItem(1, "しょうゆ", true, 1_000L),
                        StockItem(2, "塩", true, 2_000L),
                        StockItem(3, "こしょう", false, 3_000L),
                        StockItem(4, "みりん", true, 4_000L),
                    )
                ),
                Board(
                    id = 2L,
                    name = "Board 2",
                    items = listOf(
                        StockItem(1, "みそ", true, 1_000L),
                        StockItem(2, "料理酒", true, 2_000L),
                    )
                ),
            )
        )
    }

    var currentBoardId by remember { mutableStateOf(1L) }

    fun currentBoard(): Board = boards.first { it.id == currentBoardId }
    fun currentItems(): List<StockItem> = currentBoard().items

    fun updateItems(newItems: List<StockItem>) {
        boards = boards.map { b ->
            if (b.id == currentBoardId) b.copy(items = newItems) else b
        }
    }

    var showStock by remember { mutableStateOf(true) }
    var showOut by remember { mutableStateOf(true) }
    var sortMode by remember { mutableStateOf(SortMode.OLDEST) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var addModalOpen by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    // 削除アニメ中のID
    val deletingIds = remember { mutableStateListOf<Long>() }
    val scope = rememberCoroutineScope()

    // ボードメニュー
    var drawerOpen by remember { mutableStateOf(false) }

    fun toggleStock() {
        when {
            showStock && !showOut -> {
                showStock = false
                showOut = true
            }
            showStock && showOut -> showStock = false
            else -> showStock = true
        }
    }

    fun toggleOut() {
        when {
            !showStock && showOut -> {
                showOut = false
                showStock = true
            }
            showStock && showOut -> showOut = false
            else -> showOut = true
        }
    }

    fun requestDelete(id: Long) {
        if (deletingIds.contains(id)) return
        deletingIds.add(id)

        scope.launch {
            delay(220) // exitアニメより少し長め
            val newItems = currentItems().filterNot { it.id == id }
            updateItems(newItems)
            deletingIds.remove(id)
        }
    }

    val filteredSortedItems = remember(boards, currentBoardId, showStock, showOut, sortMode, query) {
        val q = query.trim()
        val items = currentItems()

        val filtered = items.filter {
            val passStock = (it.inStock && showStock) || (!it.inStock && showOut)
            val passQuery = q.isEmpty() || it.name.contains(q, ignoreCase = true)
            passStock && passQuery
        }

        when (sortMode) {
            SortMode.OLDEST -> filtered.sortedBy { it.createdAt }
            SortMode.NEWEST -> filtered.sortedByDescending { it.createdAt }
            SortMode.NAME -> filtered.sortedBy { it.name }
            SortMode.STOCK_FIRST -> filtered.sortedWith(compareBy({ !it.inStock }, { it.name }))
            SortMode.OUT_FIRST -> filtered.sortedWith(compareBy({ it.inStock }, { it.name }))
        }
    }

    Scaffold(
        containerColor = appBg,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                ) {
                    CenterAlignedTopAppBar(
                        title = {
                            Box(
                                modifier = Modifier.fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentBoard().name,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { drawerOpen = !drawerOpen }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "メニュー")
                            }
                        },
                        actions = {
                            IconButton(onClick = { searchOpen = !searchOpen }) {
                                Icon(Icons.Filled.Search, contentDescription = "検索")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp)),
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color(0xFFF3EDF7),
                            titleContentColor = Color(0xFF1C1B1F),
                            navigationIconContentColor = Color(0xFF1C1B1F),
                            actionIconContentColor = Color(0xFF1C1B1F)
                        ),
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterSegmentedRow(
                        modifier = Modifier.weight(1.3f),
                        showStock = showStock,
                        showOut = showOut,
                        onStockClick = ::toggleStock,
                        onOutClick = ::toggleOut
                    )
                    SortSplitButton(
                        modifier = Modifier.weight(1f),
                        label = sortMode.label,
                        menuOpen = sortMenuOpen,
                        onMenuOpenChange = { sortMenuOpen = it },
                        onSelect = {
                            sortMode = it
                            sortMenuOpen = false
                        }
                    )
                }

                if (searchOpen) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = { Text("アイテム名で検索") },
                        singleLine = true
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredSortedItems, key = { it.id }) { item ->
                    MagnetCard(
                        item = item,
                        stockBg = stockBg,
                        stockText = stockText,
                        stockBorder = stockBorder,
                        outBg = outBg,
                        outText = outText,
                        editMode = editMode,
                        isDeleting = deletingIds.contains(item.id),
                        onToggle = {
                            if (editMode) return@MagnetCard
                            val itemsNow = currentItems()
                            val newItems = itemsNow.map {
                                if (it.id == item.id) it.copy(inStock = !it.inStock) else it
                            }
                            updateItems(newItems)
                        },
                        onDelete = { requestDelete(item.id) }
                    )
                }
            }

            if (addModalOpen && !editMode) {
                AddModal(
                    onDismiss = { addModalOpen = false },
                    onSave = { name ->
                        val now = System.currentTimeMillis()
                        val itemsNow = currentItems()
                        val nextId = (itemsNow.maxOfOrNull { it.id } ?: 0L) + 1L
                        updateItems(itemsNow + StockItem(nextId, name, true, now))
                        addModalOpen = false
                    }
                )
            }

            FloatingActionButton(
                onClick = {
                    editMode = !editMode
                    if (editMode) addModalOpen = false
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 24.dp, bottom = 24.dp)
                    .size(56.dp),
                shape = CircleShape,
                containerColor = if (editMode) Color(0xFFB3261E) else Color.White,
                contentColor = if (editMode) Color.White else Color(0xFF6750A4)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "編集")
            }

            if (!editMode) {
                FloatingActionButton(
                    onClick = { addModalOpen = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 24.dp, bottom = 24.dp)
                        .size(56.dp),
                    shape = CircleShape,
                    containerColor = Color.White,
                    contentColor = Color(0xFF6750A4)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "追加")
                }
            }
        }
    }

    // ✅ ここがポイント：Scaffold の外（上）に置く
    BoardDrawerOverlay(
        open = drawerOpen,
        boards = boards,
        currentBoardId = currentBoardId,
        onSelectBoard = { id ->
            currentBoardId = id
            drawerOpen = false
        },
        onClose = { drawerOpen = false },
        onManageBoards = {
            // 後ほど実装
            drawerOpen = false
        }
    )
}


@Composable
private fun FilterSegmentedRow(
    modifier: Modifier,
    showStock: Boolean,
    showOut: Boolean,
    onStockClick: () -> Unit,
    onOutClick: () -> Unit
) {
    Row(modifier = modifier.height(40.dp)) {
        SegmentedLikeButton(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            text = "在庫",
            selected = showStock,
            onClick = onStockClick,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                bottomStart = 20.dp,
                topEnd = 0.dp,
                bottomEnd = 0.dp
            )
        )
        SegmentedLikeButton(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            text = "欠品",
            selected = showOut,
            onClick = onOutClick,
            shape = RoundedCornerShape(
                topStart = 0.dp,
                bottomStart = 0.dp,
                topEnd = 20.dp,
                bottomEnd = 20.dp
            )
        )
    }
}

@Composable
private fun SegmentedLikeButton(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    shape: RoundedCornerShape
) {
    val bg = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.White

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text)
        }
    }
}

@Composable
private fun SortSplitButton(
    modifier: Modifier = Modifier,
    label: String,
    menuOpen: Boolean,
    onMenuOpenChange: (Boolean) -> Unit,
    onSelect: (SortMode) -> Unit
) {
    Box(modifier = modifier) {
        val containerColor = MaterialTheme.colorScheme.secondaryContainer
        val contentColor = MaterialTheme.colorScheme.onSecondaryContainer

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            FilledTonalButton(
                onClick = { onMenuOpenChange(true) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    bottomStart = 20.dp
                ),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(label, maxLines = 1)
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
            )

            FilledTonalButton(
                onClick = { onMenuOpenChange(true) },
                modifier = Modifier
                    .width(52.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(
                    topEnd = 20.dp,
                    bottomEnd = 20.dp
                ),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "並び替え"
                )
            }
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { onMenuOpenChange(false) }
        ) {
            SortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.label) },
                    onClick = { onSelect(mode) }
                )
            }
        }
    }
}

@Composable
private fun MagnetCard(
    item: StockItem,
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

    // ひっくり返し（元のアニメに戻す：タップ時だけ animateTo / 状態追従は snapTo）
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

    // 揺れ（弱め：±0.8f。Animatableで互換性重視）
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
                        if (editMode) return@clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddModal(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    fun attemptSave() {
        val name = text.trim()
        if (name.isEmpty()) {
            showError = true
            return
        }
        onSave(name)
        text = ""
        showError = false
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "閉じる")
                    }

                    Text(
                        text = "追加",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        if (showError) {
                            showError = it.trim().isEmpty()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("名前") },
                    placeholder = { Text("名前を入力") },
                    singleLine = true,
                    isError = showError,
                    trailingIcon = {
                        if (text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    text = ""
                                    showError = true
                                }
                            ) {
                                Icon(Icons.Filled.Clear, contentDescription = "クリア")
                            }
                        }
                    },
                    supportingText = {
                        if (showError) {
                            Text("入力してください")
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { attemptSave() })
                )

                Button(
                    onClick = { attemptSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = text.trim().isNotEmpty(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6750A4),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF6750A4).copy(alpha = 0.40f),
                        disabledContentColor = Color.White.copy(alpha = 0.80f)
                    )
                ) {
                    Text("保存")
                }
            }
        }
    }
}

@Composable
private fun BoardDrawerOverlay(
    open: Boolean,
    boards: List<Board>,
    currentBoardId: Long,
    onSelectBoard: (Long) -> Unit,
    onClose: () -> Unit,
    onManageBoards: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    // アニメを Animatable で統一（環境差で animateFloatAsState 等が無くても動く）
    val scrim = remember { Animatable(0f) }
    val panelX = remember { Animatable(-280f) } // dp相当（最後に dp に入れる）

    LaunchedEffect(open) {
        if (open) {
            scope.launch { scrim.animateTo(0.45f, tween(220)) }
            scope.launch { panelX.animateTo(0f, tween(260, easing = FastOutSlowInEasing)) }
        } else {
            scope.launch { scrim.animateTo(0f, tween(180)) }
            scope.launch { panelX.animateTo(-280f, tween(220, easing = FastOutSlowInEasing)) }
        }
    }

    // 透明でもタップを拾えるよう、scrimが完全に0になるまで描画
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
                    Text(
                        text = "ボード",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(boards, key = { it.id }) { b ->
                            val selected = b.id == currentBoardId
                            val bg = if (selected) Color(0xFFF3EDF7) else Color.Transparent

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                color = bg,
                                shape = RoundedCornerShape(12.dp),
                                onClick = { onSelectBoard(b.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = b.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )

                    TextButton(
                        onClick = onManageBoards,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ボードを追加・編集")
                    }
                }
            }
        }
    }
}
