// [新增] 计算器 ViewModel：输入状态机 + 实时求值 + 历史记录
package com.example.calculator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.data.HistoryRepository
import com.example.calculator.domain.CalcResult
import com.example.calculator.domain.CalculatorEngine
import com.example.calculator.domain.InputProcessor
import com.example.calculator.domain.InputSnapshot
import com.example.calculator.domain.NumberFormatter
import com.example.calculator.model.CalcKey
import com.example.calculator.model.CalcState
import com.example.calculator.model.HistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = CalculatorEngine()
    private val historyRepository = HistoryRepository(application)

    private var snapshot = InputSnapshot()

    private val _state = MutableStateFlow(CalcState())
    val state: StateFlow<CalcState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.items.collect { items ->
                _state.update { it.copy(history = items) }
            }
        }
    }

    fun onKey(key: CalcKey) {
        if (key == CalcKey.Equals) {
            evaluate()
        } else {
            snapshot = InputProcessor.process(snapshot, key)
            refresh()
        }
    }

    fun useHistory(item: HistoryItem) {
        snapshot = InputSnapshot(
            expression = item.expression,
            justEvaluated = true,
            resultValue = item.value.ifEmpty { item.result.replace(",", "") },
        )
        _state.update {
            it.copy(expression = item.expression, preview = item.result, error = null)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) { historyRepository.clear() }
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
                recordHistory(expression, formatted, result.value.toPlainString())
            }

            is CalcResult.Failure -> _state.update { it.copy(preview = "", error = result.message) }
        }
    }

    private fun recordHistory(expression: String, formatted: String, rawValue: String) {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepository.record(
                HistoryItem(expression = expression, result = formatted, value = rawValue),
            )
        }
    }

    private companion object {
        val TRAILING_CHARS = setOf('+', '-', '×', '÷', '.')
    }
}
