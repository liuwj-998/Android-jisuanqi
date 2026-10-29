// [新增] 输入规则单元测试：覆盖连续按、重复按、结果后继续输入等边界
package com.example.calculator.domain

import com.example.calculator.model.CalcKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputProcessorTest {

    private fun press(vararg keys: CalcKey): InputSnapshot {
        var snapshot = InputSnapshot()
        keys.forEach { snapshot = InputProcessor.process(snapshot, it) }
        return snapshot
    }

    @Test
    fun `数字连续拼接`() {
        val snapshot = press(CalcKey.Digit(1), CalcKey.Digit(2), CalcKey.Digit(3))
        assertEquals("123", snapshot.expression)
    }

    @Test
    fun `开头为零时被数字替换`() {
        assertEquals("5", press(CalcKey.Digit(0), CalcKey.Digit(5)).expression)
        assertEquals("0", press(CalcKey.Digit(0), CalcKey.Digit(0)).expression)
        assertEquals("0.50", press(CalcKey.Digit(0), CalcKey.Decimal, CalcKey.Digit(5), CalcKey.Digit(0)).expression)
    }

    @Test
    fun `小数点不能重复输入`() {
        val snapshot = press(CalcKey.Digit(1), CalcKey.Decimal, CalcKey.Digit(2), CalcKey.Decimal)
        assertEquals("1.2", snapshot.expression)
    }

    @Test
    fun `小数点前无数字时自动补零`() {
        assertEquals("0.", press(CalcKey.Decimal).expression)
        assertEquals("1+0.", press(CalcKey.Digit(1), CalcKey.Operator('+'), CalcKey.Decimal).expression)
    }

    @Test
    fun `连续运算符被替换`() {
        val snapshot = press(CalcKey.Digit(1), CalcKey.Operator('+'), CalcKey.Operator('-'))
        assertEquals("1-", snapshot.expression)
    }

    @Test
    fun `结果后按数字开启新算式`() {
        val evaluated = InputSnapshot("7", justEvaluated = true)
        val snapshot = InputProcessor.process(evaluated, CalcKey.Digit(5))
        assertEquals("5", snapshot.expression)
        assertFalse(snapshot.justEvaluated)
    }

    @Test
    fun `结果后按运算符继续计算`() {
        val evaluated = InputSnapshot("7", justEvaluated = true)
        val snapshot = InputProcessor.process(evaluated, CalcKey.Operator('+'))
        assertEquals("7+", snapshot.expression)
        assertFalse(snapshot.justEvaluated)
    }

    @Test
    fun `结果继续计算时不带入千分位`() {
        val evaluated = InputSnapshot("1,235", justEvaluated = true, resultValue = "1235")
        assertEquals("1235+", InputProcessor.process(evaluated, CalcKey.Operator('+')).expression)
        assertEquals("-1235", InputProcessor.process(evaluated, CalcKey.Sign).expression)
        assertEquals("1235%", InputProcessor.process(evaluated, CalcKey.Percent).expression)
    }

    @Test
    fun `结果后按退格清空`() {
        val evaluated = InputSnapshot("7", justEvaluated = true)
        val snapshot = InputProcessor.process(evaluated, CalcKey.Backspace)
        assertEquals("", snapshot.expression)
        assertFalse(snapshot.justEvaluated)
    }

    @Test
    fun `括号后按数字自动补乘号`() {
        val snapshot = InputProcessor.process(InputSnapshot("2×(3+4)"), CalcKey.Digit(5))
        assertEquals("2×(3+4)×5", snapshot.expression)
    }

    @Test
    fun `百分号只追加在数字之后`() {
        assertEquals("5%", press(CalcKey.Digit(5), CalcKey.Percent).expression)
        assertEquals("1+", press(CalcKey.Digit(1), CalcKey.Operator('+'), CalcKey.Percent).expression)
    }

    @Test
    fun `正负号切换当前数字`() {
        assertEquals("-5", press(CalcKey.Digit(5), CalcKey.Sign).expression)
        assertEquals("5", press(CalcKey.Digit(5), CalcKey.Sign, CalcKey.Sign).expression)
        assertEquals("3+-5", press(CalcKey.Digit(3), CalcKey.Operator('+'), CalcKey.Digit(5), CalcKey.Sign).expression)
        assertEquals("3+5", press(CalcKey.Digit(3), CalcKey.Operator('+'), CalcKey.Digit(5), CalcKey.Sign, CalcKey.Sign).expression)
    }

    @Test
    fun `清空重置状态`() {
        val snapshot = press(CalcKey.Digit(1), CalcKey.Clear)
        assertTrue(snapshot.expression.isEmpty())
        assertFalse(snapshot.justEvaluated)
    }

    @Test
    fun `智能括号按上下文插入`() {
        assertEquals("(", press(CalcKey.Paren).expression)
        assertEquals("1+(", press(CalcKey.Digit(1), CalcKey.Operator('+'), CalcKey.Paren).expression)
        assertEquals(
            "(1+2)",
            press(CalcKey.Paren, CalcKey.Digit(1), CalcKey.Operator('+'), CalcKey.Digit(2), CalcKey.Paren).expression,
        )
        assertEquals(
            "(1+2)×(",
            press(
                CalcKey.Paren, CalcKey.Digit(1), CalcKey.Operator('+'), CalcKey.Digit(2),
                CalcKey.Paren, CalcKey.Paren,
            ).expression,
        )
        assertEquals("5×(", press(CalcKey.Digit(5), CalcKey.Paren).expression)
    }

    @Test
    fun `结果后按括号继续计算`() {
        val evaluated = InputSnapshot("7", justEvaluated = true, resultValue = "7")
        assertEquals("7×(", InputProcessor.process(evaluated, CalcKey.Paren).expression)
    }

    @Test
    fun `表达式长度超过上限后忽略追加输入`() {
        var snapshot = InputSnapshot()
        repeat(80) { snapshot = InputProcessor.process(snapshot, CalcKey.Digit(1)) }
        assertEquals(80, snapshot.expression.length)

        val afterLimit = InputProcessor.process(snapshot, CalcKey.Digit(1))
        assertEquals(snapshot.expression, afterLimit.expression)

        val afterBackspace = InputProcessor.process(afterLimit, CalcKey.Backspace)
        assertEquals(79, afterBackspace.expression.length)
    }
}
