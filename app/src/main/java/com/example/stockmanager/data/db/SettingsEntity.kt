package com.example.stockmanager.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 0,

    @ColumnInfo(name = "current_board_id")
    val currentBoardId: Long? = null
)
