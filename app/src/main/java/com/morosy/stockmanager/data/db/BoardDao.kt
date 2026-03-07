package com.morosy.stockmanager.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface BoardDao {

    @Query("UPDATE boards SET sort_order = :order WHERE id = :boardId")
    suspend fun updateSortOrder(boardId: Long, order: Int)

    @Transaction
    suspend fun updateBoardOrders(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            updateSortOrder(id, index)
        }
    }

    @Query("UPDATE boards SET name = :newName WHERE id = :boardId")
    suspend fun renameBoard(boardId: Long, newName: String)

    @Query("SELECT * FROM boards WHERE id = :boardId LIMIT 1")
    suspend fun getBoardOnce(boardId: Long): BoardEntity?
}

