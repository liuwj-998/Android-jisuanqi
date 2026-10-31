// [新增] 通用计算器按键：按用途区分配色
package com.example.calculator.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.model.CalcKey
import kotlinx.coroutines.delay

// [新增] 长按连删的起延时与重复间隔
private const val REPEAT_START_DELAY_MS = 500L
private const val REPEAT_INTERVAL_MS = 80L

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalcButton(
    spec: KeySpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onRepeat: (() -> Unit)? = null,
) {
    val container = when (spec.style) {
        KeyStyle.Number -> MaterialTheme.colorScheme.surfaceContainerHighest
        KeyStyle.Function -> MaterialTheme.colorScheme.surfaceContainerHigh
        KeyStyle.Operator -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        KeyStyle.Equals -> MaterialTheme.colorScheme.primary
        KeyStyle.Danger -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val content: Color = when (spec.style) {
        KeyStyle.Number -> MaterialTheme.colorScheme.onSurface
        KeyStyle.Function -> MaterialTheme.colorScheme.onSurfaceVariant
        KeyStyle.Operator -> MaterialTheme.colorScheme.primary
        KeyStyle.Equals -> MaterialTheme.colorScheme.onPrimary
        KeyStyle.Danger -> MaterialTheme.colorScheme.error
    }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    // [新增] 触感反馈与无障碍描述
    val haptic = LocalHapticFeedback.current
    val description = spec.key.contentDescription()
    val handleClick: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
    }
    val handleLongClick: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onLongClick?.invoke()
    }

    // [新增] 按住不放时重复触发（退格连续删除）
    if (onRepeat != null) {
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(REPEAT_START_DELAY_MS)
                while (true) {
                    onRepeat()
                    delay(REPEAT_INTERVAL_MS)
                }
            }
        }
    }

    val gestureModifier = if (onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = handleClick,
            onLongClick = handleLongClick,
        )
    } else {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = handleClick,
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .then(gestureModifier)
            .semantics { if (description != null) contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = spec.label,
            color = content,
            fontSize = if (spec.label.length > 1) 18.sp else 22.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

// [新增] 符号键的读屏文案：数字键仍由文本自身播报，无需重复描述
private fun CalcKey.contentDescription(): String? = when (this) {
    is CalcKey.Operator -> when (symbol) {
        '+' -> "加"
        '-' -> "减"
        '×' -> "乘"
        else -> "除"
    }

    CalcKey.Decimal -> "小数点"
    CalcKey.Percent -> "百分号"
    CalcKey.Sign -> "正负号"
    CalcKey.Clear -> "清空"
    CalcKey.Backspace -> "退格"
    CalcKey.Equals -> "等号"
    CalcKey.Paren -> "括号"
    is CalcKey.Digit -> null
}
