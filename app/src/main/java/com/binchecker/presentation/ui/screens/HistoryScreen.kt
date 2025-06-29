/*
 * Экран истории запросов - HistoryScreen
 * 
 * Этот экран отображает историю всех ранее проверенных BIN номеров и предоставляет:
 * - Список всех сохраненных запросов с полной информацией
 * - Поиск по истории запросов
 * - Детальный просмотр каждого BIN номера в модальном окне
 * - Возможность очистки всей истории
 * - Интерактивные действия для каждого сохраненного BIN
 * 
 * Основные компоненты:
 * - Header - заголовок с кнопкой очистки истории
 * - SearchBar - поле поиска по истории
 * - HistoryList - список карточек с BIN номерами
 * - BinDetailDialog - модальное окно с детальной информацией
 * - EmptyState - состояние пустой истории
 * - LoadingIndicator - индикатор загрузки
 * 
 * Функциональность:
 * - Данные загружаются из локальной базы данных (Room)
 * - Поддерживает поиск по BIN номеру, названию банка, стране
 * - Сохраняет все данные из API, включая координаты и контакты
 * - Показывает время проверки каждого BIN
 * - Поддерживает те же интерактивные действия, что и главный экран
 * 
 * Особенности:
 * - История сохраняется между сессиями приложения
 * - Адаптивный дизайн с Material3
 * - Анимированные переходы и состояния
 * - Обработка различных состояний (пусто, загрузка, ошибка)
 */

package com.binchecker.presentation.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.binchecker.domain.model.BinInfo
import com.binchecker.presentation.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        
        Header(onClearHistory = viewModel::clearHistory)
        
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.bins.isEmpty() && uiState.searchQuery.isEmpty() -> {
                    EmptyState()
                }
                uiState.bins.isEmpty() && uiState.searchQuery.isNotEmpty() -> {
                    NoSearchResults()
                }
                else -> {
                    HistoryList(
                        bins = uiState.bins,
                        onBinClick = viewModel::onBinClick
                    )
                }
            }
            
            
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(300)
                ),
                exit = fadeOut() + slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300)
                )
            ) {
                uiState.error?.let { error ->
                    ErrorCard(
                        error = error,
                        onDismiss = viewModel::clearError
                    )
                }
            }
        }
        
        
        uiState.selectedBin?.let { bin ->
            BinDetailDialog(
                bin = bin,
                onDismiss = viewModel::onDetailDismiss
            )
        }
    }
}

@Composable
private fun Header(onClearHistory: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2563EB))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "История запросов",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ранее проверенные BIN",
                    color = Color(0xFFDBEAFE),
                    fontSize = 14.sp
                )
            }
            
            
            TextButton(
                onClick = onClearHistory,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "🗑️ Очистить",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF2563EB)
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📋",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "История пуста",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Проверьте первый BIN номер, чтобы увидеть историю",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text("🔍 Поиск по BIN, банку, стране...")
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2563EB),
            unfocusedBorderColor = Color(0xFFE5E7EB)
        )
    )
}

@Composable
private fun NoSearchResults() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ничего не найдено",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Попробуйте изменить запрос",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistoryList(
    bins: List<BinInfo>,
    onBinClick: (BinInfo) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = bins,
            key = { _, bin -> bin.bin }
        ) { index, bin ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it * (index + 1) },
                    animationSpec = tween(durationMillis = 300, delayMillis = index * 100)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 300, delayMillis = index * 100)
                )
            ) {
                HistoryCard(bin = bin, onBinClick = onBinClick)
            }
        }
    }
}

@Composable
private fun HistoryCard(bin: BinInfo, onBinClick: (BinInfo) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBinClick(bin) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = bin.bin,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = formatTimestamp(bin.timestamp),
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            
            InfoRow("Тип:", bin.scheme?.replaceFirstChar { it.uppercase() } ?: "Не указан")
            InfoRow("Банк:", bin.bank?.name ?: "Не указан")
            InfoRow("Страна:", "${bin.country?.emoji ?: ""} ${bin.country?.name ?: "Не указана"}")
            
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Нажмите для подробной информации",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFF6B7280),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color(0xFF1F2937),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = error,
                color = Color(0xFFDC2626),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("✕", color = Color(0xFFDC2626))
            }
        }
    }
}

@Composable
private fun BinDetailDialog(
    bin: BinInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "BIN: ${bin.bin}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        },
        text = {
            LazyColumn {
                item {
                    Column {
                        InfoRow("Тип карты:", bin.scheme?.replaceFirstChar { it.uppercase() } ?: "Не указан")
                        InfoRow("Бренд:", bin.brand ?: "Не указан")
                        InfoRow("Тип:", bin.type?.replaceFirstChar { it.uppercase() } ?: "Не указан")
                        InfoRow("Предоплаченная:", if (bin.prepaid == true) "Да" else if (bin.prepaid == false) "Нет" else "Не указано")
                        InfoRow("Банк:", bin.bank?.name ?: "Не указан")
                        InfoRow("Страна:", "${bin.country?.emoji ?: ""} ${bin.country?.name ?: "Не указана"}")
                        InfoRow("Город:", bin.bank?.city ?: "Не указан")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        
                        bin.bank?.url?.let { url ->
                            ActionButton(
                                text = "Открыть сайт банка",
                                icon = "🌐",
                                onClick = { 
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            )
                        }
                        
                        bin.bank?.phone?.let { phone ->
                            ActionButton(
                                text = "Позвонить в банк",
                                icon = "📞",
                                onClick = { 
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                }
                            )
                        }
                        
                        bin.country?.let { country ->
                            if (country.latitude != null && country.longitude != null) {
                                ActionButton(
                                    text = "Показать на карте",
                                    icon = "📍",
                                    onClick = { 
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${country.latitude},${country.longitude}"))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun ActionButton(
    text: String,
    icon: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF3F4F6)
        )
    ) {
        Text(
            text = "$icon $text",
            color = Color(0xFF374151),
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 60 * 1000 -> "только что"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} ч назад"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} дн назад"
        else -> {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            formatter.format(date)
        }
    }
} 