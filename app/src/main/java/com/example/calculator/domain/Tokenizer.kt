// [新增] 词法分析：把界面表达式拆成数字、运算符、括号与百分号
package com.example.calculator.domain

internal sealed interface Token {
    data class Number(val text: String) : Token
    data class Operator(val symbol: Char) : Token
    data object LeftParen : Token
    data object RightParen : Token
    data object Percent : Token
}

internal object Tokenizer {

    fun tokenize(expression: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0
        while (index < expression.length) {
            val char = expression[index]
            when {
                char.isWhitespace() -> index++
                char.isDigit() || char == '.' -> {
                    val start = index
                    var dotCount = 0
                    while (index < expression.length && (expression[index].isDigit() || expression[index] == '.')) {
                        if (expression[index] == '.') {
                            dotCount++
                            if (dotCount > 1) throw CalcException("数字格式不正确")
                        }
                        index++
                    }
                    tokens += Token.Number(expression.substring(start, index))
                }

                char in OPERATOR_SYMBOLS -> {
                    tokens += Token.Operator(normalizeOperator(char))
                    index++
                }

                char == '(' -> {
                    tokens += Token.LeftParen
                    index++
                }

                char == ')' -> {
                    tokens += Token.RightParen
                    index++
                }

                char == '%' -> {
                    tokens += Token.Percent
                    index++
                }

                else -> throw CalcException("算式包含无法识别的字符：$char")
            }
        }
        return tokens
    }

    private val OPERATOR_SYMBOLS = setOf('+', '-', '*', '/', '×', '÷')

    private fun normalizeOperator(symbol: Char): Char = when (symbol) {
        '×' -> '*'
        '÷' -> '/'
        else -> symbol
    }
}
