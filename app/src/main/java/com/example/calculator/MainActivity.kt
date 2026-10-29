// [新增] 应用入口：单 Activity + Compose
package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.calculator.ui.CalculatorRoute
import com.example.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                CalculatorApp()
            }
        }
    }
}

@Composable
private fun CalculatorApp() {
    // [新增] 计算器主界面（替换骨架阶段的占位内容）
    CalculatorRoute()
}
