// [新增] 计算器 ViewModel：输入状态机 + 实时求值
package com.example.calculator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.calculator.domain.CalcResult
import com.example.calculator.domain.CalculatorEngine
import com.example.calculator.domain.InputProcessor
import com.example.calculator.domain.InputSnapshot
import com.example.calculator.domain.NumberFormatter
import com.example.calculator.model.CalcKey
import com.example.calculator.model.CalcState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = CalculatorEngine()

    private var snapshot = InputSnapshot()

    private val _state = MutableStateFlow(CalcState())
    val state: StateFlow<CalcState> = _state.asStateFlow()

    fun onKey(key: CalcKey) {
        if (key == CalcKey.Equals) {
            evaluate()
        } else {
            snapshot = InputProcessor.process(snapshot, key)
            refresh()
        }
    }

    /** 实时预览：输入过程中算式可能不完整，此时只清空预览，不向用户报错 */
    private fun refresh() {
        val expression = snapshot.expression
        if (expression.isEmpty()) {
            _state.update { it.copy(expression = "", preview = "0", error = null) }
            return
        }
        val target = expression.trimEnd { it in TRAILING_CHARS }
        when (val result = engine.evaluate(target)) {
            is CalcResult.Success -> _state.update {
                it.copy(expression = expression, preview = NumberFormatter.format(result.value), error = null)
            }

            is CalcResult.Failure -> _state.update {
                it.copy(expression = expression, preview = "", error = null)
            }
        }
    }

    private fun evaluate() {
        val expression = snapshot.expression
        if (expression.isEmpty()) return
        when (val result = engine.evaluate(expression)) {
            is CalcResult.Success -> {
                val formatted = NumberFormatter.format(result.value)
                snapshot = InputSnapshot(
                    expression = expression,
                    justEvaluated = true,
                    resultValue = result.value.toPlainString(),
                )
                _state.update { it.copy(expression = expression, preview = formatted, error = null) }
            }

            is CalcResult.Failure -> _state.update { it.copy(preview = "", error = result.message) }
        }
    }

    private companion object {
        val TRAILING_CHARS = setOf('+', '-', '×', '÷', '.')
    }
}
