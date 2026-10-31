// [新增] 计算器主界面：显示区 + 键盘 + 历史记录，宽屏时历史常驻左侧
package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.R
import com.example.calculator.model.CalcKey
import com.example.calculator.model.CalcState
import com.example.calculator.model.HistoryItem
import com.example.calculator.ui.components.DisplayPanel
import com.example.calculator.ui.components.HistoryContent
import com.example.calculator.ui.components.HistorySheet
import com.example.calculator.ui.components.Keypad
import com.example.calculator.ui.theme.CalculatorTheme
import com.example.calculator.viewmodel.CalculatorViewModel
import kotlinx.coroutines.launch

@Composable
fun CalculatorRoute(viewModel: CalculatorViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    CalculatorScreen(
        state = state,
        onKey = viewModel::onKey,
        onHistorySelect = viewModel::useHistory,
        onHistoryDelete = viewModel::deleteHistory,
        onHistoryClear = viewModel::clearHistory,
        onHistoryUndo = viewModel::undoClearHistory,
    )
}

@Composable
fun CalculatorScreen(
    state: CalcState,
    onKey: (CalcKey) -> Unit,
    onHistorySelect: (HistoryItem) -> Unit,
    onHistoryDelete: (HistoryItem) -> Unit,
    onHistoryClear: () -> Unit,
    onHistoryUndo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showHistory by rememberSaveable { mutableStateOf(false) }
    // [新增] 清空历史后的撤销提示
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clearedMessage = stringResource(R.string.history_cleared)
    val undoLabel = stringResource(R.string.undo)
    val clearWithUndo: () -> Unit = {
        if (state.history.isNotEmpty()) {
            onHistoryClear()
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = clearedMessage,
                    actionLabel = undoLabel,
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onHistoryUndo()
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        // [新增] 宽度达到阈值时切换为历史常驻双栏
        val wide = maxWidth >= WIDE_BREAKPOINT

        if (wide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    HistoryContent(
                        history = state.history,
                        onSelect = onHistorySelect,
                        onDelete = onHistoryDelete,
                        onClear = clearWithUndo,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        fillHeight = true,
                    )
                }
                CalculatorColumn(
                    state = state,
                    onKey = onKey,
                    onClearHistory = clearWithUndo,
                    showHistoryButton = false,
                    onOpenHistory = { showHistory = true },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        } else {
            CalculatorColumn(
                state = state,
                onKey = onKey,
                onClearHistory = clearWithUndo,
                showHistoryButton = true,
                onOpenHistory = { showHistory = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (!wide && showHistory) {
            HistorySheet(
                history = state.history,
                onSelect = { item ->
                    onHistorySelect(item)
                    showHistory = false
                },
                onDelete = onHistoryDelete,
                onClear = {
                    clearWithUndo()
                    showHistory = false
                },
                onDismiss = { showHistory = false },
            )
        }
    }
}

@Composable
private fun CalculatorColumn(
    state: CalcState,
    onKey: (CalcKey) -> Unit,
    onClearHistory: () -> Unit,
    showHistoryButton: Boolean,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DisplayPanel(
            expression = state.expression,
            result = state.error ?: state.preview,
            upper = state.upper,
            isError = state.error != null,
            showHistoryButton = showHistoryButton,
            onOpenHistory = onOpenHistory,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.34f),
        )
        Keypad(
            onKey = onKey,
            onClearHistory = onClearHistory,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.66f),
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
            onHistoryDelete = {},
            onHistoryClear = {},
            onHistoryUndo = {},
        )
    }
}

private val WIDE_BREAKPOINT = 600.dp
