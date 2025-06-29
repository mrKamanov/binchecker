/*
 * Главная активность приложения BIN Checker
 * 
 * Этот файл отвечает за:
 * - Точку входа в приложение
 * - Настройку темы и основной композиции
 * - Навигацию между двумя основными экранами (Проверка BIN и История)
 * - Управление состоянием выбранного экрана
 * 
 * Архитектура:
 * - Использует Jetpack Compose для UI
 * - Hilt для внедрения зависимостей
 * - Material3 для дизайна
 * - Bottom navigation для переключения между экранами
 * 
 * Экраны:
 * 1. BinInputScreen - главный экран для ввода и проверки BIN номеров
 * 2. HistoryScreen - экран истории ранее проверенных BIN номеров
 */

package com.binchecker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.binchecker.presentation.ui.screens.BinInputScreen
import com.binchecker.presentation.ui.screens.HistoryScreen
import com.binchecker.presentation.ui.theme.BINCheckerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BINCheckerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BINCheckerApp()
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Check : Screen("check", "Проверка", Icons.Default.Search)
    object History : Screen("history", "История", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BINCheckerApp() {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Check) }
    val context = LocalContext.current
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Screen.Check.icon, contentDescription = Screen.Check.title) },
                    label = { Text(Screen.Check.title) },
                    selected = selectedScreen == Screen.Check,
                    onClick = { selectedScreen = Screen.Check }
                )
                NavigationBarItem(
                    icon = { Icon(Screen.History.icon, contentDescription = Screen.History.title) },
                    label = { Text(Screen.History.title) },
                    selected = selectedScreen == Screen.History,
                    onClick = { selectedScreen = Screen.History }
                )
            }
        }
    ) { paddingValues ->
        when (selectedScreen) {
            Screen.Check -> {
                BinInputScreen(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            Screen.History -> {
                HistoryScreen(
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
} 