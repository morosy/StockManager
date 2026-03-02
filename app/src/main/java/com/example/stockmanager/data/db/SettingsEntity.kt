package com.example.stockmanager.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Long = 0L,

    @ColumnInfo(name = "current_board_id")
    val currentBoardId: Long? = null,

    @ColumnInfo(name = "show_stock")
    val showStock: Boolean = true,

    @ColumnInfo(name = "show_out")
    val showOut: Boolean = true,

    @ColumnInfo(name = "sort_mode")
    val sortMode: String = "OLDEST",

    @ColumnInfo(name = "query")
    val query: String = ""
)
