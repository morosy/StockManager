package com.morosy.stockmanager.data.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockItemStatusTest {
    @Test
    fun next_cyclesAcrossThreeStates() {
        assertEquals(StockItemStatus.HIGHLIGHTED, StockItemStatus.next(StockItemStatus.IN_STOCK))
        assertEquals(StockItemStatus.OUT_OF_STOCK, StockItemStatus.next(StockItemStatus.HIGHLIGHTED))
        assertEquals(StockItemStatus.IN_STOCK, StockItemStatus.next(StockItemStatus.OUT_OF_STOCK))
    }

    @Test
    fun normalize_fallsBackToWhiteForUnknownValues() {
        assertEquals(StockItemStatus.IN_STOCK, StockItemStatus.normalize(-1))
        assertEquals(StockItemStatus.IN_STOCK, StockItemStatus.normalize(99))
    }

    @Test
    fun legacyBooleanMappingTreatsYellowAsStockSide() {
        assertEquals(StockItemStatus.IN_STOCK, StockItemStatus.fromLegacyInStock(true))
        assertEquals(StockItemStatus.OUT_OF_STOCK, StockItemStatus.fromLegacyInStock(false))
        assertTrue(StockItemStatus.isStockVisible(StockItemStatus.HIGHLIGHTED))
    }
}
