package com.example.stockmanager.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.stockmanager.model.Board
import com.example.stockmanager.model.StockItem
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.stockManagerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "stock_manager_datastore"
)

class StockManagerLocalStore(
    private val context: Context,
) {
    private val keyStateJson = stringPreferencesKey("state_json")

    data class PersistedState(
        val currentBoardId: Long,
        val boards: List<Board>,
    )

    suspend fun load(): PersistedState? {
        val prefs = context.stockManagerDataStore.data.first()
        val json = prefs[keyStateJson] ?: return null

        return try {
            decode(json)
        } catch (_: Throwable) {
            // JSON が壊れていたら一旦クリア（落ちないこと優先）
            context.stockManagerDataStore.edit { it.remove(keyStateJson) }
            null
        }
    }

    suspend fun save(state: PersistedState) {
        val json = encode(state)
        context.stockManagerDataStore.edit { prefs ->
            prefs[keyStateJson] = json
        }
    }

    private fun encode(state: PersistedState): String {
        val root = JSONObject()
        root.put("v", 1)
        root.put("currentBoardId", state.currentBoardId)

        val boardsArr = JSONArray()
        for (b in state.boards) {
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("name", b.name)

            val itemsArr = JSONArray()
            for (it in b.items) {
                val itObj = JSONObject()
                itObj.put("id", it.id)
                itObj.put("name", it.name)
                itObj.put("inStock", it.inStock)
                itObj.put("createdAt", it.createdAt)
                itemsArr.put(itObj)
            }
            bObj.put("items", itemsArr)
            boardsArr.put(bObj)
        }

        root.put("boards", boardsArr)
        return root.toString()
    }

    private fun decode(json: String): PersistedState {
        val root = JSONObject(json)
        val boardsArr = root.getJSONArray("boards")

        val boards = buildList {
            for (i in 0 until boardsArr.length()) {
                val bObj = boardsArr.getJSONObject(i)
                val itemsArr = bObj.getJSONArray("items")

                val items = buildList {
                    for (j in 0 until itemsArr.length()) {
                        val itObj = itemsArr.getJSONObject(j)
                        add(
                            StockItem(
                                id = itObj.getLong("id"),
                                name = itObj.getString("name"),
                                inStock = itObj.getBoolean("inStock"),
                                createdAt = itObj.getLong("createdAt"),
                            )
                        )
                    }
                }

                add(
                    Board(
                        id = bObj.getLong("id"),
                        name = bObj.getString("name"),
                        items = items,
                    )
                )
            }
        }

        return PersistedState(
            currentBoardId = root.getLong("currentBoardId"),
            boards = boards,
        )
    }
}
