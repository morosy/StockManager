package com.morosy.stockmanager.ui.shopping

import com.morosy.stockmanager.data.db.BoardEntity
import com.morosy.stockmanager.data.db.BoardWithItems
import com.morosy.stockmanager.data.db.StockItemEntity
import com.morosy.stockmanager.data.db.StockItemStatus
import com.morosy.stockmanager.model.SortMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ShoppingListModelsTest {
    @Test
    fun buildShoppingListSections_picksYellowAndRedOnly() {
        val sections = buildShoppingListSections(
            boards = listOf(
                BoardWithItems(
                    board = BoardEntity(id = 10L, name = "洗面所"),
                    items = listOf(
                        item(id = 1L, boardId = 10L, name = "歯ブラシ", status = StockItemStatus.IN_STOCK),
                        item(id = 2L, boardId = 10L, name = "洗剤", status = StockItemStatus.HIGHLIGHTED),
                        item(id = 3L, boardId = 10L, name = "石けん", status = StockItemStatus.OUT_OF_STOCK)
                    )
                )
            ),
            selectedBoardIds = setOf(10L),
            sortMode = SortMode.OLDEST
        )

        assertEquals(1, sections.size)
        assertEquals(listOf("洗剤", "石けん"), sections.first().items.map { it.name })
    }

    @Test
    fun buildShoppingListSections_keepsBoardOrderAndSkipsEmptySections() {
        val sections = buildShoppingListSections(
            boards = listOf(
                BoardWithItems(
                    board = BoardEntity(id = 1L, name = "キッチン"),
                    items = listOf(
                        item(id = 1L, boardId = 1L, name = "塩", status = StockItemStatus.OUT_OF_STOCK)
                    )
                ),
                BoardWithItems(
                    board = BoardEntity(id = 2L, name = "浴室"),
                    items = listOf(
                        item(id = 2L, boardId = 2L, name = "スポンジ", status = StockItemStatus.IN_STOCK)
                    )
                ),
                BoardWithItems(
                    board = BoardEntity(id = 3L, name = "洗面所"),
                    items = listOf(
                        item(id = 3L, boardId = 3L, name = "ティッシュ", status = StockItemStatus.HIGHLIGHTED)
                    )
                )
            ),
            selectedBoardIds = setOf(1L, 2L, 3L),
            sortMode = SortMode.OLDEST
        )

        assertEquals(listOf("キッチン", "洗面所"), sections.map { it.boardName })
    }

    @Test
    fun buildShoppingListSections_usesDisplaySortModeInsideEachBoard() {
        val sections = buildShoppingListSections(
            boards = listOf(
                BoardWithItems(
                    board = BoardEntity(id = 5L, name = "食品"),
                    items = listOf(
                        item(id = 1L, boardId = 5L, name = "りんご", status = StockItemStatus.OUT_OF_STOCK),
                        item(id = 2L, boardId = 5L, name = "みかん", status = StockItemStatus.HIGHLIGHTED),
                        item(id = 3L, boardId = 5L, name = "あずき", status = StockItemStatus.OUT_OF_STOCK)
                    )
                )
            ),
            selectedBoardIds = setOf(5L),
            sortMode = SortMode.NAME
        )

        assertEquals(
            listOf("あずき", "みかん", "りんご"),
            sections.first().items.map { it.name }
        )
    }

    private fun item(
        id: Long,
        boardId: Long,
        name: String,
        status: Int
    ): StockItemEntity {
        return StockItemEntity(
            id = id,
            boardId = boardId,
            name = name,
            status = status,
            createdAt = id,
            updatedAt = id
        )
    }
}
