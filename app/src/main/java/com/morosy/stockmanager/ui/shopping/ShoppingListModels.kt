package com.morosy.stockmanager.ui.shopping

import com.morosy.stockmanager.data.db.BoardWithItems
import com.morosy.stockmanager.data.db.StockItemEntity
import com.morosy.stockmanager.data.db.StockItemStatus
import com.morosy.stockmanager.model.SortMode
import com.morosy.stockmanager.model.statusRank

data class ShoppingListBoardSection(
    val boardId: Long,
    val boardName: String,
    val items: List<StockItemEntity>
)

fun sortItemsForDisplay(items: List<StockItemEntity>, sortMode: SortMode): List<StockItemEntity> {
    return when (sortMode) {
        SortMode.OLDEST -> items.sortedBy { it.createdAt }
        SortMode.NEWEST -> items.sortedByDescending { it.createdAt }
        SortMode.NAME -> items.sortedBy { it.name }
        SortMode.NAME_DESC -> items.sortedByDescending { it.name }
        SortMode.STOCK_FIRST,
        SortMode.OUT_FIRST -> items.sortedWith(
            compareBy({ sortMode.statusRank(it.status) }, { it.name })
        )
    }
}

fun buildShoppingListSections(
    boards: List<BoardWithItems>,
    selectedBoardIds: Set<Long>,
    sortMode: SortMode
): List<ShoppingListBoardSection> {
    if (selectedBoardIds.isEmpty()) {
        return emptyList()
    }

    return boards.mapNotNull { boardWithItems ->
        if (boardWithItems.board.id !in selectedBoardIds) {
            return@mapNotNull null
        }

        val pickedItems = boardWithItems.items.filter { item ->
            when (StockItemStatus.normalize(item.status)) {
                StockItemStatus.HIGHLIGHTED,
                StockItemStatus.OUT_OF_STOCK -> true

                else -> false
            }
        }

        if (pickedItems.isEmpty()) {
            return@mapNotNull null
        }

        ShoppingListBoardSection(
            boardId = boardWithItems.board.id,
            boardName = boardWithItems.board.name,
            items = sortItemsForDisplay(pickedItems, sortMode)
        )
    }
}
