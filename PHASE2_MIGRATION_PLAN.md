# 🎯 ПЛАН МИГРАЦИИ - ФАЗА 2 (ВАЖНЫЕ УЛУЧШЕНИЯ)

**Цель**: Устранить оставшиеся deprecated API и code smells  
**Сроки**: 2-3 недели  
**Приоритет**: 🟠 СРЕДНИЙ

**Статус Фазы 1:** ✅ 100% ЗАВЕРШЕНА И ПРОТЕСТИРОВАНА

---

## 📊 ЧТО ОСТАЛОСЬ ИЗ АНАЛИЗА

### 🟠 **Критичные оставшиеся проблемы:**

| Проблема | Количество | Риск | Приоритет |
|----------|-----------|------|-----------|
| **AsyncTask в UI** | 3 файла (ActivityMain, FragmentHistory, FragmentBase) | 🟠 30% | P1 |
| **Handler() без Looper** | 13 случаев в 9 файлах | 🟡 20% | P2 |
| **PagedList deprecated** | 4 файла | 🟠 30% | P1 |
| **static Context** | 99 случаев в 21 файле | 🟡 40% | P2 |
| **LocalBroadcastManager** | Много мест | 🟡 15% | P2 |
| **android.preference.PreferenceManager** | 2 места | 🟢 10% | P3 |

---

## 📋 ЗАДАЧА 2.1: Оставшиеся AsyncTask в UI (3 файла)

### ⚠️ ОЦЕНКА РИСКОВ

| Файл | AsyncTask'ов | Риск | Причина |
|------|--------------|------|---------|
| **ActivityMain.java** | 2 | 🟠 30% | UI search operations |
| **FragmentHistory.java** | 1 | 🟡 20% | History loading |
| **FragmentBase.java** | 1 | 🟡 20% | Generic fragment operations |
| **FragmentServerInfo.java** | 1 | 🟢 10% | Server info display |

### 📝 ПЛАН:

#### **2.1.1: ActivityMain.java (2 AsyncTask):**
- Line 603: Intent deep link handling
- Line 632: Voice search handling
**Время:** 1-2 часа  
**Риск:** 🟠 Средний (UI operations)

#### **2.1.2: FragmentHistory.java:**
- Загрузка истории воспроизведения
**Время:** 1 час  
**Риск:** 🟡 Низкий

#### **2.1.3: FragmentBase.java:**
- Generic async operations для фрагментов
**Время:** 1 час  
**Риск:** 🟡 Низкий

#### **2.1.4: FragmentServerInfo.java:**
- Server stats display
**Время:** 30 минут  
**Риск:** 🟢 Очень низкий

---

## 📋 ЗАДАЧА 2.2: Handler() → Handler(Looper.getMainLooper())

### ⚠️ ОЦЕНКА РИСКОВ

**Общий риск:** 🟡 20% (простая замена, но много мест)

### Файлы для исправления (13 случаев в 9 файлах):

| Файл | Количество | Приоритет |
|------|-----------|-----------|
| NoNameRadioApp.java | 2 | P1 |
| ActivityMain.java | 4 | P1 |
| Utils.java | 1 | P2 |
| FragmentPlayerFull.java | 1 | P2 |
| PlayStationTask.java | 1 | P2 |
| PlayerSelectorAdapter.java | 1 | P3 |
| RefreshHandler.java | 1 | P3 |
| CustomFilter.java | 1 | P3 |
| UiHandler.java | 1 | P3 |

### 📝 ПЛАН:

#### **2.2.1: Критичные файлы (NoNameRadioApp, ActivityMain):**
**Время:** 30 минут  
**Риск:** 🟡 15%

#### **2.2.2: Остальные файлы:**
**Время:** 1 час  
**Риск:** 🟢 10%

---

## 📋 ЗАДАЧА 2.3: PagedList → Paging 3

### ⚠️ ОЦЕНКА РИСКОВ

**Общий риск:** 🟠 30% (архитектурное изменение)

### Файлы для миграции (4 файла):

| Файл | Тип | Риск |
|------|-----|------|
| **TrackHistoryRepository.java** | Data layer | 🟠 30% |
| **TrackHistoryViewModel.java** | ViewModel | 🟠 25% |
| **TrackHistoryAdapter.java** | RecyclerView Adapter | 🟠 35% |
| **FragmentPlayerFull.java** | Observer usage | 🟡 20% |

### 📝 ПЛАН:

#### **Миграция Paging 2 → Paging 3:**

```kotlin
// ДО (Paging 2):
LivePagedListBuilder<Int, TrackHistoryEntry>(
    dataSource.factory,
    PagedList.Config.Builder()
        .setPageSize(50)
        .build()
).build()

// ПОСЛЕ (Paging 3):
Pager(
    config = PagingConfig(pageSize = 50),
    pagingSourceFactory = { TrackHistoryPagingSource(dao) }
).flow.cachedIn(viewModelScope)
```

**Время:** 3-4 часа  
**Риск:** 🟠 Средний (требует изменений в Repository/ViewModel/Adapter)

---

## 📋 ЗАДАЧА 2.4: LocalBroadcastManager → EventBus (оставшиеся места)

### Оставшиеся файлы:
- FragmentPlayerSmall.java (2 места)
- FragmentPlayerFull.java (2 места)
- PlayerSelectorDialog.java (2 места)
- FragmentBase.java (1 место)
- FragmentServerInfo.java (1 место)
- ItemAdapterStation.java (1 место)

**Время:** 2-3 часа  
**Риск:** 🟡 20%

---

## 📋 ЗАДАЧА 2.5: android.preference.PreferenceManager → androidx

### Файлы:
- RadioAlarmManager.java (2 места)

**Время:** 15 минут  
**Риск:** 🟢 5%

---

## 📊 ОЦЕНКА ВРЕМЕНИ ФАЗЫ 2

| Задача | Оценка | Риск |
|--------|--------|------|
| 2.1 Оставшиеся AsyncTask | 3-4 часа | 🟠 |
| 2.2 Handler() fix | 1.5 часа | 🟡 |
| 2.3 Paging 2 → 3 | 3-4 часа | 🟠 |
| 2.4 LocalBroadcastManager | 2-3 часа | 🟡 |
| 2.5 PreferenceManager | 15 минут | 🟢 |
| **Разработка** | **10-13 часов** | |
| **Тестирование** | **5-8 часов** | |
| **ИТОГО** | **15-21 час** (2-3 дня) | |

---

## 🎯 ПОРЯДОК ВЫПОЛНЕНИЯ (ОТ ПРОСТОГО К СЛОЖНОМУ)

### День 1:
1. ✅ PreferenceManager → androidx (15 мин, риск 5%)
2. ✅ Handler() fixes (1.5 часа, риск 20%)
3. ✅ FragmentServerInfo AsyncTask (30 мин, риск 10%)
4. ✅ FragmentBase AsyncTask (1 час, риск 20%)

### День 2:
5. ✅ FragmentHistory AsyncTask (1 час, риск 20%)
6. ✅ ActivityMain AsyncTask (2 часа, риск 30%)
7. ✅ LocalBroadcastManager → EventBus (2-3 часа, риск 20%)

### День 3:
8. ✅ Paging 2 → Paging 3 (3-4 часа, риск 30%)
9. ✅ Финальное тестирование

---

## ✅ КРИТЕРИИ ЗАВЕРШЕНИЯ ФАЗЫ 2

- [ ] Все AsyncTask заменены (100%)
- [ ] Все Handler() с явным Looper
- [ ] Paging 3 вместо PagedList
- [ ] LocalBroadcastManager полностью удален
- [ ] androidx.preference вместо android.preference
- [ ] ✅ Компиляция без errors
- [ ] ✅ Unit тесты проходят
- [ ] ✅ Приложение работает стабильно
- [ ] ✅ Нет deprecated warnings для мигрированных компонентов

---

## 🚀 ГОТОВ НАЧАТЬ ФАЗУ 2?

**Преимущества:**
- 🎯 Еще больше deprecated API устранено
- 🎯 Современный Paging 3 для истории треков
- 🎯 Полная миграция на EventBus
- 🎯 Thread-safe Handler'ы

**Риски:**
- ⚠️ Фаза 2 менее критична чем Фаза 1
- ⚠️ Можно остановиться на Фазе 1 (уже большой прогресс)
- ⚠️ Потребуется еще 2-3 дня работы

**Рекомендую начать с самых простых задач:**
1. PreferenceManager (5% риск)
2. Handler() fixes (20% риск)
3. Простые AsyncTask

