package com.example.stockmanager.data

import com.example.stockmanager.data.db.AppDatabase
import com.example.stockmanager.data.db.BoardEntity
import com.example.stockmanager.data.db.BoardWithItems
import com.example.stockmanager.data.db.SettingsEntity
import com.example.stockmanager.data.db.StockItemEntity
import kotlinx.coroutines.flow.Flow

class StockRepository(
    private val db: AppDatabase
) {
    private val stockDao = db.stockDao()
    private val settingsDao = db.settingsDao()

    fun observeBoardsWithItems(): Flow<List<BoardWithItems>> {
        return stockDao.observeBoardsWithItems()
    }

    fun observeSettings(): Flow<SettingsEntity?> {
        return settingsDao.observeSettings()
    }

    suspend fun setCurrentBoardId(boardId: Long) {
        settingsDao.upsert(SettingsEntity(id = 0, currentBoardId = boardId))
    }

    suspend fun ensureSeeded() {
        if (stockDao.countBoards() > 0) {
            return
        }

        val now = System.currentTimeMillis()

        val homeId = stockDao.insertBoard(BoardEntity(name = "Home"))
        val board2Id = stockDao.insertBoard(BoardEntity(name = "Board 2"))

        stockDao.insertItem(StockItemEntity(boardId = homeId, name = "しょうゆ", inStock = true, createdAt = now + 1))
        stockDao.insertItem(StockItemEntity(boardId = homeId, name = "塩", inStock = true, createdAt = now + 2))
        stockDao.insertItem(StockItemEntity(boardId = homeId, name = "こしょう", inStock = false, createdAt = now + 3))
        stockDao.insertItem(StockItemEntity(boardId = homeId, name = "みりん", inStock = true, createdAt = now + 4))

        stockDao.insertItem(StockItemEntity(boardId = board2Id, name = "みそ", inStock = true, createdAt = now + 5))
        stockDao.insertItem(StockItemEntity(boardId = board2Id, name = "料理酒", inStock = true, createdAt = now + 6))

        settingsDao.upsert(SettingsEntity(id = 0, currentBoardId = homeId))
    }

    suspend fun addBoard(name: String): Long {
        return stockDao.insertBoard(BoardEntity(name = name))
    }

    suspend fun deleteBoard(boardId: Long) {
        stockDao.deleteBoardById(boardId)
    }

    suspend fun addItem(boardId: Long, name: String) {
        val now = System.currentTimeMillis()
        stockDao.insertItem(
            StockItemEntity(
                boardId = boardId,
                name = name,
                inStock = true,
                createdAt = now
            )
        )
    }

    suspend fun toggleItem(item: StockItemEntity) {
        stockDao.updateItem(item.copy(inStock = !item.inStock))
    }

    suspend fun deleteItem(itemId: Long) {
        stockDao.deleteItemById(itemId)
    }
}
