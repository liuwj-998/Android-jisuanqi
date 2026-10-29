// [新增] 连续等号支持：识别并复用形如"数 运算符 数"的简单算式
package com.example.calculator.domain

internal class RepeatOperation(val operator: Char, val operand: String)

internal object RepeatExtractor {

    private val SIMPLE_EXPRESSION = Regex("""^(-?\d+(?:\.\d+)?)([+\-×÷])(-?\d+(?:\.\d+)?)$""")

    /** 只有两数一运算符才重复运算，含括号、百分号等复杂算式直接放弃 */
    fun extract(expression: String): RepeatOperation? {
        val match = SIMPLE_EXPRESSION.matchEntire(expression) ?: return null
        return RepeatOperation(operator = match.groupValues[2][0], operand = match.groupValues[3])
    }

    fun apply(resultValue: String, operation: RepeatOperation): String =
        "$resultValue${operation.operator}${operation.operand}"
}
