// [新增] 通用计算器按键：按用途区分配色
package com.example.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalcButton(
    spec: KeySpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .clickable(onClick = onClick),
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
