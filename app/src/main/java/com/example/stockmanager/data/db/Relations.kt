package com.example.stockmanager.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class BoardWithItems(
    @Embedded val board: BoardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "board_id"
    )
    val items: List<StockItemEntity>
)
