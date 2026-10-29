// [新增] 递归下降解析：支持四则运算、括号、一元正负号与百分号
package com.example.calculator.domain

import java.math.BigDecimal

internal class ExpressionParser(private val tokens: List<Token>) {

    private var index = 0

    fun parse(): BigDecimal {
        val value = parseExpression()
        if (index < tokens.size) {
            throw if (tokens[index] is Token.RightParen) {
                CalcException("括号不匹配")
            } else {
                CalcException("算式不完整")
            }
        }
        return value
    }

    private fun parseExpression(): BigDecimal {
        var value = parseTerm()
        while (true) {
            val operator = peekOperator() ?: break
            if (operator.symbol != '+' && operator.symbol != '-') break
            index++
            val right = parseTerm()
            value = if (operator.symbol == '+') {
                value.add(right, CALC_MATH_CONTEXT)
            } else {
                value.subtract(right, CALC_MATH_CONTEXT)
            }
        }
        return value
    }

    private fun parseTerm(): BigDecimal {
        var value = parseFactor()
        while (true) {
            val operator = peekOperator() ?: break
            if (operator.symbol != '*' && operator.symbol != '/') break
            index++
            val right = parseFactor()
            value = if (operator.symbol == '*') {
                value.multiply(right, CALC_MATH_CONTEXT)
            } else {
                if (right.compareTo(BigDecimal.ZERO) == 0) throw CalcException("不能除以零")
                value.divide(right, CALC_MATH_CONTEXT)
            }
        }
        return value
    }

    private fun parseFactor(): BigDecimal {
        var negative = false
        while (true) {
            val operator = peekOperator() ?: break
            if (operator.symbol != '+' && operator.symbol != '-') break
            if (operator.symbol == '-') negative = !negative
            index++
        }
        val value = parsePostfix()
        return if (negative) value.negate(CALC_MATH_CONTEXT) else value
    }

    private fun parsePostfix(): BigDecimal {
        var value = parsePrimary()
        while (peek() is Token.Percent) {
            index++
            value = value.divide(HUNDRED, CALC_MATH_CONTEXT)
        }
        return value
    }

    private fun parsePrimary(): BigDecimal {
        val token = peek() ?: throw CalcException("算式不完整")
        return when (token) {
            is Token.Number -> {
                index++
                token.text.toNumber()
            }

            is Token.LeftParen -> {
                index++
                val value = parseExpression()
                if (peek() !is Token.RightParen) throw CalcException("括号不匹配")
                index++
                value
            }

            else -> throw CalcException("算式不完整")
        }
    }

    private fun peek(): Token? = tokens.getOrNull(index)

    private fun peekOperator(): Token.Operator? = peek() as? Token.Operator

    private fun String.toNumber(): BigDecimal {
        var text = this
        if (text.startsWith(".")) text = "0$text"
        if (text.endsWith(".")) text = "${text}0"
        return BigDecimal(text)
    }

    private companion object {
        val HUNDRED: BigDecimal = BigDecimal("100")
    }
}
