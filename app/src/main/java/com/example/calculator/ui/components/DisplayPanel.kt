// [新增] 显示区：历史入口 + 表达式行 + 结果行，超宽时横向滚动并自动滚到末尾
package com.example.calculator.ui.components

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DisplayPanel(
    expression: String,
    result: String,
    upper: String?,
    isError: Boolean,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val expressionScroll = rememberScrollState()
    val resultScroll = rememberScrollState()
    val historyLabel = stringResource(R.string.history_open)
    // [新增] 长按结果复制
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedMessage = stringResource(R.string.copy_result)

    LaunchedEffect(expression, expressionScroll.maxValue) {
        expressionScroll.animateScrollTo(expressionScroll.maxValue)
    }
    LaunchedEffect(result, resultScroll.maxValue) {
        resultScroll.animateScrollTo(resultScroll.maxValue)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.semantics { contentDescription = historyLabel },
                ) {
                    Text(
                        text = "≡",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = expression.ifEmpty { " " },
                modifier = Modifier.horizontalScroll(expressionScroll),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 18.sp,
                maxLines = 1,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = result.ifEmpty { " " },
                modifier = Modifier
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            if (result.isNotEmpty() && !isError) {
                                clipboard.setText(AnnotatedString(result))
                                Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
                    .horizontalScroll(resultScroll),
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            if (!upper.isNullOrEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = upper,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = if (upper.length > 18) 12.sp else 14.sp,
                    maxLines = 2,
                )
            }
        }
    }
}
