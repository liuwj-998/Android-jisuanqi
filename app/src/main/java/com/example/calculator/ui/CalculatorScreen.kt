// [新增] 计算器主界面：显示区 + 键盘 + 历史记录面板
package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.model.CalcKey
import com.example.calculator.model.CalcState
import com.example.calculator.model.HistoryItem
import com.example.calculator.ui.components.DisplayPanel
import com.example.calculator.ui.components.HistorySheet
import com.example.calculator.ui.components.Keypad
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.calculator.viewmodel.CalculatorViewModel

@Composable
fun CalculatorRoute(viewModel: CalculatorViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    CalculatorScreen(
        state = state,
        onKey = viewModel::onKey,
        onHistorySelect = viewModel::useHistory,
        onHistoryClear = viewModel::clearHistory,
    )
}

@Composable
fun CalculatorScreen(
    state: CalcState,
    onKey: (CalcKey) -> Unit,
    onHistorySelect: (HistoryItem) -> Unit,
    onHistoryClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showHistory by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DisplayPanel(
                expression = state.expression,
                result = state.error ?: state.preview,
                upper = state.upper,
                isError = state.error != null,
                onOpenHistory = { showHistory = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.34f),
            )
            Keypad(
                onKey = onKey,
                onClearHistory = onHistoryClear,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.66f),
            )
        }
    }

    if (showHistory) {
        HistorySheet(
            history = state.history,
            onSelect = { item ->
                onHistorySelect(item)
                showHistory = false
            },
            onClear = onHistoryClear,
            onDismiss = { showHistory = false },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F7F8)
@Composable
private fun CalculatorScreenPreview() {
    CalculatorTheme(darkTheme = false) {
        CalculatorScreen(
            state = CalcState(expression = "1+2×3", preview = "7", upper = "柒元整"),
            onKey = {},
            onHistorySelect = {},
            onHistoryClear = {},
        )
    }
}
