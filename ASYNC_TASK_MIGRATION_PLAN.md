# 🚀 План Миграции AsyncTask → AsyncExecutor

**Дата начала**: 24 октября 2025 г.
**Цель**: Постепенная замена AsyncTask на современный AsyncExecutor
**Статус**: В ПРОЦЕССЕ

## 📊 Текущий статус миграции

### ✅ **Завершено (2 файла)**
- `AlarmReceiver.java` - получение реального URL станции ✅ **ЗАВЕРШЕНО**
- `StationActions.java` - действия со станциями (3 AsyncTask) ✅ **ЗАВЕРШЕНО**

### 🔄 **В процессе (0 файлов)**
- *(ожидание выбора следующего файла)*

### 📋 **Очередь на миграцию (12 файлов)**
1. `FragmentSettings.java` - настройки прокси
3. `PlayStationTask.java` - воспроизведение станции
4. `GetRealLinkAndPlayTask.java` - получение и воспроизведение
5. `MPDPlayTask.java` - MPD команды
6. `MPDChangeVolumeTask.java` - изменение громкости MPD
7. `MPDPauseTask.java` - пауза MPD
8. `MPDResumeTask.java` - возобновление MPD
9. `MPDStopTask.java` - остановка MPD
10. `MPDClient.java` - клиент MPD
11. `MPDAsyncTask.java` - базовый класс MPD
12. `ModernAsyncTask.java` - уже современная версия

## 🎯 **Стратегия миграции**

### **Приоритеты**
1. **IO-bound операции** → `AsyncExecutor.submitIOTask()`
2. **CPU-bound операции** → `AsyncExecutor.submitComputationTask()`
3. **Простые AsyncTask** → миграция первыми

### **Шаги миграции для каждого файла**
1. **Анализ**: Определить тип операции (IO/CPU)
2. **Рефакторинг**: Заменить AsyncTask на AsyncExecutor
3. **Тестирование**: Проверить функциональность
4. **Документация**: Обновить план

### **API AsyncExecutor**
```java
// IO операции (сеть, диск)
AsyncExecutor.submitIOTask(() -> {
    // background работа
    return result;
}).thenAccept(result -> {
    // UI обновление
}).exceptionally(error -> {
    // обработка ошибки
    return null;
});

// С коллбэками
AsyncExecutor.executeIOTask(
    () -> computeResult(),  // Callable<T>
    result -> updateUI(result),  // OnResultCallback<T>
    error -> handleError(error)  // OnErrorCallback
);
```

## 🔧 **Текущий прогресс: AlarmReceiver.java**

### **Что делает AsyncTask:**
- Получает реальный URL станции через `Utils.getRealStationLink()`
- Повторяет попытки до 20 раз с задержкой 500мс
- В `onPostExecute`: запускает воспроизведение или системный будильник

### **Миграция:**
```java
// Было
new AsyncTask<Void, Void, String>() {
    @Override
    protected String doInBackground(Void... params) {
        // IO операция получения URL
        return Utils.getRealStationLink(httpClient, context, stationId);
    }
    @Override
    protected void onPostExecute(String result) {
        // UI обновление
    }
}.execute();

// Станет
AsyncExecutor.submitIOTask(() -> {
    // IO операция получения URL
    return Utils.getRealStationLink(httpClient, context, stationId);
}).thenAccept(result -> {
    // UI обновление на главном потоке
    AsyncExecutor.runOnMainThread(() -> {
        // код из onPostExecute
    });
}).exceptionally(error -> {
    // обработка ошибки
    return null;
});
```

### **Преимущества новой реализации:**
- ✅ Современный API (CompletableFuture)
- ✅ Лучшее управление потоками
- ✅ Автоматическое выполнение UI кода на главном потоке
- ✅ Лучшая обработка ошибок
- ✅ Более читаемый код

---

## 📈 **Следующие шаги**
1. Завершить миграцию AlarmReceiver.java
2. Протестировать функциональность будильника
3. Перейти к следующему файлу: StationActions.java
