package com.example.stockmanager.ui

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmanager.model.SortMode
import com.example.stockmanager.ui.components.FilterSegmentedRow
import com.example.stockmanager.ui.components.MagnetCard
import com.example.stockmanager.ui.components.SortSplitButton
import com.example.stockmanager.ui.modal.AddItemModal
import com.example.stockmanager.ui.overlay.BoardAddModal
import com.example.stockmanager.ui.overlay.BoardDrawerOverlay
import com.example.stockmanager.ui.overlay.ConfirmBoardDeleteDialog
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockManagerScreen(
    viewModel: StockManagerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val appBg = Color(0xFFF5F5F5)
    val stockBg = Color.White
    val stockText = Color(0xFF1C1B1F)
    val stockBorder = Color(0xFFE7E0EC)
    val outBg = Color(0xFFF9DEDC)
    val outText = Color(0xFFB3261E)

    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    // UIローカル状態（永続化不要）
    var sortMenuOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var addItemModalOpen by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    // 削除アニメ中ID（UIだけ）
    val deletingIds = remember { mutableStateListOf<Long>() }
    var pendingDeleteItemId by remember { mutableStateOf<Long?>(null) }

    // ボードメニュー
    var drawerOpen by remember { mutableStateOf(false) }
    var boardEditMode by remember { mutableStateOf(false) }
    var boardAddModalOpen by remember { mutableStateOf(false) }
    var pendingDeleteBoardId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteBoardName by remember { mutableStateOf<String?>(null) }

    val currentBoard = ui.boards.firstOrNull { it.board.id == ui.currentBoardId }
    val currentBoardName = currentBoard?.board?.name ?: "..."

    val currentItems = currentBoard?.items ?: emptyList()

    val filteredSortedItems = remember(
        currentItems,
        ui.showStock,
        ui.showOut,
        ui.sortMode,
        ui.query
    ) {
        val q = ui.query.trim()

        val filtered = currentItems.filter { it ->
            val passStock = (it.inStock && ui.showStock) || (!it.inStock && ui.showOut)
            val passQuery = q.isEmpty() || it.name.contains(q, ignoreCase = true)
            passStock && passQuery
        }

        when (ui.sortMode) {
            SortMode.OLDEST -> filtered.sortedBy { it.createdAt }
            SortMode.NEWEST -> filtered.sortedByDescending { it.createdAt }
            SortMode.NAME -> filtered.sortedBy { it.name }
            SortMode.STOCK_FIRST -> filtered.sortedWith(compareBy({ !it.inStock }, { it.name }))
            SortMode.OUT_FIRST -> filtered.sortedWith(compareBy({ it.inStock }, { it.name }))
        }
    }
    fun requestDeleteItem(itemId: Long) {
        if (deletingIds.contains(itemId)) {
            return
        }
        deletingIds.add(itemId)
        pendingDeleteItemId = itemId
    }

    LaunchedEffect(pendingDeleteItemId) {
        val id = pendingDeleteItemId ?: return@LaunchedEffect
        kotlinx.coroutines.delay(220)
        viewModel.deleteItem(id)
        deletingIds.remove(id)
        pendingDeleteItemId = null
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
                                        text = currentBoardName,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { drawerOpen = true }) {
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
                            showStock = ui.showStock,
                            showOut = ui.showOut,
                            onStockClick = { viewModel.toggleStock() },
                            onOutClick = { viewModel.toggleOut() }
                        )
                        SortSplitButton(
                            modifier = Modifier.weight(1f),
                            label = ui.sortMode.label,
                            menuOpen = sortMenuOpen,
                            onMenuOpenChange = { sortMenuOpen = it },
                            onSelect = {
                                viewModel.setSortMode(it)
                                sortMenuOpen = false
                            }
                        )
                    }

                    if (searchOpen) {
                        OutlinedTextField(
                            value = ui.query,
                            onValueChange = { viewModel.setQuery(it) },
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
                                viewModel.toggleItem(item)
                            },
                            onDelete = { requestDeleteItem(item.id) }
                        )
                    }
                }

                if (addItemModalOpen && !editMode) {
                    AddItemModal(
                        onDismiss = { addItemModalOpen = false },
                        onSave = { name ->
                            val boardId = ui.currentBoardId
                            if (boardId != 0L) {
                                viewModel.addItem(boardId, name)
                            }
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
                    viewModel.addBoard(name)
                    boardAddModalOpen = false
                }
            )
        }

        if (pendingDeleteBoardId != null && pendingDeleteBoardName != null) {
            ConfirmBoardDeleteDialog(
                boardName = pendingDeleteBoardName!!,
                onConfirm = {
                    viewModel.deleteBoard(pendingDeleteBoardId!!)
                    pendingDeleteBoardId = null
                    pendingDeleteBoardName = null
                },
                onCancel = {
                    pendingDeleteBoardId = null
                    pendingDeleteBoardName = null
                }
            )
        }

        BoardDrawerOverlay(
            open = drawerOpen,
            boards = ui.boards.map { it.board }, // overlayは Board(model) じゃなく BoardEntity 相当を想定
            currentBoardId = ui.currentBoardId,
            editMode = boardEditMode,
            onSelectBoard = { id ->
                viewModel.selectBoard(id)
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
                pendingDeleteBoardId = board.id
                pendingDeleteBoardName = board.name
            }
        )
    }
}
