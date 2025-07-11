# Регрессионные тесты

## Цель
Проверить, что исправленные проблемы больше не возникают и функциональность работает корректно.

---

## Тест-кейсы

### TC-35: Кнопка "Проверить" неактивна при пустом поле BIN
**Шаги:**
1. Открыть приложение
2. Убедиться, что поле BIN пустое
3. Проверить состояние кнопки "Проверить"

**Ожидаемый результат:**  
Кнопка "Проверить" неактивна (серого цвета)

**Фактический результат:** ПРОЙДЕН  
Кнопка неактивна до тех пор, пока не введено валидное значение

---

### TC-36: Кнопка "Проверить" неактивна при невалидном BIN
**Шаги:**
1. Ввести невалидный BIN (например, "123")
2. Проверить состояние кнопки "Проверить"

**Ожидаемый результат:**  
Кнопка "Проверить" неактивна

**Фактический результат:** ПРОЙДЕН  
Кнопка остается неактивной при невалидном BIN

---

### TC-37: Кнопка "Проверить" активна при валидном BIN
**Шаги:**
1. Ввести валидный BIN (например, "123456")
2. Проверить состояние кнопки "Проверить"

**Ожидаемый результат:**  
Кнопка "Проверить" активна (синего цвета)

**Фактический результат:** ПРОЙДЕН  
Кнопка становится активной при вводе валидного BIN

---

### TC-38: Корректное сообщение для несуществующего BIN "000000"
**Шаги:**
1. Ввести несуществующий BIN "000000"
2. Нажать "Проверить"
3. Дождаться ответа

**Ожидаемый результат:**  
Появляется понятное сообщение "BIN номер не найден в базе данных"

**Фактический результат:** ПРОЙДЕН  
Корректно пишет "BIN номер не найден в базе данных"

---

### TC-39: Корректное сообщение для других несуществующих BIN
**Шаги:**
1. Ввести другие несуществующие BIN (например, "999999", "111111")
2. Нажать "Проверить"
3. Дождаться ответа

**Ожидаемый результат:**  
Появляется понятное сообщение "BIN номер не найден в базе данных"

**Фактический результат:** ПРОЙДЕН  
Для всех несуществующих BIN показывается корректное сообщение

---

### TC-40: Кнопка остается неактивной при загрузке
**Шаги:**
1. Ввести валидный BIN
2. Нажать "Проверить"
3. Во время загрузки проверить состояние кнопки

**Ожидаемый результат:**  
Кнопка "Проверить" неактивна во время загрузки

**Фактический результат:** ПРОЙДЕН  
Кнопка становится неактивной во время загрузки данных

---

## Информация о тестировании

**Версия приложения:** 1.0
**Дата тестирования:** 2025-06-29
**Устройства:** OPPO E13f, Pixel 5 эмулятор
**Версия Android:** 10.0, 14.0

## Исправленные проблемы

### 1. Активность кнопки "Проверить"
- **Проблема:** Кнопка была активна даже при пустом поле
- **Решение:** Добавлена логика `isButtonEnabled` в UI
- **Результат:** Кнопка активна только при валидном BIN

### 2. Обработка ошибки HTTP 400
- **Проблема:** Несуществующие BIN возвращали "Ошибка сети: 400"
- **Решение:** Добавлена специальная обработка HTTP 400
- **Результат:** Понятное сообщение "BIN номер не найден в базе данных"

## Статус исправлений

ПРОЙДЕН: Все исправления протестированы и работают корректно
ПРОЙДЕН: UX улучшен - пользователь получает понятную обратную связь
ПРОЙДЕН: Критическая проблема TC-16 решена 