// [新增] 计算结果转人民币大写金额：保留两位小数（四舍五入），超出范围返回 null
package com.example.calculator.domain

import java.math.BigDecimal
import java.math.RoundingMode

object ChineseAmountFormatter {

    private val DIGITS = "零壹贰叁肆伍陆柒捌玖"
    private val UNITS = arrayOf("", "拾", "佰", "仟")
    private val GROUP_UNITS = arrayOf("", "万", "亿", "万亿")

    /** 与显示层科学计数法阈值保持一致，超出后大写金额没有意义 */
    private val MAX_AMOUNT = BigDecimal("1000000000000")

    fun format(value: BigDecimal): String? {
        if (value.abs() >= MAX_AMOUNT) return null

        val rounded = value.abs().setScale(2, RoundingMode.HALF_UP)
        if (rounded.compareTo(BigDecimal.ZERO) == 0) return "零元整"

        val integer = rounded.toBigInteger()
        val cents = rounded.subtract(BigDecimal(integer)).movePointRight(2).toInt()
        val sign = if (value.signum() < 0) "负" else ""
        val integerText = upperInteger(integer.toString())

        return buildString {
            append(sign)
            append(integerText.ifEmpty { "零" })
            append("元")
            append(upperFraction(cents))
        }
    }

    /** 整数部分按四位分组：如 100000005 组为 1 / 0000 / 0005 */
    private fun upperInteger(number: String): String {
        val groups = mutableListOf<String>()
        var digits = number
        while (digits.isNotEmpty()) {
            val take = minOf(4, digits.length)
            groups.add(0, digits.takeLast(take))
            digits = digits.dropLast(take)
        }

        val builder = StringBuilder()
        var pendingZero = false

        groups.forEachIndexed { index, group ->
            val groupUnit = GROUP_UNITS[groups.size - 1 - index]
            val padded = group.padStart(4, '0')
            if (padded.toInt() == 0) {
                pendingZero = builder.isNotEmpty()
                return@forEachIndexed
            }
            if (builder.isNotEmpty() && (pendingZero || padded[0] == '0')) {
                builder.append("零")
            }
            pendingZero = false

            var hasDigit = false
            padded.forEachIndexed { position, char ->
                val digit = char - '0'
                if (digit == 0) {
                    if (hasDigit && position < 3) pendingZero = true
                } else {
                    if (pendingZero && hasDigit) builder.append("零")
                    builder.append(DIGITS[digit]).append(UNITS[3 - position])
                    hasDigit = true
                    pendingZero = false
                }
            }
            builder.append(groupUnit)
        }

        return builder.toString()
    }

    private fun upperFraction(cents: Int): String {
        if (cents == 0) return "整"
        val jiao = cents / 10
        val fen = cents % 10
        return buildString {
            if (jiao > 0) append(DIGITS[jiao]).append("角")
            if (fen > 0) {
                if (jiao == 0) append("零")
                append(DIGITS[fen]).append("分")
            } else {
                append("整")
            }
        }
    }
}
