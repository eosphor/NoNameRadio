package com.nonameradio.app.core.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nonameradio.app.R;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized error handling utility
 * Provides consistent error handling across the application
 */
public class ErrorHandler {
    private static final String TAG = "ErrorHandler";
    
    // Error type mappings for user-friendly messages
    private static final Map<Class<? extends Exception>, Integer> ERROR_MESSAGE_MAP = new HashMap<>();
    
    static {
        ERROR_MESSAGE_MAP.put(ConnectException.class, R.string.error_network_connection);
        ERROR_MESSAGE_MAP.put(SocketTimeoutException.class, R.string.error_network_timeout);
        ERROR_MESSAGE_MAP.put(UnknownHostException.class, R.string.error_network_host);
        ERROR_MESSAGE_MAP.put(IOException.class, R.string.error_network_io);
        ERROR_MESSAGE_MAP.put(SecurityException.class, R.string.error_security);
        ERROR_MESSAGE_MAP.put(IllegalArgumentException.class, R.string.error_invalid_argument);
        ERROR_MESSAGE_MAP.put(IllegalStateException.class, R.string.error_invalid_state);
        ERROR_MESSAGE_MAP.put(NullPointerException.class, R.string.error_null_pointer);
    }
    
    /**
     * Handle and log an error with context information
     * @param tag Log tag
     * @param message Error message
     * @param throwable Exception or error
     * @param context Application context for user-friendly messages
     */
    public static void handleError(@NonNull String tag, @NonNull String message, 
                                 @Nullable Throwable throwable, @Nullable Context context) {
        
        // Log the error with full stack trace
        if (throwable != null) {
            Log.e(tag, message, throwable);
        } else {
            Log.e(tag, message);
        }
        
        // Send error to analytics/crash reporting if available
        reportErrorToAnalytics(message, throwable);
        
        // Show user-friendly error message if context is available
        if (context != null) {
            String userMessage = getUserFriendlyErrorMessage(throwable, context);
            // You can show toast, snackbar, or other UI feedback here
            Log.i(TAG, "User message: " + userMessage);
        }
    }
    
    /**
     * Handle error without context (for background operations)
     * @param tag Log tag
     * @param message Error message
     * @param throwable Exception or error
     */
    public static void handleError(@NonNull String tag, @NonNull String message, 
                                 @Nullable Throwable throwable) {
        handleError(tag, message, throwable, null);
    }
    
    /**
     * Get user-friendly error message based on exception type
     * @param throwable Exception
     * @param context Application context
     * @return User-friendly error message
     */
    @NonNull
    public static String getUserFriendlyErrorMessage(@Nullable Throwable throwable, 
                                                   @NonNull Context context) {
        if (throwable == null) {
            return context.getString(R.string.error_unknown);
        }
        
        // Check for specific error types
        for (Map.Entry<Class<? extends Exception>, Integer> entry : ERROR_MESSAGE_MAP.entrySet()) {
            if (entry.getKey().isInstance(throwable)) {
                try {
                    return context.getString(entry.getValue());
                } catch (Exception e) {
                    Log.w(TAG, "Error getting user-friendly message", e);
                    break;
                }
            }
        }

        // Default message for unknown errors
        if (context != null) {
            try {
                return context.getString(R.string.error_unknown);
            } catch (Exception e) {
                Log.w(TAG, "Error getting default error message", e);
            }
        }

        // Fallback when no context is available
        return "An unknown error occurred";
    }
    
    /**
     * Report error to analytics/crash reporting service
     * @param message Error message
     * @param throwable Exception
     */
    private static void reportErrorToAnalytics(@NonNull String message, @Nullable Throwable throwable) {
        try {
            // Here you would integrate with your analytics service
            // For example: Firebase Crashlytics, AppMetrica, etc.
            
            // Example for AppMetrica (if available):
            // AppMetrica.reportError(message, throwable);
            
            // Example for Firebase Crashlytics:
            // FirebaseCrashlytics.getInstance().recordException(throwable);
            
            Log.d(TAG, "Error reported to analytics: " + message);
            
        } catch (Exception e) {
            Log.w(TAG, "Failed to report error to analytics", e);
        }
    }
    
    /**
     * Validate input parameters and throw appropriate exceptions
     * @param value Value to validate
     * @param parameterName Name of the parameter for error messages
     * @throws IllegalArgumentException if value is null or invalid
     */
    public static void validateNotNull(@Nullable Object value, @NonNull String parameterName) {
        if (value == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }
    
    /**
     * Validate string parameter and throw appropriate exceptions
     * @param value String value to validate
     * @param parameterName Name of the parameter for error messages
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void validateNotEmpty(@Nullable String value, @NonNull String parameterName) {
        validateNotNull(value, parameterName);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(parameterName + " cannot be empty");
        }
    }
    
    /**
     * Safely execute a runnable with error handling
     * @param runnable Runnable to execute
     * @param errorMessage Error message for logging
     * @param context Context for user feedback
     */
    public static void safeExecute(@NonNull Runnable runnable, @NonNull String errorMessage, 
                                 @Nullable Context context) {
        try {
            runnable.run();
        } catch (Exception e) {
            handleError(TAG, errorMessage, e, context);
        }
    }
    
    /**
     * Result wrapper for operations that can fail
     * @param <T> Success result type
     */
    public static class Result<T> {
        private final T data;
        private final Throwable error;
        private final boolean success;
        
        private Result(T data, Throwable error, boolean success) {
            this.data = data;
            this.error = error;
            this.success = success;
        }
        
        public static <T> Result<T> success(T data) {
            return new Result<>(data, null, true);
        }
        
        public static <T> Result<T> error(Throwable error) {
            return new Result<>(null, error, false);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public boolean isError() {
            return !success;
        }
        
        @Nullable
        public T getData() {
            return data;
        }
        
        @Nullable
        public Throwable getError() {
            return error;
        }
        
        @Nullable
        public T getDataOrThrow() throws Exception {
            if (success) {
                return data;
            } else {
                throw new RuntimeException("Operation failed", error);
            }
        }
    }
}
