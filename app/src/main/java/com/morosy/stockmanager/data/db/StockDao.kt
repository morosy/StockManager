package com.morosy.stockmanager.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    // --- Boards ---
    @Query("SELECT * FROM boards ORDER BY id ASC")
    fun observeBoards(): Flow<List<BoardEntity>>

    @Transaction
    @Query("SELECT * FROM boards ORDER BY sort_order ASC, id ASC")
    fun observeBoardsWithItems(): Flow<List<BoardWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: BoardEntity): Long

    @Query("DELETE FROM boards WHERE id = :boardId")
    suspend fun deleteBoardById(boardId: Long): Int

    // --- Items ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StockItemEntity): Long

    @Update
    suspend fun updateItem(item: StockItemEntity): Int

    @Query("UPDATE stock_items SET name = :newName, updated_at = :updatedAt WHERE id = :itemId")
    suspend fun renameItem(itemId: Long, newName: String, updatedAt: Long): Int

    @Query("DELETE FROM stock_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Long): Int

    @Query("SELECT COUNT(*) FROM boards")
    suspend fun countBoards(): Int
}

