/*
 * Главный экран приложения - BinInputScreen
 * 
 * Этот экран является центральной частью приложения и отвечает за:
 * - Ввод BIN номера пользователем
 * - Валидацию введенных данных
 * - Отображение результатов проверки BIN
 * - Интерактивные действия (открытие сайта банка, звонки, карты)
 * - Модальное окно настроек API
 * - Индикацию режима работы (Free/Premium)
 * 
 * Основные компоненты:
 * - InputSection - поле ввода и кнопка проверки
 * - ResultsCard - карточка с результатами проверки
 * - SettingsModal - модальное окно настроек API
 * - LoadingIndicator - индикатор загрузки
 * - ErrorCard - отображение ошибок
 * 
 * Особенности:
 * - Поддерживает как бесплатный, так и премиум режим API
 * - Показывает предупреждения о необходимости Premium API
 * - Анимированные переходы между состояниями
 * - Адаптивный дизайн с Material3
 * - Интеграция с системными приложениями (браузер, телефон, карты)
 */

package com.binchecker.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.binchecker.presentation.viewmodel.BinInputUiState
import com.binchecker.presentation.viewmodel.BinInputViewModel
import com.binchecker.presentation.viewmodel.SettingsViewModel
import android.content.Intent
import android.net.Uri

@Composable
fun BinInputScreen(
    viewModel: BinInputViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    
    
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val isPremiumMode = settingsState.apiKey.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BIN Checker",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            
            
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremiumMode) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPremiumMode) "⭐ Premium" else "🆓 Free",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isPremiumMode) Color(0xFF065F46) else Color(0xFF92400E)
                    )
                }
            }
            
            IconButton(onClick = { showSettings = true }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Настройки",
                    tint = Color(0xFF6B7280)
                )
            }
        }
        
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            
            item {
                InputSection(
                    binInput = uiState.binInput,
                    isLoading = uiState.isLoading,
                    validationError = uiState.validationError,
                    isButtonEnabled = uiState.isButtonEnabled,
                    onBinInputChanged = viewModel::onBinInputChanged,
                    onCheckBin = viewModel::checkBin
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            
            if (uiState.isLoading) {
                item {
                    LoadingIndicator()
                }
            }
            
            
            uiState.binInfo?.let { binInfo ->
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(500)
                        ),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        ResultsCard(
                            binInfo = binInfo,
                            isPremiumMode = isPremiumMode,
                            onOpenBankWebsite = { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            onCallBank = { phone ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            },
                            onShowOnMap = { lat, lng ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
            
            
            uiState.error?.let { error ->
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(300)
                        ),
                        exit = fadeOut() + slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(300)
                        )
                    ) {
                        ErrorCard(
                            error = error,
                            onDismiss = viewModel::clearError
                        )
                    }
                }
            }
        }
        
        
        if (showSettings) {
            SettingsModal(
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
private fun InputSection(
    binInput: String,
    isLoading: Boolean,
    validationError: String?,
    isButtonEnabled: Boolean,
    onBinInputChanged: (String) -> Unit,
    onCheckBin: () -> Unit
) {
    Column {
        Text(
            text = "Введите BIN номер:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = binInput,
            onValueChange = { onBinInputChanged(it) },
            placeholder = {
                Text("Например: 545301 (6-8 цифр)")
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (validationError != null) Color(0xFFDC2626) else Color(0xFF2563EB),
                unfocusedBorderColor = if (validationError != null) Color(0xFFDC2626) else Color(0xFFE5E7EB)
            )
        )
        
        validationError?.let { error ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                color = Color(0xFFDC2626),
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Бесплатный API: 5 запросов/час. Для полной информации настройте Premium API",
                    fontSize = 11.sp,
                    color = Color(0xFF1E40AF)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onCheckBin,
            enabled = isButtonEnabled && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                disabledContainerColor = Color(0xFF9CA3AF)
            )
        ) {
            Text(
                text = "Проверить",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF2563EB)
        )
    }
}

@Composable
private fun ResultsCard(
    binInfo: com.binchecker.domain.model.BinInfo,
    isPremiumMode: Boolean,
    onOpenBankWebsite: (String) -> Unit,
    onCallBank: (String) -> Unit,
    onShowOnMap: (Double, Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = Color(0xFF10B981),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Информация о карте",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            
            InfoRow("BIN:", binInfo.bin)
            
            
            InfoRow("Тип карты:", binInfo.scheme?.replaceFirstChar { it.uppercase() } ?: "Не указан")
            InfoRow("Бренд:", binInfo.brand ?: "Не указан")
            InfoRow("Тип:", binInfo.type?.replaceFirstChar { it.uppercase() } ?: "Не указан")
            InfoRow("Предоплаченная:", if (binInfo.prepaid == true) "Да" else if (binInfo.prepaid == false) "Нет" else "Не указано")
            InfoRow("Банк:", binInfo.bank?.name ?: "Не указан")
            InfoRow("Страна:", "${binInfo.country?.emoji ?: ""} ${binInfo.country?.name ?: "Не указана"}")
            InfoRow("Город банка:", binInfo.bank?.city ?: "Не указан")
            
            
            binInfo.bank?.url?.let { url ->
                InfoRow("Сайт банка:", url)
            }
            binInfo.bank?.phone?.let { phone ->
                InfoRow("Телефон банка:", phone)
            }
            
            
            binInfo.country?.let { country ->
                if (country.latitude != null && country.longitude != null) {
                    InfoRow("Координаты страны:", "${country.latitude}, ${country.longitude}")
                }
            }
            
            
            binInfo.bank?.let { bank ->
                if (bank.latitude != null && bank.longitude != null) {
                    InfoRow("Координаты банка:", "${bank.latitude}, ${bank.longitude}")
                }
            }
            
            
            if (binInfo.bank?.url == null && binInfo.bank?.phone == null && !isPremiumMode) {
                println("DEBUG: Showing Premium API warning - new request without Premium data")
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💡 Для получения сайта и телефона банка настройте Premium API в настройках",
                            fontSize = 12.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                println("DEBUG: NOT showing Premium API warning - Premium data present or Premium mode active")
                println("  - bank.url: ${binInfo.bank?.url}")
                println("  - bank.phone: ${binInfo.bank?.phone}")
                println("  - isPremiumMode: $isPremiumMode")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            
            binInfo.bank?.url?.let { url ->
                ActionButton(
                    text = "Открыть сайт банка",
                    icon = "🌐",
                    onClick = { onOpenBankWebsite(url) }
                )
            }
            
            binInfo.bank?.phone?.let { phone ->
                ActionButton(
                    text = "Позвонить в банк",
                    icon = "📞",
                    onClick = { onCallBank(phone) }
                )
            }
            
            
            binInfo.bank?.let { bank ->
                if (bank.latitude != null && bank.longitude != null) {
                    ActionButton(
                        text = "Показать банк на карте",
                        icon = "🏦",
                        onClick = { onShowOnMap(bank.latitude, bank.longitude) }
                    )
                }
            } ?: run {
                binInfo.country?.let { country ->
                    if (country.latitude != null && country.longitude != null) {
                        ActionButton(
                            text = "Показать страну на карте",
                            icon = "📍",
                            onClick = { onShowOnMap(country.latitude, country.longitude) }
                        )
                    }
                }
            }
            
            
            if (binInfo.bank?.url == null && binInfo.bank?.phone == null && !isPremiumMode) {
                println("DEBUG: Showing interactive actions warning - new request without Premium data")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔗 Для интерактивных действий с банком (сайт, звонок) настройте Premium API",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                println("DEBUG: NOT showing interactive actions warning - Premium data present or Premium mode active")
                println("  - bank.url: ${binInfo.bank?.url}")
                println("  - bank.phone: ${binInfo.bank?.phone}")
                println("  - isPremiumMode: $isPremiumMode")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
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
private fun SettingsModal(
    onDismiss: () -> Unit
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Настройки API",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                
                Text(
                    text = "API ключ (Premium)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = settingsState.apiKey,
                    onValueChange = settingsViewModel::onApiKeyChanged,
                    placeholder = {
                        Text("Введите API ключ от binlist.net")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "API ключ можно получить на сайте binlist.net в разделе Premium или написав на contact@iinlist.com",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💾 История запросов сохраняется при смене режимов",
                            fontSize = 11.sp,
                            color = Color(0xFF065F46),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Текущий статус: ${if (settingsState.apiKey.isNotEmpty()) "Premium" else "Бесплатная версия"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (settingsState.apiKey.isNotEmpty()) Color(0xFF059669) else Color(0xFF6B7280)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = if (settingsState.apiKey.isNotEmpty()) {
                                "✅ URL банка\n✅ Телефон банка\n✅ Город банка\n✅ Статус предоплаченной карты"
                            } else {
                                "❌ URL банка\n❌ Телефон банка\n❌ Город банка\n❌ Статус предоплаченной карты"
                            },
                            fontSize = 10.sp,
                            color = Color(0xFF374151)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Ограничения бесплатного API:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFC2410C)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "• 5 запросов в час\n• Базовая информация о карте\n• Название банка\n• Информация о стране",
                            fontSize = 10.sp,
                            color = Color(0xFF374151)
                        )
                    }
                }
                
                
                settingsState.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = Color(0xFFDC2626),
                        fontSize = 11.sp
                    )
                }
                
                settingsState.successMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = Color(0xFF059669),
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = settingsViewModel::clearApiKey
                ) {
                    Text("Очистить")
                }
                
                Button(
                    onClick = {
                        settingsViewModel::saveApiKey
                        onDismiss()
                    }
                ) {
                    Text("Сохранить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
} 