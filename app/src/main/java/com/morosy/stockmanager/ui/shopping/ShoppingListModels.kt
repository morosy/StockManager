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

private fun shoppingListStatusRank(status: Int): Int {
    return when (StockItemStatus.normalize(status)) {
        StockItemStatus.HIGHLIGHTED -> 0
        StockItemStatus.OUT_OF_STOCK -> 1
        else -> 2
    }
}

fun sortShoppingListItems(items: List<StockItemEntity>, sortMode: SortMode): List<StockItemEntity> {
    val comparator = when (sortMode) {
        SortMode.OLDEST -> compareBy<StockItemEntity> { it.createdAt }
            .thenBy { it.name }

        SortMode.NEWEST -> compareByDescending<StockItemEntity> { it.createdAt }
            .thenBy { it.name }

        SortMode.NAME -> compareBy<StockItemEntity> { it.name }
            .thenBy { it.createdAt }

        SortMode.NAME_DESC -> compareByDescending<StockItemEntity> { it.name }
            .thenBy { it.createdAt }

        SortMode.STOCK_FIRST,
        SortMode.OUT_FIRST -> compareBy<StockItemEntity> { it.name }
            .thenBy { it.createdAt }
    }

    return items.sortedWith(
        compareBy<StockItemEntity> { shoppingListStatusRank(it.status) }
            .then(comparator)
    )
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
            items = sortShoppingListItems(pickedItems, sortMode)
        )
    }
}
