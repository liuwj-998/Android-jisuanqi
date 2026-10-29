// [新增] 连续等号重复运算的提取规则单元测试
package com.example.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RepeatExtractorTest {

    @Test
    fun `提取两数一运算符`() {
        val operation = RepeatExtractor.extract("5+3")
        assertEquals('+', operation?.operator)
        assertEquals("3", operation?.operand)
    }

    @Test
    fun `支持负数与小数`() {
        val operation = RepeatExtractor.extract("8×-2.5")
        assertEquals('×', operation?.operator)
        assertEquals("-2.5", operation?.operand)
    }

    @Test
    fun `复杂算式不重复`() {
        assertNull(RepeatExtractor.extract("2×(3+4)"))
        assertNull(RepeatExtractor.extract("50%×50%"))
        assertNull(RepeatExtractor.extract("8+"))
        assertNull(RepeatExtractor.extract(""))
    }

    @Test
    fun `按结果拼接下一次算式`() {
        val operation = RepeatExtractor.extract("5+3")!!
        assertEquals("8+3", RepeatExtractor.apply("8", operation))
    }
}
