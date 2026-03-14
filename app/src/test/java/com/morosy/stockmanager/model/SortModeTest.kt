package com.morosy.stockmanager.model

import com.morosy.stockmanager.data.db.StockItemStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SortModeTest {
    @Test
    fun stockFirst_ordersWhiteYellowRed() {
        val sorted = listOf(
            StockItemStatus.OUT_OF_STOCK,
            StockItemStatus.HIGHLIGHTED,
            StockItemStatus.IN_STOCK
        ).sortedBy { SortMode.STOCK_FIRST.statusRank(it) }

        assertEquals(
            listOf(
                StockItemStatus.IN_STOCK,
                StockItemStatus.HIGHLIGHTED,
                StockItemStatus.OUT_OF_STOCK
            ),
            sorted
        )
    }

    @Test
    fun outFirst_ordersRedYellowWhite() {
        val sorted = listOf(
            StockItemStatus.IN_STOCK,
            StockItemStatus.HIGHLIGHTED,
            StockItemStatus.OUT_OF_STOCK
        ).sortedBy { SortMode.OUT_FIRST.statusRank(it) }

        assertEquals(
            listOf(
                StockItemStatus.OUT_OF_STOCK,
                StockItemStatus.HIGHLIGHTED,
                StockItemStatus.IN_STOCK
            ),
            sorted
        )
    }
}
