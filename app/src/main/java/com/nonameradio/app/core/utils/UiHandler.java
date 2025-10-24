package com.nonameradio.app.core.utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

/**
 * Centralized UI thread handler for post and postDelayed operations.
 * Replaces multiple new Handler() instantiations throughout the app.
 *
 * @author NoNameRadio Team
 * @version 1.0.0
 * @since 0.86.903
 */
public class UiHandler {
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    /**
     * Post runnable to main thread
     */
    public static void post(@NonNull Runnable runnable) {
        MAIN_HANDLER.post(runnable);
    }

    /**
     * Post runnable to main thread with delay
     */
    public static void postDelayed(@NonNull Runnable runnable, long delayMillis) {
        MAIN_HANDLER.postDelayed(runnable, delayMillis);
    }

    /**
     * Remove callbacks from main thread
     */
    public static void removeCallbacks(@NonNull Runnable runnable) {
        MAIN_HANDLER.removeCallbacks(runnable);
    }

    /**
     * Remove all callbacks and messages
     */
    public static void removeCallbacksAndMessages(Object token) {
        MAIN_HANDLER.removeCallbacksAndMessages(token);
    }

    /**
     * Check if current thread is main thread
     */
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * Run on UI thread if not already on it
     */
    public static void runOnUiThread(@NonNull Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            post(runnable);
        }
    }

    /**
     * Post runnable at front of queue
     */
    public static void postAtFrontOfQueue(@NonNull Runnable runnable) {
        MAIN_HANDLER.postAtFrontOfQueue(runnable);
    }

    /**
     * Get main looper
     */
    public static Looper getMainLooper() {
        return Looper.getMainLooper();
    }

    /**
     * Check if handler has pending messages
     */
    public static boolean hasMessages(int what) {
        return MAIN_HANDLER.hasMessages(what);
    }

    /**
     * Send empty message
     */
    public static boolean sendEmptyMessage(int what) {
        return MAIN_HANDLER.sendEmptyMessage(what);
    }

    /**
     * Send empty message delayed
     */
    public static boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        return MAIN_HANDLER.sendEmptyMessageDelayed(what, delayMillis);
    }
}
