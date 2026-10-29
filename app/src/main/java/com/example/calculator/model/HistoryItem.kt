// [新增] 历史记录条目
package com.example.calculator.model

data class HistoryItem(
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
)
