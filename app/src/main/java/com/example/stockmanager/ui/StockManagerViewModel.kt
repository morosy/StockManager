package com.example.stockmanager.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmanager.data.StockRepository
import com.example.stockmanager.data.db.AppDatabase
import com.example.stockmanager.data.db.BoardWithItems
import com.example.stockmanager.data.db.SettingsEntity
import com.example.stockmanager.data.db.StockItemEntity
import com.example.stockmanager.model.SortMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
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

    private val showStock = MutableStateFlow(true)
    private val showOut = MutableStateFlow(true)
    private val sortMode = MutableStateFlow(SortMode.OLDEST)
    private val query = MutableStateFlow("")

    private val boardsFlow: Flow<List<BoardWithItems>> = repo.observeBoardsWithItems()
    private val settingsFlow: Flow<SettingsEntity?> = repo.observeSettings()

    val uiState: StateFlow<StockManagerUiState> =
        combine(
            boardsFlow,
            settingsFlow,
            showStock,
            showOut,
            sortMode,
            query
        ) { values: Array<Any?> ->

            @Suppress("UNCHECKED_CAST")
            val boards = values[0] as List<BoardWithItems>

            val settings = values[1] as SettingsEntity?
            val stock = values[2] as Boolean
            val out = values[3] as Boolean
            val sort = values[4] as SortMode
            val q = values[5] as String

            val preferredId: Long? = settings?.currentBoardId

            val safeCurrentId: Long = when {
                boards.isEmpty() -> 0L
                preferredId != null && boards.any { it.board.id == preferredId } -> preferredId
                else -> boards.first().board.id
            }

            StockManagerUiState(
                boards = boards,
                currentBoardId = safeCurrentId,
                showStock = stock,
                showOut = out,
                sortMode = sort,
                query = q
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
        query.value = value
    }

    fun setSortMode(mode: SortMode) {
        sortMode.value = mode
    }

    fun toggleStock() {
        val s = showStock.value
        val o = showOut.value

        when {
            s && !o -> {
                showStock.value = false
                showOut.value = true
            }
            s && o -> {
                showStock.value = false
            }
            else -> {
                showStock.value = true
            }
        }
    }

    fun toggleOut() {
        val s = showStock.value
        val o = showOut.value

        when {
            !s && o -> {
                showOut.value = false
                showStock.value = true
            }
            s && o -> {
                showOut.value = false
            }
            else -> {
                showOut.value = true
            }
        }
    }

    fun selectBoard(boardId: Long) {
        viewModelScope.launch {
            repo.setCurrentBoardId(boardId)
        }
    }

    fun addBoard(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return
        }
        viewModelScope.launch {
            val id = repo.addBoard(trimmed)
            repo.setCurrentBoardId(id)
        }
    }

    fun deleteBoard(boardId: Long) {
        viewModelScope.launch {
            repo.deleteBoard(boardId)
        }
    }

    fun addItem(boardId: Long, name: String) {
        val trimmed = name.trim()
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
}
