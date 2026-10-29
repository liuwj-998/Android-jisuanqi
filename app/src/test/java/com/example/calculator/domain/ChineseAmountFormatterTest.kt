// [新增] 人民币大写金额转换单元测试
package com.example.calculator.domain

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChineseAmountFormatterTest {

    @Test
    fun `零值与大额边界`() {
        assertEquals("零元整", ChineseAmountFormatter.format(BigDecimal.ZERO))
        assertNull(ChineseAmountFormatter.format(BigDecimal("1000000000000")))
        assertNull(ChineseAmountFormatter.format(BigDecimal("-1000000000000")))
    }

    @Test
    fun `整数金额`() {
        assertEquals("壹佰元整", ChineseAmountFormatter.format(BigDecimal("100")))
        assertEquals("壹仟贰佰叁拾肆元整", ChineseAmountFormatter.format(BigDecimal("1234")))
    }

    @Test
    fun `含零的整数金额`() {
        assertEquals("壹仟零伍拾元整", ChineseAmountFormatter.format(BigDecimal("1050")))
        assertEquals("壹万零伍元整", ChineseAmountFormatter.format(BigDecimal("10005")))
        assertEquals("壹佰万零伍元整", ChineseAmountFormatter.format(BigDecimal("1000005")))
        assertEquals("壹亿零伍元整", ChineseAmountFormatter.format(BigDecimal("100000005")))
    }

    @Test
    fun `角分金额`() {
        assertEquals("壹仟贰佰叁拾肆元伍角陆分", ChineseAmountFormatter.format(BigDecimal("1234.56")))
        assertEquals("壹拾元伍角整", ChineseAmountFormatter.format(BigDecimal("10.50")))
        assertEquals("零元零伍分", ChineseAmountFormatter.format(BigDecimal("0.05")))
        assertEquals("零元整", ChineseAmountFormatter.format(BigDecimal("0.004")))
    }

    @Test
    fun `负数金额`() {
        assertEquals("负壹拾贰元叁角整", ChineseAmountFormatter.format(BigDecimal("-12.30")))
    }
}
