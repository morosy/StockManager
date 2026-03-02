package com.example.stockmanager.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "boards"
)
data class BoardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String
)

@Entity(
    tableName = "stock_items",
    foreignKeys = [
        ForeignKey(
            entity = BoardEntity::class,
            parentColumns = ["id"],
            childColumns = ["board_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["board_id"])
    ]
)
data class StockItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "board_id")
    val boardId: Long,

    val name: String,

    @ColumnInfo(name = "in_stock")
    val inStock: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

// 二重定義になるためコメントアウト
/**
 * 設定（永続化したい「選択中のボードID」など）
 * 単一行で持ちたいので id=0 固定。
 */
// @Entity(tableName = "settings")
// data class SettingsEntity(
//     @PrimaryKey
//     val id: Int = 0,

//     @ColumnInfo(name = "current_board_id")
//     val currentBoardId: Long
// )
