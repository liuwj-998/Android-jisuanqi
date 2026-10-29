// [新增] 求值入口：统一异常出口，向界面返回可读错误
package com.example.calculator.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

internal val CALC_MATH_CONTEXT: MathContext = MathContext(16, RoundingMode.HALF_UP)

internal class CalcException(message: String) : RuntimeException(message)

class CalculatorEngine {

    fun evaluate(expression: String): CalcResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return CalcResult.Success(BigDecimal.ZERO)
        return try {
            CalcResult.Success(ExpressionParser(Tokenizer.tokenize(trimmed)).parse())
        } catch (error: CalcException) {
            CalcResult.Failure(error.message.orEmpty())
        } catch (error: ArithmeticException) {
            CalcResult.Failure("数值超出可计算范围")
        }
    }
}
