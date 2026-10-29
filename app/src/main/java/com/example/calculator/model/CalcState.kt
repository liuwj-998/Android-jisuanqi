// [新增] 界面状态：表达式、预览结果、错误信息与历史记录
package com.example.calculator.model

data class CalcState(
    val expression: String = "",
    val preview: String = "0",
    val error: String? = null,
    val history: List<HistoryItem> = emptyList(),
)
