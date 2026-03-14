package com.morosy.stockmanager.model

import com.morosy.stockmanager.data.db.StockItemStatus

enum class SortMode(val label: String) {
    OLDEST("古い順"),
    NEWEST("新しい順"),
    NAME("名前順"),
    STOCK_FIRST("在庫順"),
    OUT_FIRST("欠品順"),
}

fun SortMode.statusRank(status: Int): Int {
    return when (this) {
        SortMode.STOCK_FIRST -> when (StockItemStatus.normalize(status)) {
            StockItemStatus.IN_STOCK -> 0
            StockItemStatus.HIGHLIGHTED -> 1
            StockItemStatus.OUT_OF_STOCK -> 2
            else -> 0
        }

        SortMode.OUT_FIRST -> when (StockItemStatus.normalize(status)) {
            StockItemStatus.OUT_OF_STOCK -> 0
            StockItemStatus.HIGHLIGHTED -> 1
            StockItemStatus.IN_STOCK -> 2
            else -> 2
        }

        else -> StockItemStatus.normalize(status)
    }
}
