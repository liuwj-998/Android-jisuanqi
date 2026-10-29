// [新增] 求值结果：成功携带精确数值，失败携带用户可读文案
package com.example.calculator.domain

import java.math.BigDecimal

sealed interface CalcResult {
    data class Success(val value: BigDecimal) : CalcResult
    data class Failure(val message: String) : CalcResult
}
