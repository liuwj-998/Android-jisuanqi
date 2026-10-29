// [新增] 输入规则状态机：纯逻辑实现"按下去会发生什么"，便于单元测试
package com.example.calculator.domain

import com.example.calculator.model.CalcKey

data class InputSnapshot(
    val expression: String = "",
    val justEvaluated: Boolean = false,
    val resultValue: String = "",
)

object InputProcessor {

    private val OPERATORS = setOf('+', '-', '×', '÷')

    // [新增] 表达式长度上限，避免超长输入拖慢求值与无限横向滚动
    private const val MAX_EXPRESSION_LENGTH = 80

    fun process(current: InputSnapshot, key: CalcKey): InputSnapshot = when (key) {
        CalcKey.Clear -> InputSnapshot()
        CalcKey.Backspace -> backspace(current)
        CalcKey.Decimal -> decimal(current)
        CalcKey.Percent -> percent(current)
        CalcKey.Sign -> sign(current)
        CalcKey.Paren -> paren(current)
        CalcKey.Equals -> current
        is CalcKey.Digit -> digit(current, key.value)
        is CalcKey.Operator -> operator(current, key.symbol)
    }

    private fun digit(current: InputSnapshot, digit: Int): InputSnapshot {
        val text = digit.toString()
        if (current.justEvaluated) return InputSnapshot(text)

        val expression = current.expression
        val last = expression.lastOrNull()
        val base = if (last == ')' || last == '%') expression + "×" else expression
        val segment = trailingNumber(base)
        val next = when {
            segment == "0" && text == "0" -> base
            segment == "0" -> base.dropLast(1) + text
            else -> base + text
        }
        return limited(current, next)
    }

    private fun decimal(current: InputSnapshot): InputSnapshot {
        if (current.justEvaluated) return InputSnapshot("0.")

        val expression = current.expression
        val last = expression.lastOrNull()
        val base = if (last == ')' || last == '%') expression + "×" else expression
        val segment = trailingNumber(base)
        if (segment.contains('.')) return InputSnapshot(base)
        return limited(current, if (segment.isEmpty()) base + "0." else base + ".")
    }

    private fun operator(current: InputSnapshot, symbol: Char): InputSnapshot {
        val expression = editableExpression(current)
        if (expression.isEmpty()) return current
        val last = expression.last()
        val next = when {
            last in OPERATORS -> expression.dropLast(1) + symbol
            last == '.' -> "${expression}0$symbol"
            last == '(' -> if (symbol == '-') expression + symbol else expression
            else -> expression + symbol
        }
        return limited(current, next)
    }

    private fun percent(current: InputSnapshot): InputSnapshot {
        if (current.justEvaluated) {
            return limited(current, editableExpression(current) + "%")
        }
        val expression = current.expression
        val last = expression.lastOrNull() ?: return current
        if (!(last.isDigit() || last == ')' || last == '%')) return current
        return limited(current, expression + "%")
    }

    private fun sign(current: InputSnapshot): InputSnapshot {
        val expression = editableExpression(current)
        if (expression.isEmpty()) return current
        val segment = trailingNumber(expression)
        if (segment.isEmpty() || segment == "0") return current

        val start = expression.length - segment.length
        val hasUnaryMinus = start > 0 &&
            expression[start - 1] == '-' &&
            (start - 1 == 0 || expression[start - 2] in OPERATORS || expression[start - 2] == '(')
        val next = if (hasUnaryMinus) {
            expression.removeRange(start - 1, start)
        } else {
            expression.substring(0, start) + "-" + segment
        }
        return limited(current, next)
    }

    private fun backspace(current: InputSnapshot): InputSnapshot {
        if (current.justEvaluated) return InputSnapshot()
        return InputSnapshot(current.expression.dropLast(1))
    }

    /**
     * [新增] 智能括号：算式末尾缺操作数时补左括号，有未闭合括号时补右括号，
     * 其余情况补"×("，这样不需要一个独立按键占位也能输入括号。
     */
    private fun paren(current: InputSnapshot): InputSnapshot {
        if (current.justEvaluated) return limited(current, editableExpression(current) + "×(")

        val expression = current.expression
        val last = expression.lastOrNull()
        val open = expression.count { it == '(' }
        val close = expression.count { it == ')' }
        val insert = when {
            last == null -> "("
            last in OPERATORS || last == '(' -> "("
            open > close -> ")"
            else -> "×("
        }
        return limited(current, expression + insert)
    }

    /** 追加类输入的统一起点校验：超过长度上限时保持原状态 */
    private fun limited(current: InputSnapshot, next: String): InputSnapshot =
        if (next.length > MAX_EXPRESSION_LENGTH) current else InputSnapshot(next)

    /** 结果行带千分位格式化，继续计算时必须回退到未格式化的原始数值 */
    private fun editableExpression(current: InputSnapshot): String =
        if (current.justEvaluated) current.resultValue.ifEmpty { current.expression } else current.expression

    private fun trailingNumber(expression: String): String {
        var index = expression.length
        while (index > 0 && (expression[index - 1].isDigit() || expression[index - 1] == '.')) {
            index--
        }
        return expression.substring(index)
    }
}
