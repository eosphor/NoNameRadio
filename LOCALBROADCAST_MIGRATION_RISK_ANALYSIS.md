# ⚠️ АНАЛИЗ РИСКОВ: LocalBroadcastManager → EventBus

**Дата анализа**: 12 октября 2025  
**Статус**: Детальный анализ перед миграцией  
**Оценка риска**: 🟡 **СРЕДНИЙ-ВЫСОКИЙ** (требуется осторожность)

---

## 📊 МАСШТАБ МИГРАЦИИ

### Файлы для миграции (11 файлов)
| № | Файл | Использование | Риск |
|---|------|---------------|------|
| 1 | **PlayerService.java** | Отправитель событий (SENDER) | 🔴 ВЫСОКИЙ |
| 2 | FragmentPlayerFull.java | Получатель: TIMER + META + STATE | 🟡 СРЕДНИЙ |
| 3 | FragmentPlayerSmall.java | Получатель: META + STATE | 🟡 СРЕДНИЙ |
| 4 | NoNameRadioBrowserService.java | Сервис (Android Auto) | 🔴 ВЫСОКИЙ |
| 5 | FragmentStations.java | UI список станций | 🟢 НИЗКИЙ |
| 6 | MediaControllerHelper.java | Контроллер MediaSession | 🔴 ВЫСОКИЙ |
| 7 | MediaSessionManager.java | Управление MediaSession | 🔴 ВЫСОКИЙ |
| 8 | StationActions.java | Действия со станциями | 🟢 НИЗКИЙ |
| 9 | ItemAdapterStation.java | Adapter для списка станций | 🟢 НИЗКИЙ |
| 10 | PlayerSelectorDialog.java | Диалог выбора плеера | 🟢 НИЗКИЙ |
| 11 | BroadcastReceiverManager.java | Менеджер receivers | 🔴 ВЫСОКИЙ |
| 12 | PlayerServiceUtil.java | Утилиты сервиса | 🟡 СРЕДНИЙ |

### НЕ мигрировать (System BroadcastReceivers)
- ❌ HeadsetConnectionReceiver.java - системные события headset
- ❌ BecomingNoisyReceiver.java - системное событие audio becoming noisy
- ❌ AlarmReceiver.java - системный alarm
- ❌ AlarmSetupReceiver.java - системный alarm setup
- ❌ BootReceiver.java - системное событие BOOT_COMPLETED

---

## 🎯 СОБЫТИЯ ДЛЯ МИГРАЦИИ

### Текущие LocalBroadcast события
```java
// PlayerService.java
PLAYER_SERVICE_TIMER_UPDATE = "com.nonameradio.app.timerupdate"
PLAYER_SERVICE_META_UPDATE = "com.nonameradio.app.metaupdate"
PLAYER_SERVICE_STATE_CHANGE = "com.nonameradio.app.statechange"
PLAYER_SERVICE_BOUND = "com.nonameradio.app.playerservicebound"

// ActivityMain.java
ACTION_SHOW_LOADING = "com.nonameradio.app.show_loading"
ACTION_HIDE_LOADING = "com.nonameradio.app.hide_loading"
ACTION_PLAYER_STATE_CHANGED = "com.nonameradio.app.player_state_changed"
```

### Новые EventBus события (нужно создать)
```java
✅ ShowLoadingEvent - УЖЕ СОЗДАН
✅ HideLoadingEvent - УЖЕ СОЗДАН
✅ PlayerStateChangeEvent - УЖЕ СОЗДАН
✅ MeteredConnectionEvent - УЖЕ СОЗДАН
✅ MediaSessionUpdateEvent - УЖЕ СОЗДАН

🆕 TimerUpdateEvent - НУЖНО СОЗДАТЬ
🆕 MetaUpdateEvent - НУЖНО СОЗДАТЬ
🆕 PlayerServiceBoundEvent - НУЖНО СОЗДАТЬ (возможно не нужен)
```

---

## ⚠️ КРИТИЧЕСКИЕ РИСКИ

### 🔴 РИСК 1: PlayerService - центральный компонент
**Проблема**:
- PlayerService отправляет события в 4+ места
- Используется для playback control
- Ошибка может сломать весь playback

**Митигация**:
1. ✅ Создать backup branch
2. ✅ Мигрировать постепенно (сначала получатели, потом отправители)
3. ✅ После каждого шага - тестирование
4. ✅ Сохранить старый код закомментированным

---

### 🔴 РИСК 2: MediaSession интеграция
**Проблема**:
- MediaControllerHelper и MediaSessionManager взаимосвязаны
- Ошибка может сломать Android Auto / Bluetooth control
- Notification управление зависит от этих компонентов

**Митигация**:
1. ✅ Изучить зависимости перед изменениями
2. ✅ Мигрировать MediaControllerHelper и MediaSessionManager вместе
3. ✅ Проверить notification отображение после миграции

---

### 🟡 РИСК 3: Таймер обновления (PLAYER_SERVICE_TIMER_UPDATE)
**Проблема**:
- Событие отправляется очень часто (каждую секунду)
- EventBus может не справиться с частыми событиями
- Может быть performance issue

**Митигация**:
1. ✅ Проверить производительность EventBus с частыми событиями
2. ✅ Возможно, оставить таймер на LocalBroadcastManager
3. ✅ Или использовать throttling в EventBus

---

### 🟡 РИСК 4: Fragment lifecycle
**Проблема**:
- FragmentPlayerFull и FragmentPlayerSmall регистрируют receivers в onResume/onPause
- EventBus register/unregister в неправильном месте может вызвать memory leaks

**Митигация**:
1. ✅ Использовать getViewLifecycleOwner() для Fragment observers
2. ✅ Регистрировать EventBus в onViewCreated/onDestroyView
3. ✅ Проверить отсутствие leaks после миграции

---

### 🟡 РИСК 5: BroadcastReceiverManager
**Проблема**:
- Этот класс управляет всеми BroadcastReceivers
- Неясна его роль и зависимости
- Может быть сложная логика

**Митигация**:
1. ✅ Детально изучить этот класс перед изменениями
2. ✅ Возможно, оставить как есть или refactor полностью

---

## 📋 ПЛАН МИГРАЦИИ (поэтапный)

### ФАЗА 1: Подготовка (БЕЗОПАСНО)
1. ✅ Создать backup branch `backup-before-localbroadcast-migration`
2. ✅ Создать недостающие EventBus события:
   - TimerUpdateEvent.java
   - MetaUpdateEvent.java
3. ✅ Написать тесты для новых событий
4. ✅ Commit: "feat: Add EventBus events for LocalBroadcastManager migration"

**Риск**: 🟢 МИНИМАЛЬНЫЙ (только добавление классов)

---

### ФАЗА 2: UI компоненты (НИЗКИЙ РИСК)
**Порядок миграции**:
1. ✅ StationActions.java (простой)
2. ✅ ItemAdapterStation.java (adapter)
3. ✅ PlayerSelectorDialog.java (dialog)
4. ✅ FragmentStations.java (список)

**После каждого файла**:
- ✅ Compile
- ✅ Run app
- ✅ Проверить UI

**Риск**: 🟢 НИЗКИЙ (изолированные UI компоненты)

---

### ФАЗА 3: Получатели в Fragment (СРЕДНИЙ РИСК)
**Порядок миграции**:
1. ✅ FragmentPlayerSmall.java (получатель META + STATE)
2. ✅ FragmentPlayerFull.java (получатель TIMER + META + STATE)

**Критично**:
- ❗ Использовать ViewLifecycleOwner
- ❗ Проверить register/unregister в правильных местах
- ❗ Проверить отсутствие memory leaks

**После КАЖДОГО файла**:
- ✅ Compile
- ✅ Run app
- ✅ Открыть Player (Small и Full)
- ✅ Запустить станцию
- ✅ Проверить что таймер обновляется
- ✅ Проверить что metadata отображается

**Риск**: 🟡 СРЕДНИЙ (core UI компоненты)

---

### ФАЗА 4: MediaSession компоненты (ВЫСОКИЙ РИСК)
**Порядок миграции** (вместе!):
1. ⚠️ MediaSessionManager.java
2. ⚠️ MediaControllerHelper.java
3. ⚠️ NoNameRadioBrowserService.java (Android Auto)

**Критично**:
- ❗ Мигрировать ВСЕ ТРИ ФАЙЛА вместе в одном коммите
- ❗ Проверить Android Auto (если есть возможность)
- ❗ Проверить Bluetooth control
- ❗ Проверить notification controls

**После миграции**:
- ✅ Compile
- ✅ Run app
- ✅ Запустить станцию
- ✅ Проверить notification
- ✅ Проверить кнопки в notification
- ✅ Проверить Bluetooth headset control (если есть)

**Риск**: 🔴 ВЫСОКИЙ (критичные для playback control)

---

### ФАЗА 5: Отправители (ОЧЕНЬ ВЫСОКИЙ РИСК)
**Файлы**:
1. ⚠️⚠️ PlayerService.java (SENDER всех событий)
2. ⚠️ PlayerServiceUtil.java

**Стратегия**:
- ❗ Заменить sendBroadcast() на EventBus.post()
- ❗ Сохранить старый код закомментированным
- ❗ Добавить extensive logging

**После миграции**:
- ✅ Compile
- ✅ Run FULL regression test:
  - Запуск станции
  - Пауза/Resume
  - Переключение станций
  - Таймер обновление
  - Metadata обновление
  - Notification управление
  - Bluetooth control

**Риск**: 🔴🔴 ОЧЕНЬ ВЫСОКИЙ (центральный компонент)

---

### ФАЗА 6: BroadcastReceiverManager (НЕЯСНЫЙ РИСК)
**Файлы**:
1. ❓ BroadcastReceiverManager.java

**Стратегия**:
1. ✅ Детально изучить этот класс
2. ✅ Понять его роль
3. ✅ Решить: мигрировать или оставить как есть

**Риск**: ❓ НЕЯСНЫЙ (требуется анализ)

---

## 🧪 ТЕСТИРОВАНИЕ

### После каждой фазы
```
✅ Compile: ./gradlew :NoNameRadio:app:compileFreeDebugJavaWithJavac
✅ Build APK: ./gradlew :NoNameRadio:app:assembleFreeDebug
✅ Install: adb install -r <apk>
✅ Launch: adb shell am start -n com.nonameradio.app.debug/...
✅ Check logs: adb logcat -d | grep -E "NoNameRadio|FATAL"
```

### Регрессионное тестирование (после Фазы 5)
```
✅ Запуск станции - работает
✅ Пауза - работает
✅ Resume - работает
✅ Переключение станций - работает
✅ Таймер обновляется - работает
✅ Metadata отображается - работает
✅ Notification показывается - работает
✅ Notification controls - работают
✅ Bluetooth control - работает (если есть)
✅ Android Auto - работает (если есть)
```

---

## ⏱️ ОЦЕНКА ВРЕМЕНИ

| Фаза | Время | Риск |
|------|-------|------|
| Фаза 1: Подготовка | 15 мин | 🟢 |
| Фаза 2: UI компоненты | 30 мин | 🟢 |
| Фаза 3: Fragment получатели | 45 мин | 🟡 |
| Фаза 4: MediaSession | 1 час | 🔴 |
| Фаза 5: Отправители (PlayerService) | 1 час | 🔴🔴 |
| Фаза 6: BroadcastReceiverManager | 30 мин | ❓ |
| **ИТОГО** | **~4 часа** | 🟡 |

---

## 💡 РЕКОМЕНДАЦИИ

### ✅ ДО начала миграции
1. ✅ Создать backup branch
2. ✅ Убедиться что текущая версия работает
3. ✅ Сохранить APK текущей версии для сравнения

### ✅ ВО ВРЕМЯ миграции
1. ✅ Мигрировать поэтапно (не все сразу)
2. ✅ После каждого файла - compile + test
3. ✅ Коммитить часто (каждый файл или маленькую группу)
4. ✅ Сохранять старый код закомментированным
5. ✅ Добавлять logging для отладки

### ✅ ПОСЛЕ миграции
1. ✅ Полное регрессионное тестирование
2. ✅ Проверить memory leaks (Android Profiler)
3. ✅ Проверить performance (таймер не должен лагать)
4. ✅ Создать детальный отчет

---

## 🚨 КРИТЕРИИ ОТКАТА

**Откатить миграцию если**:
- ❌ Playback не работает
- ❌ Таймер лагает или не обновляется
- ❌ Metadata не отображается
- ❌ Notification не показывается
- ❌ Notification controls не работают
- ❌ Есть crashes или FATAL errors
- ❌ Обнаружены memory leaks

**Как откатить**:
```bash
git reset --hard backup-before-localbroadcast-migration
```

---

## 🎯 ЗАКЛЮЧЕНИЕ

**Миграция возможна, но требует**:
- ⚠️ Очень осторожного подхода
- ⚠️ Поэтапной реализации
- ⚠️ Extensive тестирования после каждого шага
- ⚠️ Готовности к откату

**Рекомендация**: Выполнить миграцию в **6 фаз**, начиная с самых безопасных компонентов (UI) и заканчивая самыми рискованными (PlayerService).

**Альтернатива**: Оставить LocalBroadcastManager как есть, так как:
- ✅ Приложение работает стабильно
- ✅ LocalBroadcastManager не deprecated
- ✅ Android его поддерживает

---

**Версия**: 1.0  
**Автор**: AI Assistant  
**Дата**: 12 октября 2025, 15:35

