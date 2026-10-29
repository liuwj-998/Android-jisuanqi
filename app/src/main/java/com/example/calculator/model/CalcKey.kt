// [新增] 按键定义：所有输入都通过 CalcKey 传递，便于测试与扩展
package com.example.calculator.model

sealed interface CalcKey {
    data class Digit(val value: Int) : CalcKey
    data class Operator(val symbol: Char) : CalcKey
    data object Decimal : CalcKey
    data object Percent : CalcKey
    // [新增] 智能括号键：按上下文自动插入左/右括号
    data object Paren : CalcKey
    data object Sign : CalcKey
    data object Clear : CalcKey
    data object Backspace : CalcKey
    data object Equals : CalcKey
}
