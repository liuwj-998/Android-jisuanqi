// [新增] 历史记录持久化：SharedPreferences + JSON，最多保留 100 条
package com.example.calculator.data

import android.content.Context
import com.example.calculator.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class HistoryRepository(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<HistoryItem>> = _items.asStateFlow()

    fun record(item: HistoryItem) {
        val updated = (listOf(item) + _items.value)
            .distinctBy { it.expression to it.result }
            .take(MAX_ITEMS)
        save(updated)
        _items.value = updated
    }

    fun clear() {
        save(emptyList())
        _items.value = emptyList()
    }

    private fun load(): List<HistoryItem> {
        val raw = preferences.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        HistoryItem(
                            expression = item.getString(FIELD_EXPRESSION),
                            result = item.getString(FIELD_RESULT),
                            timestamp = item.optLong(FIELD_TIMESTAMP),
                            value = item.optString(FIELD_VALUE),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun save(items: List<HistoryItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put(FIELD_EXPRESSION, item.expression)
                    .put(FIELD_RESULT, item.result)
                    .put(FIELD_TIMESTAMP, item.timestamp)
                    .put(FIELD_VALUE, item.value),
            )
        }
        preferences.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private companion object {
        const val PREFS_NAME = "calculator_history"
        const val KEY_ITEMS = "items"
        const val FIELD_EXPRESSION = "expression"
        const val FIELD_RESULT = "result"
        const val FIELD_TIMESTAMP = "timestamp"
        const val FIELD_VALUE = "value"
        const val MAX_ITEMS = 100
    }
}
