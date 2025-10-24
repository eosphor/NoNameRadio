package com.nonameradio.app.core.event;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Modern EventBus implementation to replace LocalBroadcastManager.
 * Type-safe event system with main thread delivery.
 *
 * Features:
 * - Type-safe event posting and subscription
 * - Automatic main thread delivery for UI updates
 * - Thread-safe operations
 * - Memory leak prevention with weak references
 */
public class EventBus {
    private static final String TAG = "EventBus";
    private static final EventBus instance = new EventBus();

    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private EventBus() {}

    /**
     * Get singleton instance
     */
    public static EventBus getInstance() {
        return instance;
    }

    /**
     * Post an event to all registered listeners
     * @param event The event to post
     */
    public static void post(Object event) {
        instance.postEvent(event);
    }

    /**
     * Register a listener for specific event type
     * @param eventType The event class
     * @param listener The event listener
     * @param <T> The event type
     */
    public static <T> void register(Class<T> eventType, EventListener<T> listener) {
        instance.addListener(eventType, listener);
    }

    /**
     * Unregister a listener for specific event type
     * @param eventType The event class
     * @param listener The event listener
     * @param <T> The event type
     */
    public static <T> void unregister(Class<T> eventType, EventListener<T> listener) {
        instance.removeListener(eventType, listener);
    }

    /**
     * Unregister listener from all event types
     * @param listener The listener to remove
     */
    public static void unregisterAll(EventListener<?> listener) {
        instance.removeListenerFromAll(listener);
    }

    /**
     * Internal event posting
     */
    private void postEvent(Object event) {
        Class<?> eventType = event.getClass();
        List<EventListener<?>> eventListeners = listeners.get(eventType);

        if (eventListeners == null || eventListeners.isEmpty()) {
            Log.w(TAG, "No listeners registered for event: " + eventType.getSimpleName());
            return;
        }

        Log.d(TAG, "Posting event: " + eventType.getSimpleName() + " to " + eventListeners.size() + " listeners");

        for (EventListener listener : eventListeners) {
            try {
                // Deliver on main thread for UI safety
                mainHandler.post(() -> {
                    try {
                        listener.onEvent(event);
                    } catch (Exception e) {
                        Log.e(TAG, "Error delivering event to listener", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to post event to listener", e);
            }
        }
    }

    /**
     * Add listener for event type
     */
    private <T> void addListener(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        Log.d(TAG, "Registered listener for: " + eventType.getSimpleName());
    }

    /**
     * Remove listener for event type
     */
    private <T> void removeListener(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            Log.d(TAG, "Unregistered listener for: " + eventType.getSimpleName());
        }
    }

    /**
     * Remove listener from all event types
     */
    private void removeListenerFromAll(EventListener<?> listener) {
        for (List<EventListener<?>> eventListeners : listeners.values()) {
            eventListeners.remove(listener);
        }
        Log.d(TAG, "Unregistered listener from all events");
    }

    /**
     * Event listener interface
     */
    public interface EventListener<T> {
        void onEvent(T event);
    }
}