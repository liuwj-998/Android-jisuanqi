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
            val termStart = index
            val right = parseTerm()
            // [新增] 加减号后的裸百分数按左侧数值的比例计算：100+10% 得 110
            val relative = tokens.subList(termStart, index).isBarePercent()
            value = when {
                operator.symbol == '+' && relative ->
                    value.add(value.multiply(right, CALC_MATH_CONTEXT), CALC_MATH_CONTEXT)

                operator.symbol == '+' -> value.add(right, CALC_MATH_CONTEXT)
                relative -> value.subtract(value.multiply(right, CALC_MATH_CONTEXT), CALC_MATH_CONTEXT)
                else -> value.subtract(right, CALC_MATH_CONTEXT)
            }
        }
        return value
    }

    /** 判断一段 token 是否只是"可选正负号 + 数字 + 百分号"，用于区分比例语义与普通数 */
    private fun List<Token>.isBarePercent(): Boolean {
        if (isEmpty()) return false
        val body = if (first() is Token.Operator && (first() as Token.Operator).symbol in SIGNS) {
            drop(1)
        } else {
            this
        }
        return body.size >= 2 &&
            body.first() is Token.Number &&
            body.drop(1).all { it is Token.Percent }
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
        val SIGNS = setOf('+', '-')
    }
}
