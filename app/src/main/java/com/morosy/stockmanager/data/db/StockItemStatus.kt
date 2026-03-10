package com.morosy.stockmanager.data.db

object StockItemStatus {
    const val IN_STOCK = 0
    const val HIGHLIGHTED = 1
    const val OUT_OF_STOCK = 2

    fun normalize(status: Int): Int {
        return when (status) {
            IN_STOCK, HIGHLIGHTED, OUT_OF_STOCK -> status
            else -> IN_STOCK
        }
    }

    fun next(status: Int): Int {
        return when (normalize(status)) {
            IN_STOCK -> HIGHLIGHTED
            HIGHLIGHTED -> OUT_OF_STOCK
            else -> IN_STOCK
        }
    }

    fun fromLegacyInStock(inStock: Boolean): Int {
        return if (inStock) IN_STOCK else OUT_OF_STOCK
    }

    fun isStockVisible(status: Int): Boolean {
        return normalize(status) != OUT_OF_STOCK
    }
}
