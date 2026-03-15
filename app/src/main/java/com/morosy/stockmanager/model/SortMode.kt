package com.morosy.stockmanager.model

import com.morosy.stockmanager.data.db.StockItemStatus

enum class SortMode(val label: String) {
    OLDEST("古い順"),
    NEWEST("新しい順"),
    NAME("名前順"),
    NAME_DESC("名前逆順"),
    STOCK_FIRST("在庫順"),
    OUT_FIRST("欠品順"),
}

fun SortMode.togglePair(): SortMode? {
    return when (this) {
        SortMode.OLDEST -> SortMode.NEWEST
        SortMode.NEWEST -> SortMode.OLDEST
        SortMode.NAME -> SortMode.NAME_DESC
        SortMode.NAME_DESC -> SortMode.NAME
        SortMode.STOCK_FIRST -> SortMode.OUT_FIRST
        SortMode.OUT_FIRST -> SortMode.STOCK_FIRST
    }
}

fun SortMode.toggleArrowRotation(): Float {
    return when (this) {
        SortMode.NEWEST,
        SortMode.NAME_DESC,
        SortMode.OUT_FIRST -> 180f
        else -> 0f
    }
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
