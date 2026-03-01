package com.example.stockmanager.ui

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.stockmanager.model.Board
import com.example.stockmanager.model.SortMode
import com.example.stockmanager.model.StockItem
import com.example.stockmanager.ui.components.FilterSegmentedRow
import com.example.stockmanager.ui.components.MagnetCard
import com.example.stockmanager.ui.components.SortSplitButton
import com.example.stockmanager.ui.modal.AddItemModal
import com.example.stockmanager.ui.overlay.BoardAddModal
import com.example.stockmanager.ui.overlay.BoardDrawerOverlay
import com.example.stockmanager.ui.overlay.ConfirmBoardDeleteDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockManagerScreen() {
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
            if (b.id == currentBoardId) {
                b.copy(items = newItems)
            } else {
                b
            }
        }
    }

    var showStock by remember { mutableStateOf(true) }
    var showOut by remember { mutableStateOf(true) }
    var sortMode by remember { mutableStateOf(SortMode.OLDEST) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var addItemModalOpen by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    // アイテム削除アニメ中のID
    val deletingIds = remember { mutableStateListOf<Long>() }
    val scope = rememberCoroutineScope()

    // ボードメニュー
    var drawerOpen by remember { mutableStateOf(false) }

    // ボード管理（編集）モード
    var boardEditMode by remember { mutableStateOf(false) }

    // ボード追加モーダル
    var boardAddModalOpen by remember { mutableStateOf(false) }

    // ボード削除確認（対象ボード）
    var pendingDeleteBoard by remember { mutableStateOf<Board?>(null) }

    fun toggleStock() {
        when {
            showStock && !showOut -> {
                showStock = false
                showOut = true
            }
            showStock && showOut -> {
                showStock = false
            }
            else -> {
                showStock = true
            }
        }
    }

    fun toggleOut() {
        when {
            !showStock && showOut -> {
                showOut = false
                showStock = true
            }
            showStock && showOut -> {
                showOut = false
            }
            else -> {
                showOut = true
            }
        }
    }

    fun requestDeleteItem(id: Long) {
        if (deletingIds.contains(id)) {
            return
        }
        deletingIds.add(id)

        scope.launch {
            delay(220)
            val newItems = currentItems().filterNot { it.id == id }
            updateItems(newItems)
            deletingIds.remove(id)
        }
    }

    fun addBoard(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return
        }
        val nextId = (boards.maxOfOrNull { it.id } ?: 0L) + 1L
        boards = boards + Board(
            id = nextId,
            name = trimmed,
            items = emptyList()
        )
    }

    fun deleteBoard(boardId: Long) {
        val remaining = boards.filterNot { it.id == boardId }
        if (remaining.isEmpty()) {
            return
        }

        boards = remaining

        if (currentBoardId == boardId) {
            currentBoardId = remaining.first().id
        }
    }

    val filteredSortedItems = remember(boards, currentBoardId, showStock, showOut, sortMode, query) {
        val q = query.trim()
        val items = currentItems()

        val filtered = items.filter { it ->
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                                IconButton(
                                    onClick = { drawerOpen = !drawerOpen }
                                ) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "メニュー")
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { searchOpen = !searchOpen }
                                ) {
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
                            onStockClick = { toggleStock() },
                            onOutClick = { toggleOut() }
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
                                if (editMode) {
                                    return@MagnetCard
                                }
                                val itemsNow = currentItems()
                                val newItems = itemsNow.map { now ->
                                    if (now.id == item.id) {
                                        now.copy(inStock = !now.inStock)
                                    } else {
                                        now
                                    }
                                }
                                updateItems(newItems)
                            },
                            onDelete = { requestDeleteItem(item.id) }
                        )
                    }
                }

                if (addItemModalOpen && !editMode) {
                    AddItemModal(
                        onDismiss = { addItemModalOpen = false },
                        onSave = { name ->
                            val now = System.currentTimeMillis()
                            val itemsNow = currentItems()
                            val nextId = (itemsNow.maxOfOrNull { it.id } ?: 0L) + 1L
                            updateItems(itemsNow + StockItem(nextId, name, true, now))
                            addItemModalOpen = false
                        }
                    )
                }

                FloatingActionButton(
                    onClick = {
                        editMode = !editMode
                        if (editMode) {
                            addItemModalOpen = false
                        }
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
                        onClick = { addItemModalOpen = true },
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

        if (boardAddModalOpen) {
            BoardAddModal(
                onDismiss = { boardAddModalOpen = false },
                onSave = { name ->
                    addBoard(name)
                    boardAddModalOpen = false
                }
            )
        }

        pendingDeleteBoard?.let { target ->
            ConfirmBoardDeleteDialog(
                boardName = target.name,
                onConfirm = {
                    deleteBoard(target.id)
                    pendingDeleteBoard = null
                },
                onCancel = {
                    pendingDeleteBoard = null
                }
            )
        }

        BoardDrawerOverlay(
            open = drawerOpen,
            boards = boards,
            currentBoardId = currentBoardId,
            editMode = boardEditMode,
            onSelectBoard = { id ->
                currentBoardId = id
                drawerOpen = false
            },
            onClose = {
                drawerOpen = false
                boardEditMode = false
            },
            onEnterEdit = { boardEditMode = true },
            onExitEdit = { boardEditMode = false },
            onAddBoard = { boardAddModalOpen = true },
            onRequestDeleteBoard = { board ->
                pendingDeleteBoard = board
            }
        )
    }
}
