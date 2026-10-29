// [新增] 四列六行键盘：首行功能键，末行等号通栏
package com.example.calculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calculator.model.CalcKey

enum class KeyStyle { Number, Function, Operator, Equals, Danger }

data class KeySpec(val label: String, val key: CalcKey, val style: KeyStyle)

private val keypadRows: List<List<KeySpec>> = listOf(
    listOf(
        KeySpec("AC", CalcKey.Clear, KeyStyle.Danger),
        KeySpec("( )", CalcKey.Paren, KeyStyle.Function),
        KeySpec("%", CalcKey.Percent, KeyStyle.Function),
        KeySpec("⌫", CalcKey.Backspace, KeyStyle.Function),
    ),
    listOf(
        KeySpec("7", CalcKey.Digit(7), KeyStyle.Number),
        KeySpec("8", CalcKey.Digit(8), KeyStyle.Number),
        KeySpec("9", CalcKey.Digit(9), KeyStyle.Number),
        KeySpec("÷", CalcKey.Operator('÷'), KeyStyle.Operator),
    ),
    listOf(
        KeySpec("4", CalcKey.Digit(4), KeyStyle.Number),
        KeySpec("5", CalcKey.Digit(5), KeyStyle.Number),
        KeySpec("6", CalcKey.Digit(6), KeyStyle.Number),
        KeySpec("×", CalcKey.Operator('×'), KeyStyle.Operator),
    ),
    listOf(
        KeySpec("1", CalcKey.Digit(1), KeyStyle.Number),
        KeySpec("2", CalcKey.Digit(2), KeyStyle.Number),
        KeySpec("3", CalcKey.Digit(3), KeyStyle.Number),
        KeySpec("-", CalcKey.Operator('-'), KeyStyle.Operator),
    ),
    listOf(
        KeySpec("±", CalcKey.Sign, KeyStyle.Function),
        KeySpec("0", CalcKey.Digit(0), KeyStyle.Number),
        KeySpec(".", CalcKey.Decimal, KeyStyle.Number),
        KeySpec("+", CalcKey.Operator('+'), KeyStyle.Operator),
    ),
    listOf(
        KeySpec("=", CalcKey.Equals, KeyStyle.Equals),
    ),
)

@Composable
fun Keypad(
    onKey: (CalcKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        keypadRows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { spec ->
                    CalcButton(
                        spec = spec,
                        onClick = { onKey(spec.key) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}
