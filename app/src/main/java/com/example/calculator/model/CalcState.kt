// [新增] 界面状态：表达式、预览结果、错误信息与历史记录
package com.example.calculator.model

data class CalcState(
    val expression: String = "",
    val preview: String = "0",
    val error: String? = null,
    // [新增] 结果对应的大写金额，输入过程中为空
    val upper: String? = null,
    val history: List<HistoryItem> = emptyList(),
)
