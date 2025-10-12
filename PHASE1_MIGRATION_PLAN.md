# 🎯 ПЛАН МИГРАЦИИ - ФАЗА 1 (КРИТИЧНЫЕ ИСПРАВЛЕНИЯ)

**Цель**: Устранить критические deprecated API для совместимости с Android API 35+  
**Сроки**: 1-2 недели  
**Приоритет**: 🔴 ВЫСОКИЙ

---

## 📋 ЗАДАЧА 1.1: ЗАМЕНА AsyncTask НА CompletableFuture

### ⚠️ ОЦЕНКА РИСКОВ

| Файл | Риск | Причина | Mitigation |
|------|------|---------|------------|
| **PlayStationTask.java** | 🔴 ВЫСОКИЙ | Основной механизм воспроизведения. Сбой = не работает play | Детальное тестирование всех сценариев playback |
| **GetRealLinkAndPlayTask.java** | 🔴 ВЫСОКИЙ | URL resolution для потоков. Сбой = не загружаются станции | Тестирование с разными типами URL (redirect, M3U, прямые) |
| **StationActions.java** | 🟠 СРЕДНИЙ | Действия над станциями (добавить, удалить). Сбой = UX проблемы | Unit тесты для всех действий |
| **ActivityMain.java** | 🟠 СРЕДНИЙ | UI обновления. Сбой = зависания интерфейса | UI тесты |
| **FragmentBase.java** | 🟠 СРЕДНИЙ | Базовый класс фрагментов. Сбой = все фрагменты | Тестирование всех производных фрагментов |
| **FragmentHistory.java** | 🟡 НИЗКИЙ | История воспроизведения | Тестирование загрузки истории |
| **FragmentServerInfo.java** | 🟡 НИЗКИЙ | Информация о сервере | Простая проверка |
| **NoNameRadioBrowser.java** | 🟠 СРЕДНИЙ | Android Auto integration | Тестирование MediaBrowser |
| **AlarmReceiver.java** | 🟠 СРЕДНИЙ | Будильники. Сбой = не срабатывают | Тестирование с реальными алармами |
| **ProxySettingsDialog.java** | 🟡 НИЗКИЙ | Диалог настроек прокси | UI тест диалога |
| **MPD Tasks (5 файлов)** | 🟢 ОЧЕНЬ НИЗКИЙ | MPD клиент редко используется | Минимальное тестирование |

### 📝 ПЛАН ДЕЙСТВИЙ

#### **Шаг 1.1.1: PlayStationTask.java** (2-3 часа)
```java
// БЫЛО:
public class PlayStationTask extends AsyncTask<Void, Void, String> {
    @Override
    protected String doInBackground(Void... voids) { ... }
    @Override
    protected void onPostExecute(String result) { ... }
}

// СТАНЕТ:
public class PlayStationTask {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public CompletableFuture<String> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            // Логика из doInBackground
            return resolvedUrl;
        }, executor).thenApplyAsync(result -> {
            // Логика из onPostExecute на main thread
            return result;
        }, new Handler(Looper.getMainLooper())::post);
    }
}
```

**Что может сломаться:**
- ❌ Не воспроизводятся станции
- ❌ Утечки памяти из-за неправильного управления ExecutorService
- ❌ Ошибки потоков при обновлении UI

**Как проверить:**
```
✅ Запустить 5-10 разных станций подряд
✅ Переключаться между станциями быстро (stress test)
✅ Проверить алармы
✅ Проверить MPD playback
✅ Проверить Cast (Chromecast)
✅ Логи: нет memory leaks, нет thread exceptions
```

---

#### **Шаг 1.1.2: GetRealLinkAndPlayTask.java** (1-2 часа)
```java
// Аналогичная миграция для URL resolution
```

**Что может сломаться:**
- ❌ M3U/PLS плейлисты не разрешаются
- ❌ Redirect URL не обрабатываются
- ❌ Таймауты при медленном интернете

**Как проверить:**
```
✅ Протестировать станции с прямыми URL
✅ Протестировать станции с M3U/PLS
✅ Протестировать станции с 301/302 редиректами
✅ Протестировать при медленном интернете (4G, throttled)
```

---

#### **Шаг 1.1.3: MPD Tasks (5 файлов)** (1-2 часа)
- MPDPlayTask.java
- MPDStopTask.java
- MPDResumeTask.java
- MPDPauseTask.java
- MPDChangeVolumeTask.java

**Риск:** 🟢 ОЧЕНЬ НИЗКИЙ (MPD редко используется)

**Как проверить:**
```
✅ Если есть MPD сервер - базовая проверка подключения
✅ Если нет - skip (low priority feature)
```

---

#### **Шаг 1.1.4: StationActions.java** (1 час)
**Риск:** 🟠 СРЕДНИЙ (операции с избранными станциями)

**Что может сломаться:**
- ❌ Не добавляются/удаляются избранные
- ❌ Не экспортируются/импортируются станции

**Как проверить:**
```
✅ Добавить станцию в избранное
✅ Удалить станцию из избранного
✅ Экспорт/импорт станций
```

---

#### **Шаг 1.1.5: UI AsyncTasks (5 файлов)** (2-3 часа)
- ActivityMain.java
- FragmentBase.java
- FragmentHistory.java
- FragmentServerInfo.java
- ProxySettingsDialog.java
- AlarmReceiver.java
- NoNameRadioBrowser.java

**Риск:** 🟠 СРЕДНИЙ

**Что может сломаться:**
- ❌ UI не обновляется
- ❌ Загрузка истории зависает
- ❌ Алармы не срабатывают
- ❌ Android Auto не подключается

**Как проверить:**
```
✅ Открыть все экраны приложения
✅ Проверить загрузку истории
✅ Установить и запустить алларм
✅ Попробовать Android Auto (если доступен)
✅ Изменить настройки прокси
```

---

## 📋 ЗАДАЧА 1.2: МИГРАЦИЯ Observable НА LiveData

### ⚠️ ОЦЕНКА РИСКОВ

| Файл | Риск | Причина | Mitigation |
|------|------|---------|------------|
| **RadioAlarmManager.java** | 🟡 НИЗКИЙ | Управление алармами | Тестирование алармов |
| **StationSaveManager.java** | 🟠 СРЕДНИЙ | Legacy класс, много зависимостей | Делегирование на StationManager |
| **StationManager.java** | 🟠 СРЕДНИЙ | Ядро управления станциями | Детальное тестирование |
| **FragmentPlayerFull.java** | 🔴 ВЫСОКИЙ | Главный плеер UI | Полное тестирование UI |
| **FragmentStarred.java** | 🟡 НИЗКИЙ | Список избранных | UI тест |
| **FragmentAlarm.java** | 🟡 НИЗКИЙ | UI алармов | UI тест |
| **TvChannelManager.kt** | 🟢 ОЧЕНЬ НИЗКИЙ | TV интеграция (редко используется) | Минимальное тестирование |

### 📝 ПЛАН ДЕЙСТВИЙ

#### **Шаг 1.2.1: RadioAlarmManager.java** (1-2 часа)
```java
// БЫЛО:
private final Observable savedAlarmsObservable = new AlarmsObservable();

public Observable getSavedAlarmsObservable() {
    return savedAlarmsObservable;
}

// СТАНЕТ:
private final MutableLiveData<List<DataRadioStationAlarm>> alarmsLiveData = 
    new MutableLiveData<>();

public LiveData<List<DataRadioStationAlarm>> getAlarmsLiveData() {
    return alarmsLiveData;
}

// В методах add/remove/update:
alarmsLiveData.postValue(list);
```

**Что может сломаться:**
- ❌ UI не обновляется при изменении алармов
- ❌ Алармы не сохраняются

**Как проверить:**
```
✅ Создать новый аларм - UI обновился
✅ Удалить аларм - UI обновился
✅ Изменить аларм - UI обновился
✅ Перезапустить приложение - алармы на месте
```

---

#### **Шаг 1.2.2: StationSaveManager.java** (2-3 часа)
**Риск:** 🟠 СРЕДНИЙ

**Стратегия:** Не мигрировать напрямую, а делегировать на `StationManager`

```java
// Уже есть TODO в коде:
// TODO: Migrate all usage to StationManager directly.

// ПЛАН:
// 1. Найти все места использования StationSaveManager
// 2. Постепенно заменить на StationManager
// 3. Пометить StationSaveManager как @Deprecated
// 4. Удалить в Фазе 3
```

**Что может сломаться:**
- ❌ Не сохраняются станции
- ❌ Не загружаются сохраненные станции

**Как проверить:**
```
✅ Добавить станцию в избранное
✅ Экспорт станций
✅ Импорт станций
✅ Проверить Room database
```

---

#### **Шаг 1.2.3: FragmentPlayerFull.java** (3-4 часа)
**Риск:** 🔴 ВЫСОКИЙ (главный плеер UI)

```java
// БЫЛО:
Observable.addObserver(new Observer() {
    @Override
    public void update(Observable o, Object arg) {
        // Update UI
    }
});

// СТАНЕТ:
viewModel.getPlayerStateLiveData().observe(getViewLifecycleOwner(), state -> {
    // Update UI
});
```

**Что может сломаться:**
- ❌ Плеер UI не обновляется
- ❌ Metadata не отображается
- ❌ Обложки не загружаются
- ❌ История треков не обновляется

**Как проверить:**
```
✅ Запустить станцию - UI обновляется
✅ Остановить - UI обновляется
✅ Metadata отображается
✅ Обложки загружаются
✅ История треков работает
✅ Swipe between recordings/history tabs
```

---

#### **Шаг 1.2.4: Остальные фрагменты** (2-3 часа)
- FragmentStarred.java
- FragmentAlarm.java
- TvChannelManager.kt

**Риск:** 🟡 НИЗКИЙ

**Как проверить:**
```
✅ Открыть экран избранных
✅ Открыть экран алармов
✅ Проверить TV launcher integration (если доступно)
```

---

## 📋 ЗАДАЧА 1.3: ОБНОВЛЕНИЕ LIFECYCLE МЕТОДОВ

### ⚠️ ОЦЕНКА РИСКОВ

| Файл | Риск | Причина | Mitigation |
|------|------|---------|------------|
| **FragmentPlayerSmall.java** | 🟡 НИЗКИЙ | Мини-плеер | UI тест |
| **FragmentPlayerFull.java** | 🟠 СРЕДНИЙ | Полный плеер (уже будет трогаться в 1.2.3) | Совместить с 1.2.3 |

### 📝 ПЛАН ДЕЙСТВИЙ

#### **Шаг 1.3.1-1.3.2: Оба фрагмента** (1-2 часа)
```java
// БЫЛО:
@Override
public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    // Инициализация
}

setRetainInstance(true);

// СТАНЕТ:
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Инициализация
}

// Вместо setRetainInstance - использовать ViewModel
PlayerViewModel viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
```

**Что может сломаться:**
- ❌ Состояние теряется при повороте экрана
- ❌ Плеер сбрасывается при configuration changes

**Как проверить:**
```
✅ Запустить станцию
✅ Повернуть экран
✅ Станция продолжает играть
✅ UI в правильном состоянии
✅ Метаданные на месте
```

---

## 🧪 ОБЩИЙ ПЛАН ТЕСТИРОВАНИЯ

### 🔍 **Критические сценарии (Must Pass)**

#### 1. Воспроизведение
- [ ] Запустить станцию через поиск
- [ ] Запустить станцию из избранного
- [ ] Запустить станцию из истории
- [ ] Переключиться на другую станцию
- [ ] Пауза/Resume
- [ ] Stop

#### 2. UI Обновления
- [ ] Metadata обновляется в реальном времени
- [ ] Обложки загружаются
- [ ] Прогресс показывается при загрузке
- [ ] Поворот экрана не сбивает состояние

#### 3. Фоновая работа
- [ ] Воспроизведение в фоне
- [ ] Уведомление отображается
- [ ] Управление через уведомление
- [ ] Lock screen controls

#### 4. Сетевые сценарии
- [ ] Прямые URL
- [ ] M3U/PLS плейлисты
- [ ] Redirect URLs
- [ ] Медленный интернет
- [ ] Потеря соединения / восстановление

#### 5. База данных
- [ ] Добавить в избранное
- [ ] Удалить из избранного
- [ ] История сохраняется
- [ ] Алармы сохраняются

#### 6. Особые функции
- [ ] Алармы срабатывают
- [ ] Запись работает (если используется)
- [ ] Android Auto (если доступен)
- [ ] Chromecast (если доступен)

---

## 🚨 ЧТО ТОЧНО СЛОМАЕТСЯ И КАК ГОТОВИТЬСЯ

### 🔥 **Высокая вероятность поломки**

| Что | Вероятность | Как обнаружить | Как исправить |
|-----|-------------|----------------|---------------|
| **Memory Leaks** | 80% | LeakCanary, Android Profiler | Правильно закрывать ExecutorService |
| **Thread Exceptions** | 70% | Logcat | Handler(Looper.getMainLooper()) для UI |
| **UI не обновляется** | 60% | Визуально | Проверить observe на правильном Lifecycle |
| **Context утечки** | 50% | LeakCanary | WeakReference для Context |
| **Станции не играют** | 40% | Тестирование | Детальная отладка PlayStationTask |

### 🛡️ **Стратегия минимизации рисков**

#### **1. Создать backup ветку**
```bash
git checkout -b phase1-migration
git checkout master
git checkout -b backup-before-phase1
```

#### **2. Поэтапный подход**
```
Day 1-2: Миграция PlayStationTask + тестирование (критично)
Day 3: Миграция GetRealLinkAndPlayTask + тестирование
Day 4-5: MPD tasks + StationActions + тестирование
Day 6-7: UI AsyncTasks + тестирование
Day 8-9: Observable → LiveData + тестирование
Day 10-11: Lifecycle методы + финальное тестирование
Day 12-14: Резерв на исправление багов
```

#### **3. Автоматические тесты**
```bash
# После каждого изменения:
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
./gradlew lint                    # Linter
```

#### **4. Ручное тестирование**
```
✅ Чек-лист из 30+ сценариев (выше)
✅ Тестирование на реальном устройстве
✅ Тестирование на эмуляторе
✅ Тестирование при плохом интернете
✅ Длительное тестирование (30+ минут воспроизведения)
```

#### **5. Мониторинг**
```
✅ Logcat - нет exception'ов
✅ Android Profiler - нет memory leaks
✅ Battery usage - не увеличился
✅ CPU usage - не увеличился
```

---

## 📊 ОЦЕНКА ВРЕМЕНИ

| Задача | Оценка | Риск | Приоритет |
|--------|--------|------|-----------|
| 1.1.1 PlayStationTask | 2-3 часа | 🔴 | P0 |
| 1.1.2 GetRealLinkAndPlayTask | 1-2 часа | 🔴 | P0 |
| 1.1.3 MPD Tasks | 1-2 часа | 🟢 | P3 |
| 1.1.4 StationActions | 1 час | 🟠 | P1 |
| 1.1.5 UI AsyncTasks | 2-3 часа | 🟠 | P1 |
| 1.2.1 RadioAlarmManager | 1-2 часа | 🟡 | P2 |
| 1.2.2 StationSaveManager | 2-3 часа | 🟠 | P1 |
| 1.2.3 FragmentPlayerFull | 3-4 часа | 🔴 | P0 |
| 1.2.4 Остальные фрагменты | 2-3 часа | 🟡 | P2 |
| 1.3 Lifecycle методы | 1-2 часа | 🟡 | P2 |
| **Разработка** | **17-25 часов** | | |
| **Тестирование** | **15-20 часов** | | |
| **ИТОГО** | **32-45 часов** (4-6 дней) | | |

---

## ✅ КРИТЕРИИ ЗАВЕРШЕНИЯ ФАЗЫ 1

- [ ] Все AsyncTask заменены на CompletableFuture
- [ ] Все Observable заменены на LiveData
- [ ] Все deprecated Lifecycle методы обновлены
- [ ] ✅ All unit tests pass
- [ ] ✅ All instrumented tests pass
- [ ] ✅ No lint errors
- [ ] ✅ No memory leaks (LeakCanary)
- [ ] ✅ 30+ manual test scenarios pass
- [ ] ✅ App stable for 1+ hour of playback
- [ ] 📝 Documentation updated
- [ ] 🔄 Code reviewed
- [ ] ✅ Git commit with detailed changelog

---

## 🎯 ФИНАЛЬНАЯ ОЦЕНКА РИСКОВ

| Категория | Вероятность проблем | Критичность | Итого |
|-----------|---------------------|-------------|-------|
| Воспроизведение | 40% | 🔴 КРИТИЧНО | 🔴 |
| UI обновления | 60% | 🟠 ВАЖНО | 🟠 |
| База данных | 20% | 🟠 ВАЖНО | 🟡 |
| Фоновая работа | 30% | 🟠 ВАЖНО | 🟡 |
| Особые функции | 50% | 🟡 НЕКРИТИЧНО | 🟡 |

**Общий риск Фазы 1:** 🟠 **СРЕДНИЙ** (при правильном подходе)

**Рекомендация:** 
- ✅ Выполнять поэтапно с тестированием после каждого шага
- ✅ Начать с наименее рискованных задач для "разогрева"
- ✅ Критичные задачи (PlayStationTask, FragmentPlayerFull) делать в первую очередь
- ✅ Держать backup ветку для быстрого rollback
- ✅ Тестировать на реальном устройстве

