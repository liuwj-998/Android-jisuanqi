// [新增] 历史记录条目：value 保存未格式化的结果，供点击回填后继续计算
package com.example.calculator.model

data class HistoryItem(
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis(),
    val value: String = "",
)
