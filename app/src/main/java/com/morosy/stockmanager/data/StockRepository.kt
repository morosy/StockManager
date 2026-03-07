package com.morosy.stockmanager.data

import androidx.room.withTransaction
import com.morosy.stockmanager.MAX_BOARD_NAME_LENGTH
import com.morosy.stockmanager.MAX_ITEM_NAME_LENGTH
import com.morosy.stockmanager.data.db.AppDatabase
import com.morosy.stockmanager.data.db.BoardEntity
import com.morosy.stockmanager.data.db.BoardWithItems
import com.morosy.stockmanager.data.db.SettingsEntity
import com.morosy.stockmanager.data.db.StockItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class StockRepository(private val db: AppDatabase) {
    private val stockDao = db.stockDao()
    private val settingsDao = db.settingsDao()
    private val boardDao = db.boardDao()

    fun observeBoardsWithItems(): Flow<List<BoardWithItems>> {
        return stockDao.observeBoardsWithItems()
    }

    fun observeSettings(): Flow<SettingsEntity?> {
        return settingsDao.observe()
    }

    suspend fun setCurrentBoardId(boardId: Long) {
        settingsDao.upsert(SettingsEntity(id = 0, currentBoardId = boardId))
    }

    suspend fun updateBoardOrders(orderedIds: List<Long>) {
        boardDao.updateBoardOrders(orderedIds)
    }

    suspend fun ensureSeeded() {
        if (stockDao.countBoards() > 0) {
            return
        }

        val now = System.currentTimeMillis()
        val homeId = stockDao.insertBoard(
            BoardEntity(
                name = "ボード１".take(MAX_BOARD_NAME_LENGTH),
                createdAt = now,
                sortOrder = 0
            )
        )

        settingsDao.upsert(SettingsEntity(id = 0, currentBoardId = homeId))
    }

    suspend fun addBoard(name: String): Long {
        val normalized = name.trim().take(MAX_BOARD_NAME_LENGTH)
        require(normalized.isNotEmpty()) { "board name is empty" }
        val nextOrder = stockDao.countBoards()
        val now = System.currentTimeMillis()
        return stockDao.insertBoard(BoardEntity(name = normalized, createdAt = now, sortOrder = nextOrder))
    }

    suspend fun deleteBoard(boardId: Long) {
        stockDao.deleteBoardById(boardId)
    }

    suspend fun addItem(boardId: Long, name: String) {
        val normalized = name.trim().take(MAX_ITEM_NAME_LENGTH)
        require(normalized.isNotEmpty()) { "item name is empty" }
        val now = System.currentTimeMillis()
        stockDao.insertItem(
            StockItemEntity(
                boardId = boardId,
                name = normalized,
                inStock = true,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun toggleItem(item: StockItemEntity) {
        stockDao.updateItem(item.copy(inStock = !item.inStock, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteItem(itemId: Long) {
        stockDao.deleteItemById(itemId)
    }

    suspend fun updateSettings(transform: (SettingsEntity) -> SettingsEntity) {
        val current = settingsDao.getOnce() ?: SettingsEntity()
        settingsDao.upsert(transform(current))
    }

    suspend fun renameBoard(boardId: Long, newName: String) {
        val normalized = newName.trim().take(MAX_BOARD_NAME_LENGTH)
        if (normalized.isEmpty()) {
            return
        }
        boardDao.renameBoard(boardId, normalized)
    }

    suspend fun buildBoardExport(boardId: Long, format: BoardTransferFormat): ExportPayload? {
        val boardWithItems = observeBoardsWithItems().first().firstOrNull { it.board.id == boardId } ?: return null
        return BoardTransferCodec.exportBoard(boardWithItems, format)
    }

    suspend fun importBoard(content: String, requestedFormat: BoardTransferFormat?): Result<Long> = withContext(Dispatchers.IO) {
        val decoded = BoardTransferCodec.import(content, requestedFormat).getOrElse {
            return@withContext Result.failure(it)
        }
        val now = System.currentTimeMillis()
        val trimmedBoardName = decoded.boardName.trim().take(MAX_BOARD_NAME_LENGTH)
        if (trimmedBoardName.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("board.name が空です"))
        }

        runCatching {
            db.withTransaction {
                val newBoardId = stockDao.insertBoard(
                    BoardEntity(
                        name = trimmedBoardName,
                        createdAt = now,
                        exportId = decoded.boardExportId ?: "b-${UUID.randomUUID()}",
                        sortOrder = stockDao.countBoards()
                    )
                )

                decoded.items.take(500).forEach { item ->
                    val itemName = item.name.trim().take(MAX_ITEM_NAME_LENGTH)
                    if (itemName.isEmpty()) {
                        return@forEach
                    }
                    stockDao.insertItem(
                        StockItemEntity(
                            boardId = newBoardId,
                            name = itemName,
                            inStock = item.inStock,
                            createdAt = item.createdAt ?: now,
                            updatedAt = item.updatedAt ?: now,
                            exportId = item.exportId ?: "i-${UUID.randomUUID()}"
                        )
                    )
                }
                newBoardId
            }
        }
    }
}

