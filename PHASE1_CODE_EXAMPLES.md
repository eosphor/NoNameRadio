# 💻 ПРИМЕРЫ КОДА ДЛЯ ФАЗЫ 1

Детальные примеры миграции с объяснением рисков и решений.

---

## 1️⃣ МИГРАЦИЯ AsyncTask → CompletableFuture

### ❌ АНТИПАТТЕРНЫ (ЧТО НЕ ДЕЛАТЬ)

#### **Антипаттерн #1: Использование thread pool без shutdown**
```java
// ❌ ПЛОХО - утечка памяти
public class BadExample {
    private ExecutorService executor = Executors.newCachedThreadPool();
    
    public void doWork() {
        executor.submit(() -> {
            // work
        });
        // executor никогда не закрывается!
    }
}
```

**Проблема:** ExecutorService не останавливается, потоки висят в памяти  
**Последствия:** Memory leak, app зависает при закрытии  
**Обнаружение:** LeakCanary показывает утечку, Android Profiler показывает рост потоков

#### **Антипаттерн #2: UI обновление не на main thread**
```java
// ❌ ПЛОХО - crash при обновлении UI
CompletableFuture.supplyAsync(() -> {
    String result = doNetworkCall();
    textView.setText(result); // CRASH! Not on main thread
    return result;
});
```

**Проблема:** UI обновляется в background thread  
**Последствия:** `CalledFromWrongThreadException`  
**Обнаружение:** Immediate crash при запуске

#### **Антипаттерн #3: Неправильная обработка ошибок**
```java
// ❌ ПЛОХО - ошибки теряются
CompletableFuture.supplyAsync(() -> {
    return riskyOperation(); // Exception исчезнет в никуда
});
```

**Проблема:** Exception не обрабатывается  
**Последствия:** Молчаливый fail, пользователь не понимает что сломалось  
**Обнаружение:** Функция просто не работает, нет логов

---

### ✅ ПРАВИЛЬНЫЕ ПАТТЕРНЫ

#### **Паттерн #1: Singleton ExecutorService с правильным shutdown**
```java
public class AsyncExecutor {
    private static volatile AsyncExecutor instance;
    private final ExecutorService executor;
    
    private AsyncExecutor() {
        // Используем cached thread pool для разных типов задач
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setName("AsyncExecutor-" + thread.getId());
            thread.setDaemon(false); // Не daemon - нужно явно останавливать
            return thread;
        });
    }
    
    public static AsyncExecutor getInstance() {
        if (instance == null) {
            synchronized (AsyncExecutor.class) {
                if (instance == null) {
                    instance = new AsyncExecutor();
                }
            }
        }
        return instance;
    }
    
    public <T> CompletableFuture<T> submit(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executor);
    }
    
    // Вызывать при выходе из приложения
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

**Использование в NoNameRadioApp:**
```java
public class NoNameRadioApp extends Application {
    private AsyncExecutor asyncExecutor;
    
    @Override
    public void onCreate() {
        super.onCreate();
        asyncExecutor = AsyncExecutor.getInstance();
    }
    
    @Override
    public void onTerminate() {
        asyncExecutor.shutdown();
        super.onTerminate();
    }
    
    public AsyncExecutor getAsyncExecutor() {
        return asyncExecutor;
    }
}
```

---

#### **Паттерн #2: UI обновления на main thread**
```java
public class UiExecutor {
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    public static void runOnUiThread(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            mainHandler.post(action);
        }
    }
    
    public static <T> CompletableFuture<T> onUiThread(CompletableFuture<T> future) {
        return future.thenApplyAsync(result -> {
            return result;
        }, mainHandler::post);
    }
}

// Использование:
asyncExecutor.submit(() -> {
    return doNetworkCall();
}).thenAccept(result -> {
    UiExecutor.runOnUiThread(() -> {
        textView.setText(result);
    });
});
```

---

#### **Паттерн #3: Правильная обработка ошибок**
```java
public class ErrorHandler {
    private static final String TAG = "ErrorHandler";
    private final Context context;
    
    public ErrorHandler(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public <T> void handleError(CompletableFuture<T> future, String operation) {
        future.exceptionally(throwable -> {
            Log.e(TAG, "Error in " + operation, throwable);
            
            String message = getErrorMessage(throwable);
            UiExecutor.runOnUiThread(() -> {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            });
            
            return null;
        });
    }
    
    private String getErrorMessage(Throwable throwable) {
        if (throwable instanceof IOException) {
            return context.getString(R.string.error_network);
        } else if (throwable instanceof TimeoutException) {
            return context.getString(R.string.error_timeout);
        } else {
            return context.getString(R.string.error_unknown);
        }
    }
}
```

---

### 🔧 ПРИМЕР МИГРАЦИИ: PlayStationTask.java

#### **ДО (AsyncTask):**
```java
public class PlayStationTask extends AsyncTask<Void, Void, String> {
    private final PlayFunc playFunc;
    private final DataRadioStation stationToPlay;
    private final WeakReference<Context> contextWeakReference;
    private final PostExecuteTask postExecuteTask;
    
    public PlayStationTask(Context context, DataRadioStation station, 
                          PlayFunc playFunc, PostExecuteTask callback) {
        this.contextWeakReference = new WeakReference<>(context);
        this.stationToPlay = station;
        this.playFunc = playFunc;
        this.postExecuteTask = callback;
    }
    
    @Override
    protected String doInBackground(Void... voids) {
        Context context = contextWeakReference.get();
        if (context == null) return null;
        
        try {
            // Network call to resolve URL
            String resolvedUrl = resolveStreamUrl(stationToPlay.Url);
            return resolvedUrl;
        } catch (IOException e) {
            Log.e(TAG, "Error resolving URL", e);
            return null;
        }
    }
    
    @Override
    protected void onPostExecute(String resolvedUrl) {
        if (resolvedUrl != null) {
            playFunc.play(resolvedUrl);
            postExecuteTask.onPostExecute(ExecutionResult.SUCCESS);
        } else {
            postExecuteTask.onPostExecute(ExecutionResult.FAILURE);
        }
    }
}

// Использование:
new PlayStationTask(context, station, url -> player.play(url), result -> {
    if (result == SUCCESS) {
        hideLoading();
    } else {
        showError();
    }
}).execute();
```

---

#### **ПОСЛЕ (CompletableFuture):**
```java
public class PlayStationTask {
    private static final String TAG = "PlayStationTask";
    
    private final WeakReference<Context> contextWeakReference;
    private final DataRadioStation stationToPlay;
    private final PlayFunc playFunc;
    private final PostExecuteTask postExecuteTask;
    private final AsyncExecutor asyncExecutor;
    private final ErrorHandler errorHandler;
    
    public PlayStationTask(Context context, DataRadioStation station, 
                          PlayFunc playFunc, PostExecuteTask callback) {
        this.contextWeakReference = new WeakReference<>(context);
        this.stationToPlay = station;
        this.playFunc = playFunc;
        this.postExecuteTask = callback;
        
        NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();
        this.asyncExecutor = app.getAsyncExecutor();
        this.errorHandler = new ErrorHandler(context);
    }
    
    public CompletableFuture<String> executeAsync() {
        // Показываем loading сразу (на текущем thread)
        EventBus.post(new ShowLoadingEvent());
        
        // Background работа
        CompletableFuture<String> future = asyncExecutor.submit(() -> {
            Context context = contextWeakReference.get();
            if (context == null) {
                throw new IllegalStateException("Context is null");
            }
            
            // Network call
            String resolvedUrl = resolveStreamUrl(stationToPlay.Url);
            if (resolvedUrl == null || resolvedUrl.isEmpty()) {
                throw new IOException("Failed to resolve URL");
            }
            
            return resolvedUrl;
        });
        
        // UI обновление на main thread
        future.thenAccept(resolvedUrl -> {
            UiExecutor.runOnUiThread(() -> {
                playFunc.play(resolvedUrl);
                postExecuteTask.onPostExecute(ExecutionResult.SUCCESS);
                EventBus.post(new HideLoadingEvent());
            });
        });
        
        // Обработка ошибок
        future.exceptionally(throwable -> {
            Log.e(TAG, "Error in PlayStationTask", throwable);
            
            UiExecutor.runOnUiThread(() -> {
                postExecuteTask.onPostExecute(ExecutionResult.FAILURE);
                EventBus.post(new HideLoadingEvent());
                
                Context context = contextWeakReference.get();
                if (context != null) {
                    String message;
                    if (throwable instanceof IOException) {
                        message = context.getString(R.string.error_station_load);
                    } else if (throwable instanceof TimeoutException) {
                        message = context.getString(R.string.error_timeout);
                    } else {
                        message = context.getString(R.string.error_unknown);
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            });
            
            return null;
        });
        
        return future;
    }
    
    private String resolveStreamUrl(String url) throws IOException {
        // Существующая логика
        // ...
    }
}

// Использование:
PlayStationTask task = new PlayStationTask(context, station, 
    url -> player.play(url), 
    result -> {
        if (result == SUCCESS) {
            Log.d(TAG, "Station started successfully");
        } else {
            Log.e(TAG, "Failed to start station");
        }
    });

CompletableFuture<String> future = task.executeAsync();

// Опционально: можно отменить
// future.cancel(true);
```

---

### 🧪 ТЕСТИРОВАНИЕ МИГРАЦИИ AsyncTask

#### **Unit Test:**
```java
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class PlayStationTaskTest {
    
    @Test
    public void testSuccessfulUrlResolution() throws Exception {
        Context context = RuntimeEnvironment.getApplication();
        DataRadioStation station = new DataRadioStation();
        station.Url = "http://example.com/stream.mp3";
        
        AtomicBoolean played = new AtomicBoolean(false);
        AtomicReference<ExecutionResult> result = new AtomicReference<>();
        
        PlayStationTask task = new PlayStationTask(
            context, 
            station,
            url -> played.set(true),
            res -> result.set(res)
        );
        
        CompletableFuture<String> future = task.executeAsync();
        
        // Ждем завершения
        future.get(5, TimeUnit.SECONDS);
        
        assertTrue(played.get());
        assertEquals(ExecutionResult.SUCCESS, result.get());
    }
    
    @Test
    public void testNetworkError() throws Exception {
        // Тест обработки ошибок
        // ...
    }
}
```

---

## 2️⃣ МИГРАЦИЯ Observable → LiveData

### ❌ АНТИПАТТЕРНЫ

#### **Антипаттерн #1: Забыть удалить observer**
```java
// ❌ ПЛОХО - утечка памяти
liveData.observe(this, value -> {
    // Если Fragment уничтожается, observer остается
});
```

**Проблема:** Observer не отвязывается при уничтожении  
**Последствия:** Memory leak  
**Решение:** Использовать `getViewLifecycleOwner()` для Fragment

#### **Антипаттерн #2: Observe в неправильном lifecycle**
```java
// ❌ ПЛОХО - observer живет дольше чем view
@Override
public void onCreate(Bundle savedInstanceState) {
    liveData.observe(this, value -> {
        updateUI(value); // View может быть null!
    });
}
```

**Проблема:** Observer привязан к Fragment lifecycle, не View lifecycle  
**Последствия:** NPE при обновлении UI после onDestroyView  
**Решение:** `observe(getViewLifecycleOwner(), ...)`

---

### ✅ ПРАВИЛЬНЫЕ ПАТТЕРНЫ

#### **Паттерн #1: Observable → LiveData в Manager классе**
```java
// ДО:
public class RadioAlarmManager {
    private class AlarmsObservable extends Observable {
        @Override
        public synchronized boolean hasChanged() {
            return true;
        }
    }
    
    private final Observable savedAlarmsObservable = new AlarmsObservable();
    
    public Observable getSavedAlarmsObservable() {
        return savedAlarmsObservable;
    }
    
    private void notifyChange() {
        savedAlarmsObservable.notifyObservers();
    }
}

// ПОСЛЕ:
public class RadioAlarmManager {
    private final MutableLiveData<List<DataRadioStationAlarm>> alarmsLiveData = 
        new MutableLiveData<>();
    
    public LiveData<List<DataRadioStationAlarm>> getAlarmsLiveData() {
        return alarmsLiveData;
    }
    
    private void notifyChange() {
        // Вызывать из background thread - безопасно
        alarmsLiveData.postValue(new ArrayList<>(list));
    }
    
    // Или из main thread:
    private void notifyChangeFromMainThread() {
        alarmsLiveData.setValue(new ArrayList<>(list));
    }
}
```

---

#### **Паттерн #2: Правильный observe во Fragment**
```java
// ДО:
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    RadioAlarmManager manager = app.getRadioAlarmManager();
    manager.getSavedAlarmsObservable().addObserver(new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            // Нужно вручную удалять в onDestroyView!
            updateAlarmsList();
        }
    });
}

// ПОСЛЕ:
@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    RadioAlarmManager manager = app.getRadioAlarmManager();
    
    // getViewLifecycleOwner() - автоматически unsubscribe при onDestroyView
    manager.getAlarmsLiveData().observe(getViewLifecycleOwner(), alarms -> {
        updateAlarmsList(alarms);
    });
}
```

---

### 🔧 ПРИМЕР МИГРАЦИИ: FragmentPlayerFull.java

#### **ДО (Observable):**
```java
public class FragmentPlayerFull extends Fragment {
    private BroadcastReceiver updateUIReceiver;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Регистрация receiver
        updateUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI();
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.nonameradio.app.PLAYER_STATE_CHANGED");
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(updateUIReceiver, filter);
    }
    
    @Override
    public void onDestroyView() {
        // Нужно вручную отписываться!
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(updateUIReceiver);
        super.onDestroyView();
    }
}
```

#### **ПОСЛЕ (LiveData + ViewModel):**
```java
public class PlayerViewModel extends ViewModel {
    private final MutableLiveData<PlayState> playerState = new MutableLiveData<>();
    private final MutableLiveData<DataRadioStation> currentStation = new MutableLiveData<>();
    private final MutableLiveData<String> metadata = new MutableLiveData<>();
    
    public LiveData<PlayState> getPlayerState() {
        return playerState;
    }
    
    public LiveData<DataRadioStation> getCurrentStation() {
        return currentStation;
    }
    
    public LiveData<String> getMetadata() {
        return metadata;
    }
    
    public void updatePlayerState(PlayState state) {
        playerState.postValue(state);
    }
    
    public void updateStation(DataRadioStation station) {
        currentStation.postValue(station);
    }
    
    public void updateMetadata(String meta) {
        metadata.postValue(meta);
    }
}

public class FragmentPlayerFull extends Fragment {
    private PlayerViewModel viewModel;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // ViewModel автоматически переживает configuration changes
        viewModel = new ViewModelProvider(requireActivity()).get(PlayerViewModel.class);
        
        // Автоматический unsubscribe при onDestroyView
        viewModel.getPlayerState().observe(getViewLifecycleOwner(), state -> {
            updatePlayPauseButton(state);
        });
        
        viewModel.getCurrentStation().observe(getViewLifecycleOwner(), station -> {
            updateStationInfo(station);
        });
        
        viewModel.getMetadata().observe(getViewLifecycleOwner(), metadata -> {
            updateMetadataDisplay(metadata);
        });
    }
    
    // Не нужен onDestroyView для cleanup!
}
```

---

## 🎯 ЧЕКЛИСТ МИГРАЦИИ

### ☑️ AsyncTask → CompletableFuture
- [ ] Создан `AsyncExecutor` singleton
- [ ] Создан `UiExecutor` для UI updates
- [ ] Все AsyncTask заменены
- [ ] Обработка ошибок добавлена везде
- [ ] ExecutorService правильно shutdown в Application.onTerminate()
- [ ] Нет memory leaks (LeakCanary)
- [ ] Нет thread exceptions (Logcat)

### ☑️ Observable → LiveData
- [ ] Создан ViewModel для каждого экрана
- [ ] Все Observable заменены на LiveData
- [ ] Используется `getViewLifecycleOwner()` во Fragment
- [ ] Нет manual unsubscribe (все автоматически)
- [ ] Configuration changes работают (rotation)
- [ ] Нет memory leaks (LeakCanary)

### ☑️ Lifecycle методы
- [ ] `onActivityCreated` → `onCreate`
- [ ] `setRetainInstance` удален
- [ ] Состояние хранится в ViewModel
- [ ] Rotation работает корректно

---

## 📊 ТАБЛИЦА РИСКОВ ПО ФАЙЛАМ

| Файл | Сложность | Риск поломки | Время на fix | Стратегия |
|------|-----------|--------------|--------------|-----------|
| PlayStationTask.java | 🔴 Высокая | 40% | 4-6 часов | Детальное тестирование |
| GetRealLinkAndPlayTask.java | 🟠 Средняя | 30% | 2-3 часа | Тесты с разными URL |
| FragmentPlayerFull.java | 🔴 Высокая | 50% | 6-8 часов | ViewModel + LiveData |
| StationActions.java | 🟡 Низкая | 20% | 1-2 часа | Простая миграция |
| MPD Tasks | 🟢 Очень низкая | 10% | 1 час | Minimal testing |

---

**🎯 ИТОГО:** С правильным подходом и тестированием риск успешно минимизируется до приемлемого уровня.

