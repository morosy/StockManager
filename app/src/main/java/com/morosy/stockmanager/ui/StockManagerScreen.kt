package com.morosy.stockmanager.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morosy.stockmanager.data.BoardTransferFormat
import com.morosy.stockmanager.data.ExportPayload
import com.morosy.stockmanager.model.SortMode
import com.morosy.stockmanager.ui.components.FilterSegmentedRow
import com.morosy.stockmanager.ui.components.MagnetCard
import com.morosy.stockmanager.ui.components.SortSplitButton
import com.morosy.stockmanager.ui.modal.AddItemModal
import com.morosy.stockmanager.ui.overlay.AppInfoScreenOverlay
import com.morosy.stockmanager.ui.overlay.AppInfoScreenType
import com.morosy.stockmanager.ui.overlay.BoardAddModal
import com.morosy.stockmanager.ui.overlay.BoardDrawerOverlay
import com.morosy.stockmanager.ui.overlay.ConfirmBoardDeleteDialog
import com.morosy.stockmanager.ui.overlay.RenameBoardOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appVersionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        }.getOrDefault("")
    }

    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    var renameOpen by remember { mutableStateOf(false) }
    val currentBoardWithItems = ui.boards.firstOrNull { it.board.id == ui.currentBoardId }
    val currentBoardEntity = currentBoardWithItems?.board
    val currentBoardName = currentBoardEntity?.name ?: ""
    val currentItems = currentBoardWithItems?.items ?: emptyList()

    var sortMenuOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var addItemModalOpen by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    val deletingIds = remember { mutableStateListOf<Long>() }
    var pendingDeleteItemId by remember { mutableStateOf<Long?>(null) }

    var drawerOpen by remember { mutableStateOf(false) }
    var boardEditMode by remember { mutableStateOf(false) }
    var boardAddModalOpen by remember { mutableStateOf(false) }
    var appInfoScreenType by remember { mutableStateOf<AppInfoScreenType?>(null) }
    var pendingDeleteBoardId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteBoardName by remember { mutableStateOf<String?>(null) }

    var pendingExportPayload by remember { mutableStateOf<ExportPayload?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        val payload = pendingExportPayload
        pendingExportPayload = null

        if (uri == null || payload == null) {
            return@rememberLauncherForActivityResult
        }

        scope.launch(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(payload.content.toByteArray(Charsets.UTF_8))
                } ?: error("出力ストリームを開けませんでした")
            }.onSuccess {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "エクスポートしました", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "エクスポート失敗: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        scope.launch(Dispatchers.IO) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: error("入力ストリームを開けませんでした")
            }

            text.onSuccess { content ->
                val mimeType = context.contentResolver.getType(uri)
                val fileName = uri.lastPathSegment.orEmpty()
                val format = when {
                    mimeType == "application/json" -> BoardTransferFormat.JSON
                    mimeType == "text/csv" -> BoardTransferFormat.CSV
                    fileName.endsWith(".json", ignoreCase = true) -> BoardTransferFormat.JSON
                    fileName.endsWith(".csv", ignoreCase = true) -> BoardTransferFormat.CSV
                    else -> null
                }
                viewModel.importBoard(content, format) { result ->
                    result.onSuccess {
                        Toast.makeText(context, "ボードをインポートしました", Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        Toast.makeText(context, "インポート失敗: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.onFailure { e ->
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "ファイル読み込み失敗: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val filteredSortedItems = remember(
        currentItems,
        ui.showStock,
        ui.showOut,
        ui.sortMode,
        ui.query
    ) {
        val q = ui.query.trim()

        val filtered = currentItems.filter { item ->
            val passStock = (item.inStock && ui.showStock) || (!item.inStock && ui.showOut)
            val passQuery = q.isEmpty() || item.name.contains(q, ignoreCase = true)
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

    fun runExport(format: BoardTransferFormat) {
        viewModel.exportCurrentBoard(format) { result ->
            result.onSuccess { payload ->
                pendingExportPayload = payload
                createDocumentLauncher.launch(payload.fileName)
            }.onFailure { e ->
                Toast.makeText(context, "エクスポート失敗: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun loadRawText(resId: Int): String {
        return runCatching {
            context.resources.openRawResource(resId).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrDefault("")
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
                                        modifier = Modifier.clickable(enabled = currentBoardEntity != null) {
                                            renameOpen = true
                                        },
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
                                .clip(RoundedCornerShape(28.dp)),
                            expandedHeight = 56.dp,
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
                                if (!editMode) {
                                    viewModel.toggleItem(item)
                                }
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
            boards = ui.boards.map { it.board },
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
            },
            onExportBoardJson = { runExport(BoardTransferFormat.JSON) },
            onExportBoardCsv = { runExport(BoardTransferFormat.CSV) },
            onImportBoard = {
                openDocumentLauncher.launch(
                    arrayOf(
                        "application/json",
                        "text/csv",
                        "text/comma-separated-values"
                    )
                )
            },
            onCreateBoardFromTool = {
                Toast.makeText(context, "外部サイトへアクセスします", Toast.LENGTH_SHORT).show()
                scope.launch(Dispatchers.Main) {
                    delay(600)
                    runCatching {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://morosy.github.io/sm_template_maker.html")
                        )
                        context.startActivity(intent)
                    }.onFailure { e ->
                        Toast.makeText(context, "ブラウザを起動できませんでした: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onOpenHowToUse = { appInfoScreenType = AppInfoScreenType.HOW_TO_USE },
            onOpenAbout = { appInfoScreenType = AppInfoScreenType.ABOUT },
            onOpenOssLicenses = { appInfoScreenType = AppInfoScreenType.OSS_LICENSES },
            onOpenPrivacyPolicy = { appInfoScreenType = AppInfoScreenType.PRIVACY_POLICY },
            onReorderBoards = { ids -> viewModel.reorderBoards(ids) }
        )

        appInfoScreenType?.let { screenType ->
            AppInfoScreenOverlay(
                type = screenType,
                onClose = { appInfoScreenType = null },
                appVersion = appVersionName,
                textLoader = { resId -> loadRawText(resId) }
            )
        }

        RenameBoardOverlay(
            open = renameOpen,
            initialName = currentBoardName,
            onDismiss = { renameOpen = false },
            onRename = { newName ->
                currentBoardEntity?.let { viewModel.renameBoard(it.id, newName) }
                renameOpen = false
            }
        )
    }
}

