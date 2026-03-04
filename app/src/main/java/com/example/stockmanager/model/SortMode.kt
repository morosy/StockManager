package com.example.stockmanager.model

enum class SortMode(val label: String) {
    OLDEST("古い順"),
    NEWEST("新しい順"),
    NAME("名前順"),
    STOCK_FIRST("在庫順"),
    OUT_FIRST("欠品順"),
}
