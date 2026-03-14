package com.morosy.stockmanager.data

import com.morosy.stockmanager.data.db.BoardEntity
import com.morosy.stockmanager.data.db.BoardWithItems
import com.morosy.stockmanager.data.db.StockItemEntity
import com.morosy.stockmanager.data.db.StockItemStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTransferCodecTest {
    @Test
    fun importCsv_supportsLegacyFormatWithoutStatusColumn() {
        val csv = """
            meta_key,meta_value
            schemaVersion,1
            format,stockmanager-board-export-csv
            boardName,Legacy Board
            boardCreatedAt,1700000000000

            type,exportId,name,inStock,createdAt,updatedAt
            item,i-1,White,true,1700000001000,1700000001000
            item,i-2,Red,false,1700000002000,1700000002000
        """.trimIndent()

        val result = BoardTransferCodec.import(csv, BoardTransferFormat.CSV).getOrThrow()

        assertEquals("Legacy Board", result.boardName)
        assertEquals(2, result.items.size)
        assertEquals(StockItemStatus.IN_STOCK, result.items[0].status)
        assertEquals(StockItemStatus.OUT_OF_STOCK, result.items[1].status)
    }

    @Test
    fun exportAndImportCsv_preservesHighlightedStatus() {
        val board = BoardWithItems(
            board = BoardEntity(id = 1L, name = "Board", createdAt = 1700000000000, exportId = "b-1"),
            items = listOf(
                StockItemEntity(
                    id = 10L,
                    boardId = 1L,
                    name = "Yellow",
                    status = StockItemStatus.HIGHLIGHTED,
                    createdAt = 1700000001000,
                    updatedAt = 1700000002000,
                    exportId = "i-10"
                )
            )
        )

        val exported = BoardTransferCodec.exportBoard(board, BoardTransferFormat.CSV)
        val imported = BoardTransferCodec.import(exported.content, BoardTransferFormat.CSV).getOrThrow()

        assertTrue(exported.content.contains("type,exportId,name,status,inStock,createdAt,updatedAt"))
        assertEquals(1, imported.items.size)
        assertEquals(StockItemStatus.HIGHLIGHTED, imported.items.single().status)
    }
}
