package com.morosy.stockmanager

import com.morosy.stockmanager.data.db.StockItemStatus
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for core business logic in StockManager.
 */
class StockItemStatusTest {
    @Test
    fun testNormalize() {
        assertEquals(StockItemStatus.IN_STOCK, StockItemStatus.normalize(0))
        assertEquals(StockItemStatus.HIGHLIGHTED, StockItemStatus.normalize(1))
        assertEquals(StockItemStatus.OUT_OF_STOCK, StockItemStatus.normalize(2))
        assertEquals(StockItemStatus.IN_STOCK, StockItemStatus.normalize(-1))
        assertEquals(StockItemStatus.IN_STOCK, StockItemStatus.normalize(999))
    }

    @Test
    fun testNext() {
        assertEquals(StockItemStatus.HIGHLIGHTED, StockItemStatus.next(StockItemStatus.IN_STOCK))
        assertEquals(StockItemStatus.OUT_OF_STOCK, StockItemStatus.next(StockItemStatus.HIGHLIGHTED))
        assertEquals(StockItemStatus.IN_STOCK, StockItemStatus.next(StockItemStatus.OUT_OF_STOCK))
    }

    @Test
    fun testFromLegacyInStock() {
        assertEquals(StockItemStatus.IN_STOCK, StockItemStatus.fromLegacyInStock(true))
        assertEquals(StockItemStatus.OUT_OF_STOCK, StockItemStatus.fromLegacyInStock(false))
    }

    @Test
    fun testIsStockVisible() {
        assertTrue(StockItemStatus.isStockVisible(StockItemStatus.IN_STOCK))
        assertTrue(StockItemStatus.isStockVisible(StockItemStatus.HIGHLIGHTED))
        assertFalse(StockItemStatus.isStockVisible(StockItemStatus.OUT_OF_STOCK))
    }
}

