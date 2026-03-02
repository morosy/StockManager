package com.example.stockmanager.data

import com.example.stockmanager.data.db.BoardWithItems
import org.json.JSONArray
import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private const val FORMAT_JSON = "stockmanager-board-export"
private const val FORMAT_TEMPLATE = "stockmanager-board-template"
private const val FORMAT_CSV = "stockmanager-board-export-csv"
private const val SCHEMA_VERSION = 1
private const val MAX_IMPORT_ITEMS = 500

enum class BoardTransferFormat {
    JSON,
    CSV
}

data class ExportPayload(
    val fileName: String,
    val mimeType: String,
    val content: String
)

data class BoardImportItem(
    val name: String,
    val inStock: Boolean,
    val createdAt: Long?,
    val updatedAt: Long?,
    val exportId: String?
)

data class BoardImportData(
    val boardName: String,
    val boardCreatedAt: Long?,
    val boardExportId: String?,
    val items: List<BoardImportItem>
)

object BoardTransferCodec {
    fun exportBoard(boardWithItems: BoardWithItems, format: BoardTransferFormat): ExportPayload {
        return when (format) {
            BoardTransferFormat.JSON -> exportJson(boardWithItems)
            BoardTransferFormat.CSV -> exportCsv(boardWithItems)
        }
    }

    fun import(content: String, requestedFormat: BoardTransferFormat?): Result<BoardImportData> {
        return when (requestedFormat ?: detectFormat(content)) {
            BoardTransferFormat.JSON -> importJson(content)
            BoardTransferFormat.CSV -> importCsv(content)
        }
    }

    private fun exportJson(boardWithItems: BoardWithItems): ExportPayload {
        val board = boardWithItems.board
        val boardExportId = board.exportId ?: "b-${UUID.randomUUID()}"

        val boardJson = JSONObject()
            .put("exportId", boardExportId)
            .put("name", board.name)
            .put("createdAt", board.createdAt)

        val items = JSONArray()
        boardWithItems.items.forEach { item ->
            items.put(
                JSONObject()
                    .put("exportId", item.exportId ?: "i-${UUID.randomUUID()}")
                    .put("name", item.name)
                    .put("inStock", item.inStock)
                    .put("createdAt", item.createdAt)
                    .put("updatedAt", item.updatedAt)
            )
        }
        boardJson.put("items", items)

        val root = JSONObject()
            .put("schemaVersion", SCHEMA_VERSION)
            .put("format", FORMAT_JSON)
            .put("exportedAt", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .put("board", boardJson)

        val safeName = sanitizeFileName(board.name.ifBlank { "board" })
        return ExportPayload(
            fileName = "$safeName.json",
            mimeType = "application/json",
            content = root.toString(2)
        )
    }

    private fun exportCsv(boardWithItems: BoardWithItems): ExportPayload {
        val board = boardWithItems.board
        val boardExportId = board.exportId ?: "b-${UUID.randomUUID()}"
        val sb = StringBuilder()
        sb.appendLine("meta_key,meta_value")
        sb.appendLine(csvLine("schemaVersion", SCHEMA_VERSION.toString()))
        sb.appendLine(csvLine("format", FORMAT_CSV))
        sb.appendLine(csvLine("exportedAt", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
        sb.appendLine(csvLine("boardExportId", boardExportId))
        sb.appendLine(csvLine("boardName", board.name))
        sb.appendLine(csvLine("boardCreatedAt", board.createdAt.toString()))
        sb.appendLine()
        sb.appendLine("type,exportId,name,inStock,createdAt,updatedAt")
        boardWithItems.items.forEach { item ->
            sb.appendLine(
                csvLine(
                    "item",
                    item.exportId ?: "i-${UUID.randomUUID()}",
                    item.name,
                    item.inStock.toString(),
                    item.createdAt.toString(),
                    item.updatedAt.toString()
                )
            )
        }

        val safeName = sanitizeFileName(board.name.ifBlank { "board" })
        return ExportPayload(
            fileName = "$safeName.csv",
            mimeType = "text/csv",
            content = sb.toString()
        )
    }

    private fun importJson(content: String): Result<BoardImportData> = runCatching {
        val root = JSONObject(content)
        val schemaVersion = root.optInt("schemaVersion", -1)
        if (schemaVersion != SCHEMA_VERSION) {
            error("schemaVersion=$schemaVersion は未対応です")
        }

        val boardObject = root.optJSONObject("board") ?: error("board が見つかりません")
        val boardName = boardObject.optString("name").trim()
        if (boardName.isEmpty()) {
            error("board.name が空です")
        }

        val rawItems = boardObject.optJSONArray("items") ?: JSONArray()
        val items = buildList {
            for (i in 0 until minOf(rawItems.length(), MAX_IMPORT_ITEMS)) {
                val itemObj = rawItems.optJSONObject(i) ?: continue
                val itemName = itemObj.optString("name").trim()
                if (itemName.isEmpty()) {
                    continue
                }
                add(
                    BoardImportItem(
                        name = itemName,
                        inStock = itemObj.optBoolean("inStock", true),
                        createdAt = itemObj.optLongOrNull("createdAt"),
                        updatedAt = itemObj.optLongOrNull("updatedAt"),
                        exportId = itemObj.optString("exportId").takeIf { !it.isNullOrBlank() }
                    )
                )
            }
        }

        BoardImportData(
            boardName = boardName,
            boardCreatedAt = boardObject.optLongOrNull("createdAt"),
            boardExportId = boardObject.optString("exportId").takeIf { !it.isNullOrBlank() },
            items = items
        )
    }

    private fun importCsv(content: String): Result<BoardImportData> = runCatching {
        val lines = content
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .split('\n')

        val meta = mutableMapOf<String, String>()
        val items = mutableListOf<BoardImportItem>()
        var inItems = false
        for (line in lines) {
            if (line.isBlank()) {
                continue
            }
            val cols = parseCsvLine(line)
            if (cols.isEmpty()) {
                continue
            }

            if (!inItems) {
                if (cols[0] == "type") {
                    inItems = true
                    continue
                }
                if (cols[0] == "meta_key" || cols.size < 2) {
                    continue
                }
                meta[cols[0]] = cols[1]
                continue
            }

            if (items.size >= MAX_IMPORT_ITEMS) {
                break
            }
            if (cols[0] != "item") {
                continue
            }
            val itemName = cols.getOrNull(2)?.trim().orEmpty()
            if (itemName.isEmpty()) {
                continue
            }
            items += BoardImportItem(
                name = itemName,
                inStock = cols.getOrNull(3)?.toBooleanStrictOrNull() ?: true,
                createdAt = cols.getOrNull(4)?.toLongOrNull(),
                updatedAt = cols.getOrNull(5)?.toLongOrNull(),
                exportId = cols.getOrNull(1)?.takeIf { it.isNotBlank() }
            )
        }

        val schemaVersion = meta["schemaVersion"]?.toIntOrNull() ?: -1
        if (schemaVersion != SCHEMA_VERSION) {
            error("schemaVersion=$schemaVersion は未対応です")
        }

        val boardName = meta["boardName"]?.trim().orEmpty()
        if (boardName.isEmpty()) {
            error("board.name が空です")
        }

        BoardImportData(
            boardName = boardName,
            boardCreatedAt = meta["boardCreatedAt"]?.toLongOrNull(),
            boardExportId = meta["boardExportId"]?.takeIf { it.isNotBlank() },
            items = items
        )
    }

    private fun detectFormat(content: String): BoardTransferFormat {
        val trimmed = content.trimStart()
        return if (trimmed.startsWith("{")) BoardTransferFormat.JSON else BoardTransferFormat.CSV
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i += 2
                }
                ch == '"' -> {
                    inQuotes = !inQuotes
                    i++
                }
                ch == ',' && !inQuotes -> {
                    result += current.toString()
                    current.clear()
                    i++
                }
                else -> {
                    current.append(ch)
                    i++
                }
            }
        }
        result += current.toString()
        return result
    }

    private fun csvLine(vararg values: String): String {
        return values.joinToString(",") { escapeCsvValue(it) }
    }

    private fun escapeCsvValue(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.any { it == ',' || it == '\n' || it == '"' }) "\"$escaped\"" else escaped
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }
}

private fun JSONObject.optLongOrNull(key: String): Long? {
    if (!has(key) || isNull(key)) {
        return null
    }
    return optLong(key)
}
