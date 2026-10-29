// [新增] 数值格式化：去尾零、千分位、超大或超小数值转科学计数法
package com.example.calculator.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs

object NumberFormatter {

    private const val MAX_SIGNIFICANT_DIGITS = 16
    private val LARGE_THRESHOLD = BigDecimal("1000000000000")
    private val SMALL_THRESHOLD = BigDecimal("0.0000001")

    fun format(value: BigDecimal): String {
        val rounded = value
            .round(MathContext(MAX_SIGNIFICANT_DIGITS, RoundingMode.HALF_UP))
            .stripTrailingZeros()
        val magnitude = rounded.abs()
        val text = when {
            magnitude >= LARGE_THRESHOLD -> scientific(rounded)
            magnitude > BigDecimal.ZERO && magnitude < SMALL_THRESHOLD -> scientific(rounded)
            else -> rounded.toPlainString()
        }
        return groupThousands(text)
    }

    private fun scientific(value: BigDecimal): String {
        val rounded = value
            .round(MathContext(10, RoundingMode.HALF_UP))
            .stripTrailingZeros()
        val digits = rounded.unscaledValue().abs().toString()
        val exponent = digits.length - 1 - rounded.scale()
        val mantissa = if (digits.length == 1) digits else "${digits[0]}.${digits.substring(1)}"
        val sign = if (rounded.signum() < 0) "-" else ""
        val exponentSign = if (exponent >= 0) "+" else "-"
        return "$sign${mantissa}e$exponentSign${abs(exponent)}"
    }

    private fun groupThousands(text: String): String {
        if (text.contains('e') || text.contains('E')) return text.lowercase()
        val negative = text.startsWith("-")
        val body = if (negative) text.substring(1) else text
        val dotIndex = body.indexOf('.')
        val integerPart = if (dotIndex >= 0) body.substring(0, dotIndex) else body
        val fractionPart = if (dotIndex >= 0) body.substring(dotIndex) else ""
        val grouped = integerPart.reversed().chunked(3).joinToString(",").reversed()
        return (if (negative) "-" else "") + grouped + fractionPart
    }
}
