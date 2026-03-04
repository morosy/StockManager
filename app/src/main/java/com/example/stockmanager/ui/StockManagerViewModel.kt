package com.example.stockmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanager.MAX_BOARD_NAME_LENGTH
import com.example.stockmanager.MAX_ITEM_NAME_LENGTH
import com.example.stockmanager.data.BoardTransferFormat
import com.example.stockmanager.data.ExportPayload
import com.example.stockmanager.data.StockRepository
import com.example.stockmanager.data.db.AppDatabase
import com.example.stockmanager.data.db.BoardWithItems
import com.example.stockmanager.data.db.SettingsEntity
import com.example.stockmanager.data.db.StockItemEntity
import com.example.stockmanager.model.SortMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StockManagerUiState(
    val boards: List<BoardWithItems> = emptyList(),
    val currentBoardId: Long = 0L,
    val showStock: Boolean = true,
    val showOut: Boolean = true,
    val sortMode: SortMode = SortMode.OLDEST,
    val query: String = ""
)

class StockManagerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = StockRepository(AppDatabase.getInstance(app))

    private val boardsFlow = repo.observeBoardsWithItems()
    private val settingsFlow = repo.observeSettings()

    val uiState: StateFlow<StockManagerUiState> =
        combine(boardsFlow, settingsFlow) { boards, settings ->
            val s = settings ?: SettingsEntity()

            val safeCurrentId = when {
                boards.isEmpty() -> 0L
                s.currentBoardId != null && boards.any { it.board.id == s.currentBoardId } -> s.currentBoardId
                else -> boards.first().board.id
            } ?: 0L

            StockManagerUiState(
                boards = boards,
                currentBoardId = safeCurrentId,
                showStock = s.showStock,
                showOut = s.showOut,
                sortMode = SortMode.valueOf(s.sortMode),
                query = s.query
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StockManagerUiState()
        )

    init {
        viewModelScope.launch {
            repo.ensureSeeded()
        }
    }

    fun setQuery(value: String) {
        viewModelScope.launch {
            repo.updateSettings { it.copy(query = value) }
        }
    }

    fun setSortMode(mode: SortMode) {
        viewModelScope.launch {
            repo.updateSettings { it.copy(sortMode = mode.name) }
        }
    }

    fun toggleStock() {
        viewModelScope.launch {
            repo.updateSettings { s ->
                val nextShowStock = !s.showStock
                val nextShowOut = if (!nextShowStock && !s.showOut) true else s.showOut
                s.copy(showStock = nextShowStock, showOut = nextShowOut)
            }
        }
    }

    fun toggleOut() {
        viewModelScope.launch {
            repo.updateSettings { s ->
                val nextShowOut = !s.showOut
                val nextShowStock = if (!nextShowOut && !s.showStock) true else s.showStock
                s.copy(showOut = nextShowOut, showStock = nextShowStock)
            }
        }
    }

    fun selectBoard(boardId: Long) {
        viewModelScope.launch {
            repo.updateSettings { it.copy(currentBoardId = boardId) }
        }
    }

    fun addBoard(name: String) {
        val trimmed = name.trim().take(MAX_BOARD_NAME_LENGTH)
        if (trimmed.isEmpty()) {
            return
        }
        viewModelScope.launch {
            val id = repo.addBoard(trimmed)
            repo.updateSettings { it.copy(currentBoardId = id) }
        }
    }

    fun deleteBoard(boardId: Long) {
        viewModelScope.launch {
            repo.deleteBoard(boardId)
        }
    }

    fun addItem(boardId: Long, name: String) {
        val trimmed = name.trim().take(MAX_ITEM_NAME_LENGTH)
        if (trimmed.isEmpty()) {
            return
        }
        viewModelScope.launch {
            repo.addItem(boardId, trimmed)
        }
    }

    fun toggleItem(item: StockItemEntity) {
        viewModelScope.launch {
            repo.toggleItem(item)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            repo.deleteItem(itemId)
        }
    }

    fun reorderBoards(orderedIds: List<Long>) {
        viewModelScope.launch {
            repo.updateBoardOrders(orderedIds)
        }
    }

    fun renameBoard(boardId: Long, newName: String) {
        val trimmed = newName.trim().take(MAX_BOARD_NAME_LENGTH)
        if (trimmed.isEmpty()) {
            return
        }
        viewModelScope.launch {
            repo.renameBoard(boardId, trimmed)
        }
    }

    fun exportCurrentBoard(format: BoardTransferFormat, onResult: (Result<ExportPayload>) -> Unit) {
        val boardId = uiState.value.currentBoardId
        if (boardId == 0L) {
            onResult(Result.failure(IllegalStateException("ボードが選択されていません")))
            return
        }

        viewModelScope.launch {
            val payload = repo.buildBoardExport(boardId, format)
            if (payload == null) {
                onResult(Result.failure(IllegalStateException("エクスポート対象のボードが見つかりません")))
            } else {
                onResult(Result.success(payload))
            }
        }
    }

    fun importBoard(content: String, format: BoardTransferFormat?, onResult: (Result<Long>) -> Unit) {
        viewModelScope.launch {
            val result = repo.importBoard(content, format)
            result.onSuccess { newBoardId ->
                repo.updateSettings { it.copy(currentBoardId = newBoardId) }
            }
            onResult(result)
        }
    }
}
