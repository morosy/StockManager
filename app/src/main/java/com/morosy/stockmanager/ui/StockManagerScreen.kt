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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morosy.stockmanager.R
import com.morosy.stockmanager.data.BoardTransferFormat
import com.morosy.stockmanager.data.ExportPayload
import com.morosy.stockmanager.data.db.StockItemStatus
import com.morosy.stockmanager.model.SortMode
import com.morosy.stockmanager.model.statusRank
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
import com.morosy.stockmanager.ui.overlay.TutorialOverlay
import com.morosy.stockmanager.ui.tutorial.TutorialStep
import com.morosy.stockmanager.ui.tutorial.TutorialTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockManagerScreen(
    viewModel: StockManagerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val appBg = colorScheme.background
    val stockBg = colorScheme.surface
    val stockText = colorScheme.onSurface
    val stockBorder = colorScheme.outline.copy(alpha = 0.35f)
    val outBg = colorScheme.errorContainer
    val outText = colorScheme.onErrorContainer

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
    val hasBoard = currentBoardEntity != null
    val emptyStateTitle = when {
        ui.boards.isEmpty() -> "ボートがありません"
        currentItems.isEmpty() -> "アイテムがありません"
        else -> null
    }
    val emptyStateMessage = when {
        ui.boards.isEmpty() -> "ボードを追加してください"
        currentItems.isEmpty() -> "アイテムを追加してください"
        else -> null
    }

    var sortMenuOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var addItemModalOpen by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }
    var renameItemModalOpen by remember { mutableStateOf(false) }
    var pendingRenameItemId by remember { mutableStateOf<Long?>(null) }
    var pendingRenameItemName by remember { mutableStateOf("") }

    val deletingIds = remember { mutableStateListOf<Long>() }
    var pendingDeleteItemId by remember { mutableStateOf<Long?>(null) }

    var drawerOpen by remember { mutableStateOf(false) }
    var boardEditMode by remember { mutableStateOf(false) }
    var boardAddModalOpen by remember { mutableStateOf(false) }
    var appInfoScreenType by remember { mutableStateOf<AppInfoScreenType?>(null) }
    var pendingDeleteBoardId by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteBoardName by remember { mutableStateOf<String?>(null) }

    var pendingExportPayload by remember { mutableStateOf<ExportPayload?>(null) }
    val tutorialTargets = remember { mutableStateMapOf<TutorialTarget, Rect>() }
    var tutorialVisible by rememberSaveable { mutableStateOf(false) }
    var tutorialStep by rememberSaveable { mutableStateOf(TutorialStep.OPEN_BOARD_LIST) }
    var tutorialAutoStarted by rememberSaveable { mutableStateOf(false) }
    var tutorialIncludesAddBoardStep by rememberSaveable { mutableStateOf(true) }
    var searchFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = ui.query,
                selection = TextRange(ui.query.length)
            )
        )
    }

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

    fun startTutorial() {
        tutorialIncludesAddBoardStep = ui.boards.isEmpty()
        tutorialVisible = true
        tutorialStep = TutorialStep.OPEN_BOARD_LIST
        drawerOpen = false
        boardEditMode = false
        editMode = false
        sortMenuOpen = false
        searchOpen = false
        appInfoScreenType = null
        if (!ui.tutorialSeen) {
            viewModel.markTutorialSeen()
        }
    }

    fun closeTutorial() {
        tutorialVisible = false
        boardEditMode = false
        sortMenuOpen = false
    }

    val tutorialFlow = remember(tutorialIncludesAddBoardStep) {
        TutorialStep.flow(includeAddBoardStep = tutorialIncludesAddBoardStep)
    }
    val tutorialStepIndex = tutorialFlow.indexOf(tutorialStep)
    val tutorialPreviousStep = tutorialFlow.getOrNull(tutorialStepIndex - 1)
    val tutorialNextStep = tutorialFlow.getOrNull(tutorialStepIndex + 1)

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
            val normalizedStatus = StockItemStatus.normalize(item.status)
            val passStock =
                (StockItemStatus.isStockVisible(normalizedStatus) && ui.showStock) ||
                    (normalizedStatus == StockItemStatus.OUT_OF_STOCK && ui.showOut)
            val passQuery = q.isEmpty() || item.name.contains(q, ignoreCase = true)
            passStock && passQuery
        }

        when (ui.sortMode) {
            SortMode.OLDEST -> filtered.sortedBy { it.createdAt }
            SortMode.NEWEST -> filtered.sortedByDescending { it.createdAt }
            SortMode.NAME -> filtered.sortedBy { it.name }
            SortMode.NAME_DESC -> filtered.sortedByDescending { it.name }
            SortMode.STOCK_FIRST -> filtered.sortedWith(
                compareBy({ ui.sortMode.statusRank(it.status) }, { it.name })
            )
            SortMode.OUT_FIRST -> filtered.sortedWith(
                compareBy({ ui.sortMode.statusRank(it.status) }, { it.name })
            )
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

    fun openRenameItem(itemId: Long, itemName: String) {
        pendingRenameItemId = itemId
        pendingRenameItemName = itemName
        renameItemModalOpen = true
    }

    fun closeRenameItem() {
        renameItemModalOpen = false
        pendingRenameItemId = null
        pendingRenameItemName = ""
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

    LaunchedEffect(ui.query) {
        if (ui.query != searchFieldValue.text) {
            searchFieldValue = TextFieldValue(
                text = ui.query,
                selection = TextRange(ui.query.length)
            )
        }
    }

    LaunchedEffect(hasBoard) {
        if (!hasBoard) {
            editMode = false
            addItemModalOpen = false
        }
    }

    LaunchedEffect(ui.tutorialSeen, ui.shouldAutoStartTutorial, ui.settingsResolved) {
        if (ui.settingsResolved && ui.shouldAutoStartTutorial && !ui.tutorialSeen && !tutorialAutoStarted) {
            tutorialAutoStarted = true
            startTutorial()
        }
    }

    LaunchedEffect(drawerOpen, boardEditMode, hasBoard) {
        if (!drawerOpen) {
            tutorialTargets.remove(TutorialTarget.BOARD_EDIT)
            tutorialTargets.remove(TutorialTarget.BOARD_ADD)
        }
        if (!hasBoard) {
            tutorialTargets.remove(TutorialTarget.ITEM_ADD_FAB)
            tutorialTargets.remove(TutorialTarget.ITEM_EDIT_FAB)
            tutorialTargets.remove(TutorialTarget.BOARD_LIST)
            tutorialTargets.remove(TutorialTarget.CURRENT_BOARD_ITEM)
            tutorialTargets.remove(TutorialTarget.BOARD_TITLE)
            tutorialTargets.remove(TutorialTarget.FILTER_ROW)
            tutorialTargets.remove(TutorialTarget.SORT_BUTTON)
        }
        if (!boardEditMode) {
            tutorialTargets.remove(TutorialTarget.BOARD_ADD)
        }
    }

    LaunchedEffect(tutorialVisible, tutorialStep) {
        if (!tutorialVisible) {
            return@LaunchedEffect
        }
        when (tutorialStep) {
            TutorialStep.OPEN_BOARD_LIST -> {
                drawerOpen = false
                boardEditMode = false
                editMode = false
            }
            TutorialStep.OPEN_BOARD_EDIT -> {
                drawerOpen = true
                boardEditMode = false
                editMode = false
            }
            TutorialStep.ADD_BOARD -> {
                drawerOpen = true
                boardEditMode = true
                editMode = false
            }
            TutorialStep.ADD_ITEM,
            TutorialStep.FILTER_ITEMS,
            TutorialStep.SORT_ITEMS,
            TutorialStep.TUTORIAL_REMINDER -> {
                drawerOpen = false
                boardEditMode = false
                editMode = false
            }
            TutorialStep.EDIT_ITEM -> {
                drawerOpen = false
                boardEditMode = false
            }
            TutorialStep.BOARD_LIST_OVERVIEW,
            TutorialStep.BOARD_DISPLAY_SWITCH -> {
                drawerOpen = true
                boardEditMode = false
                editMode = false
            }
            TutorialStep.RENAME_BOARD -> {
                drawerOpen = false
                boardEditMode = false
                editMode = false
            }
        }
    }

    LaunchedEffect(tutorialVisible, tutorialStep, ui.boards.size, boardAddModalOpen) {
        if (!tutorialVisible || tutorialStep != TutorialStep.ADD_BOARD) {
            return@LaunchedEffect
        }
        if (!boardAddModalOpen && ui.boards.isNotEmpty()) {
            tutorialStep = TutorialStep.ADD_ITEM
        }
    }

    val tutorialTargetRect = tutorialTargets[tutorialStep.target]
    val hideTutorialOverlay =
        !tutorialVisible ||
            (tutorialStep == TutorialStep.ADD_BOARD && boardAddModalOpen)

    val tutorialSupportingMessage = when {
        tutorialStep == TutorialStep.TUTORIAL_REMINDER -> null
        tutorialStep == TutorialStep.ADD_BOARD && ui.boards.isEmpty() -> "ボードを追加すると次へ進めます"
        tutorialTargetRect == null -> "表示を準備しています..."
        else -> null
    }

    val tutorialCanAdvance = when (tutorialStep) {
        TutorialStep.TUTORIAL_REMINDER -> true
        TutorialStep.ADD_BOARD -> tutorialTargetRect != null && ui.boards.isNotEmpty()
        else -> tutorialTargetRect != null
    }

    fun onTutorialTargetTap() {
        when (tutorialStep) {
            TutorialStep.OPEN_BOARD_LIST -> {
                drawerOpen = true
                tutorialStep = TutorialStep.OPEN_BOARD_EDIT
            }
            TutorialStep.OPEN_BOARD_EDIT -> {
                boardEditMode = true
                tutorialStep = tutorialNextStep ?: TutorialStep.OPEN_BOARD_EDIT
            }
            TutorialStep.ADD_BOARD -> {
                boardAddModalOpen = true
            }
            TutorialStep.ADD_ITEM -> {
                tutorialStep = TutorialStep.EDIT_ITEM
            }
            TutorialStep.EDIT_ITEM -> {
                editMode = true
                tutorialStep = TutorialStep.BOARD_LIST_OVERVIEW
            }
            TutorialStep.BOARD_LIST_OVERVIEW -> tutorialStep = TutorialStep.BOARD_DISPLAY_SWITCH
            TutorialStep.BOARD_DISPLAY_SWITCH -> tutorialStep = TutorialStep.RENAME_BOARD
            TutorialStep.RENAME_BOARD -> tutorialStep = TutorialStep.FILTER_ITEMS
            TutorialStep.FILTER_ITEMS,
            TutorialStep.SORT_ITEMS,
            TutorialStep.TUTORIAL_REMINDER -> Unit
        }
    }

    fun advanceTutorial() {
        when (tutorialStep) {
            TutorialStep.OPEN_BOARD_LIST,
            TutorialStep.OPEN_BOARD_EDIT,
            TutorialStep.ADD_ITEM,
            TutorialStep.EDIT_ITEM,
            TutorialStep.BOARD_LIST_OVERVIEW,
            TutorialStep.BOARD_DISPLAY_SWITCH,
            TutorialStep.RENAME_BOARD -> onTutorialTargetTap()
            TutorialStep.ADD_BOARD -> {
                if (ui.boards.isNotEmpty()) {
                    tutorialStep = TutorialStep.ADD_ITEM
                } else {
                    onTutorialTargetTap()
                }
            }
            TutorialStep.FILTER_ITEMS,
            TutorialStep.SORT_ITEMS -> {
                val next = tutorialNextStep
                if (next == null) {
                    closeTutorial()
                } else {
                    tutorialStep = next
                }
            }
            TutorialStep.TUTORIAL_REMINDER -> closeTutorial()
        }
    }

    fun goBackTutorial() {
        tutorialPreviousStep?.let { previous ->
            tutorialStep = previous
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
                                        text = currentBoardName,
                                        modifier = Modifier
                                            .tutorialTarget(TutorialTarget.BOARD_TITLE, tutorialTargets)
                                            .clickable(enabled = currentBoardEntity != null) {
                                                renameOpen = true
                                            },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { drawerOpen = true },
                                    modifier = Modifier.tutorialTarget(TutorialTarget.NAV_MENU, tutorialTargets)
                                ) {
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
                                containerColor = colorScheme.surfaceVariant,
                                titleContentColor = colorScheme.onSurface,
                                navigationIconContentColor = colorScheme.onSurface,
                                actionIconContentColor = colorScheme.onSurface
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
                            modifier = Modifier
                                .weight(1.3f)
                                .tutorialTarget(TutorialTarget.FILTER_ROW, tutorialTargets),
                            showStock = ui.showStock,
                            showOut = ui.showOut,
                            onStockClick = { viewModel.toggleStock() },
                            onOutClick = { viewModel.toggleOut() }
                        )
                        SortSplitButton(
                            modifier = Modifier
                                .weight(1f)
                                .tutorialTarget(TutorialTarget.SORT_BUTTON, tutorialTargets),
                            currentMode = ui.sortMode,
                            menuOpen = sortMenuOpen,
                            onMenuOpenChange = { sortMenuOpen = it },
                            onSelect = {
                                viewModel.setSortMode(it)
                                sortMenuOpen = false
                            }
                        )
                    }

                    if (searchOpen) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchFieldValue,
                                onValueChange = { value ->
                                    searchFieldValue = value
                                    viewModel.setQuery(value.text)
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("アイテム名で検索") },
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp)
                            )
                            TextButton(
                                onClick = {
                                    searchFieldValue = TextFieldValue("")
                                    viewModel.setQuery("")
                                    searchOpen = false
                                },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("閉じる")
                            }
                        }
                    }

                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (emptyStateTitle != null && emptyStateMessage != null) {
                    EmptyHomeMessage(
                        title = emptyStateTitle,
                        message = emptyStateMessage,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                    )
                } else {
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
                                onEditName = { openRenameItem(item.id, item.name) },
                                onDelete = { requestDeleteItem(item.id) }
                            )
                        }
                    }
                }

                if (addItemModalOpen && !editMode) {
                    AddItemModal(
                        errorMessage = { name ->
                            when {
                                name.isEmpty() -> null
                                currentItems.any { it.name == name } -> "同じ名前のアイテムは追加できません"
                                else -> null
                            }
                        },
                        onDismiss = { addItemModalOpen = false },
                        onSave = { name ->
                            val boardId = ui.currentBoardId
                            if (boardId != 0L) {
                                viewModel.addItem(boardId, name) { added ->
                                    if (added) {
                                        addItemModalOpen = false
                                    }
                                }
                            } else {
                                addItemModalOpen = false
                            }
                        }
                    )
                }

                if (renameItemModalOpen) {
                    AddItemModal(
                        title = "名称変更",
                        initialText = pendingRenameItemName,
                        confirmLabel = "保存",
                        placeholder = "アイテム名を入力",
                        onDismiss = { closeRenameItem() },
                        onSave = { name ->
                            pendingRenameItemId?.let { itemId ->
                                viewModel.renameItem(itemId, name)
                            }
                            closeRenameItem()
                        }
                    )
                }

                if (editMode && hasBoard) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(24.dp)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "アイテムをタップで名称変更",
                            color = colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (hasBoard) {
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
                            .size(56.dp)
                            .tutorialTarget(TutorialTarget.ITEM_EDIT_FAB, tutorialTargets),
                        shape = CircleShape,
                        containerColor = if (editMode) colorScheme.errorContainer else colorScheme.surface,
                        contentColor = if (editMode) colorScheme.onErrorContainer else colorScheme.primary
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "編集")
                    }
                }

                if (hasBoard && !editMode) {
                    FloatingActionButton(
                        onClick = { addItemModalOpen = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(end = 24.dp, bottom = 24.dp)
                            .size(56.dp)
                            .tutorialTarget(TutorialTarget.ITEM_ADD_FAB, tutorialTargets),
                        shape = CircleShape,
                        containerColor = colorScheme.surface,
                        contentColor = colorScheme.primary
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
            boardEditButtonModifier = Modifier.tutorialTarget(TutorialTarget.BOARD_EDIT, tutorialTargets),
            addBoardButtonModifier = Modifier.tutorialTarget(TutorialTarget.BOARD_ADD, tutorialTargets),
            boardListModifier = Modifier.tutorialTarget(TutorialTarget.BOARD_LIST, tutorialTargets),
            currentBoardItemModifier = Modifier.tutorialTarget(TutorialTarget.CURRENT_BOARD_ITEM, tutorialTargets),
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
            onOpenHowToUse = { startTutorial() },
            onOpenAbout = { appInfoScreenType = AppInfoScreenType.ABOUT },
            onOpenTerms = { appInfoScreenType = AppInfoScreenType.TERMS },
            onOpenOssLicenses = { appInfoScreenType = AppInfoScreenType.OSS_LICENSES },
            onOpenContact = {
                Toast.makeText(context, "外部サイトへアクセスします", Toast.LENGTH_SHORT).show()
                scope.launch(Dispatchers.Main) {
                    delay(600)
                    runCatching {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://morosy.github.io/contact.html")
                        )
                        context.startActivity(intent)
                    }.onFailure { e ->
                        Toast.makeText(context, "ブラウザを起動できませんでした: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
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

        if (!hideTutorialOverlay) {
            TutorialOverlay(
                step = tutorialStep,
                targetRect = tutorialTargetRect,
                progressIndex = tutorialStepIndex + 1,
                progressTotal = tutorialFlow.size,
                canGoBack = tutorialPreviousStep != null,
                isLastStep = tutorialNextStep == null,
                canAdvance = tutorialCanAdvance,
                supportingMessage = tutorialSupportingMessage,
                onTargetTap = { onTutorialTargetTap() },
                onBack = { goBackTutorial() },
                onAdvance = {
                    if (tutorialCanAdvance) {
                        advanceTutorial()
                    }
                },
                onSkip = { closeTutorial() }
            )
        }
    }
}

@Composable
private fun EmptyHomeMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Modifier.tutorialTarget(
    target: TutorialTarget,
    registry: MutableMap<TutorialTarget, Rect>
): Modifier {
    return this.onGloballyPositioned { coordinates ->
        registry[target] = coordinates.boundsInRoot()
    }
}







