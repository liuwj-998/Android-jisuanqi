// [新增] 计算器 ViewModel：输入状态机 + 实时求值 + 历史记录
package com.example.calculator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.calculator.data.HistoryRepository
import com.example.calculator.domain.CalcResult
import com.example.calculator.domain.CalculatorEngine
import com.example.calculator.domain.ChineseAmountFormatter
import com.example.calculator.domain.InputProcessor
import com.example.calculator.domain.InputSnapshot
import com.example.calculator.domain.NumberFormatter
import com.example.calculator.domain.RepeatExtractor
import com.example.calculator.domain.RepeatOperation
import com.example.calculator.model.CalcKey
import com.example.calculator.model.CalcState
import com.example.calculator.model.HistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val engine = CalculatorEngine()
    private val historyRepository = HistoryRepository(application)

    // [新增] 输入状态写入 SavedStateHandle，进程被杀后仍可恢复
    private var snapshot = InputSnapshot(
        expression = savedStateHandle.get<String>(KEY_EXPRESSION) ?: "",
        justEvaluated = savedStateHandle.get<Boolean>(KEY_JUST_EVALUATED) ?: false,
        resultValue = savedStateHandle.get<String>(KEY_RESULT_VALUE) ?: "",
    )

    // [新增] 连续按等号时重复的运算
    private var repeatOperation: RepeatOperation? = restoreRepeatOperation()

    // [新增] 清空历史前的备份与清空任务，用于撤销
    private var clearedHistory: List<HistoryItem>? = null
    private var clearJob: Job? = null

    private val _state = MutableStateFlow(CalcState())
    val state: StateFlow<CalcState> = _state.asStateFlow()

    init {
        restoreState()
        // [新增] 历史记录改为异步加载，避免主线程读盘
        viewModelScope.launch { historyRepository.load() }
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
            repeatOperation = null
            snapshot = InputProcessor.process(snapshot, key)
            persist()
            refresh()
        }
    }

    fun useHistory(item: HistoryItem) {
        snapshot = InputSnapshot(
            expression = item.expression,
            justEvaluated = true,
            resultValue = item.value.ifEmpty { item.result.replace(",", "") },
        )
        repeatOperation = RepeatExtractor.extract(item.expression)
        persist()
        _state.update {
            it.copy(
                expression = item.expression,
                preview = item.result,
                error = null,
                upper = item.value.ifEmpty { item.result.replace(",", "") }
                    .toBigDecimalOrNull()
                    ?.let(ChineseAmountFormatter::format),
            )
        }
    }

    /** [新增] 删除单条历史记录 */
    fun deleteHistory(item: HistoryItem) {
        viewModelScope.launch(Dispatchers.IO) { historyRepository.remove(item) }
    }

    fun clearHistory() {
        val backup = _state.value.history
        if (backup.isEmpty()) return
        clearedHistory = backup
        clearJob = viewModelScope.launch(Dispatchers.IO) { historyRepository.clear() }
    }

    /** [新增] 撤销清空：等清空任务结束后把备份写回 */
    fun undoClearHistory() {
        val backup = clearedHistory ?: return
        clearedHistory = null
        viewModelScope.launch(Dispatchers.IO) {
            clearJob?.join()
            historyRepository.restore(backup)
        }
    }

    /** 实时预览：输入过程中算式可能不完整，此时只清空预览，不向用户报错 */
    private fun refresh() {
        val expression = snapshot.expression
        if (expression.isEmpty()) {
            _state.update { it.copy(expression = "", preview = "0", error = null, upper = null) }
            return
        }
        val target = expression.trimEnd { it in TRAILING_CHARS }
        when (val result = engine.evaluate(target)) {
            is CalcResult.Success -> _state.update {
                it.copy(expression = expression, preview = NumberFormatter.format(result.value), error = null, upper = null)
            }

            is CalcResult.Failure -> _state.update {
                it.copy(expression = expression, preview = "", error = null, upper = null)
            }
        }
    }

    private fun evaluate() {
        val expression = if (snapshot.justEvaluated) {
            repeatOperation?.let {
                RepeatExtractor.apply(snapshot.resultValue.ifEmpty { snapshot.expression }, it)
            } ?: snapshot.expression
        } else {
            snapshot.expression
        }
        if (expression.isEmpty()) return
        when (val result = engine.evaluate(expression)) {
            is CalcResult.Success -> {
                val formatted = NumberFormatter.format(result.value)
                snapshot = InputSnapshot(
                    expression = expression,
                    justEvaluated = true,
                    resultValue = result.value.toPlainString(),
                )
                repeatOperation = RepeatExtractor.extract(expression)
                persist()
                _state.update {
                    it.copy(
                        expression = expression,
                        preview = formatted,
                        error = null,
                        upper = ChineseAmountFormatter.format(result.value),
                    )
                }
                recordHistory(expression, formatted, result.value.toPlainString())
            }

            is CalcResult.Failure -> _state.update { it.copy(preview = "", error = result.message, upper = null) }
        }
    }

    private fun recordHistory(expression: String, formatted: String, rawValue: String) {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepository.record(
                HistoryItem(expression = expression, result = formatted, value = rawValue),
            )
        }
    }

    /** 进程重建后回填界面：已完成的结果直接复用保存值，输入中的算式重新求值 */
    private fun restoreState() {
        if (snapshot.expression.isEmpty()) return
        if (snapshot.justEvaluated) {
            val value = snapshot.resultValue.toBigDecimalOrNull()
            _state.update {
                it.copy(
                    expression = snapshot.expression,
                    preview = value?.let(NumberFormatter::format) ?: "",
                    error = null,
                    upper = value?.let(ChineseAmountFormatter::format),
                )
            }
        } else {
            refresh()
        }
    }

    private fun persist() {
        savedStateHandle[KEY_EXPRESSION] = snapshot.expression
        savedStateHandle[KEY_JUST_EVALUATED] = snapshot.justEvaluated
        savedStateHandle[KEY_RESULT_VALUE] = snapshot.resultValue

        val repeat = repeatOperation
        if (repeat == null) {
            savedStateHandle.remove<String>(KEY_REPEAT_OPERATOR)
            savedStateHandle.remove<String>(KEY_REPEAT_OPERAND)
        } else {
            savedStateHandle[KEY_REPEAT_OPERATOR] = repeat.operator.toString()
            savedStateHandle[KEY_REPEAT_OPERAND] = repeat.operand
        }
    }

    private fun restoreRepeatOperation(): RepeatOperation? {
        val symbol = savedStateHandle.get<String>(KEY_REPEAT_OPERATOR) ?: return null
        val operand = savedStateHandle.get<String>(KEY_REPEAT_OPERAND) ?: return null
        return RepeatOperation(operator = symbol[0], operand = operand)
    }

    private companion object {
        val TRAILING_CHARS = setOf('+', '-', '×', '÷', '.')
        const val KEY_EXPRESSION = "expression"
        const val KEY_JUST_EVALUATED = "justEvaluated"
        const val KEY_RESULT_VALUE = "resultValue"
        const val KEY_REPEAT_OPERATOR = "repeatOperator"
        const val KEY_REPEAT_OPERAND = "repeatOperand"
    }
}
