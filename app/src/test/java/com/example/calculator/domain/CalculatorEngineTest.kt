// [新增] 引擎单元测试：覆盖算符优先级、精度、括号、百分比与错误分支
package com.example.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {

    private val engine = CalculatorEngine()

    private fun evaluate(expression: String): String = when (val result = engine.evaluate(expression)) {
        is CalcResult.Success -> NumberFormatter.format(result.value)
        is CalcResult.Failure -> "错误：${result.message}"
    }

    @Test
    fun `空表达式按零处理`() {
        assertEquals("0", evaluate(""))
        assertEquals("0", evaluate("   "))
    }

    @Test
    fun `算符优先级正确`() {
        assertEquals("7", evaluate("1+2×3"))
        assertEquals("-1", evaluate("1-2"))
    }

    @Test
    fun `小数运算没有浮点误差`() {
        assertEquals("0.3", evaluate("0.1+0.2"))
        assertEquals("3.3", evaluate("1.1+2.2"))
    }

    @Test
    fun `除法保留十六位有效数字`() {
        assertEquals("0.3333333333333333", evaluate("1÷3"))
        assertEquals("0.5", evaluate("1÷2"))
    }

    @Test
    fun `括号改变优先级`() {
        assertEquals("9", evaluate("(1+2)×3"))
        assertEquals("14", evaluate("2×(3+4)"))
    }

    @Test
    fun `一元负号可用`() {
        assertEquals("-1", evaluate("-3+2"))
        assertEquals("-6", evaluate("3×-2"))
        assertEquals("3", evaluate("--3"))
    }

    @Test
    fun `百分号按除以一百处理`() {
        assertEquals("0.05", evaluate("5%"))
        assertEquals("100.1", evaluate("100+10%"))
        assertEquals("0.25", evaluate("50%×50%"))
    }

    @Test
    fun `除以零返回可读错误`() {
        assertEquals("错误：不能除以零", evaluate("1÷0"))
        assertEquals("错误：不能除以零", evaluate("5÷(2-2)"))
    }

    @Test
    fun `括号不匹配返回可读错误`() {
        assertEquals("错误：括号不匹配", evaluate("2×(3+4"))
        assertEquals("错误：括号不匹配", evaluate("2×3+4)"))
    }

    @Test
    fun `非法数字返回可读错误`() {
        assertEquals("错误：数字格式不正确", evaluate("1.2.3+1"))
    }

    @Test
    fun `超大数值转科学计数法`() {
        assertEquals("1e+12", evaluate("999999999999+1"))
        assertEquals("1e-8", evaluate("0.00000001"))
    }

    @Test
    fun `千分位分组`() {
        assertEquals("1,234,567.89", evaluate("1234567.89"))
        assertEquals("-12,345", evaluate("-12345"))
    }
}
