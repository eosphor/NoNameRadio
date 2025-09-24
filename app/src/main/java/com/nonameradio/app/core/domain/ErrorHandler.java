package com.nonameradio.app.core.domain;

import android.content.Context;
import android.util.Log;
import com.nonameradio.app.core.utils.UiUtils;

public class ErrorHandler {
    private static final String TAG = "ErrorHandler";
    private final Context context;

    public ErrorHandler(Context context) {
        this.context = context;
    }

    public void handleNetworkError(Exception e) {
        Log.e(TAG, "Network error occurred", e);
        UiUtils.showToast(context, "Network connection error");
    }

    public void handlePlaybackError(Exception e) {
        Log.e(TAG, "Playback error occurred", e);
        UiUtils.showToast(context, "Unable to play station");
    }

    public void handleRecordingError(Exception e) {
        Log.e(TAG, "Recording error occurred", e);
        UiUtils.showToast(context, "Recording failed");
    }

    public void handleStorageError(Exception e) {
        Log.e(TAG, "Storage error occurred", e);
        UiUtils.showToast(context, "Storage access error");
    }

    public void handleGenericError(Exception e) {
        Log.e(TAG, "Unexpected error occurred", e);
        UiUtils.showToast(context, "An unexpected error occurred");
    }

    public void handlePermissionDenied(String permission) {
        Log.w(TAG, "Permission denied: " + permission);
        UiUtils.showToast(context, "Permission required: " + permission);
    }

    public void handleApiError(int code, String message) {
        Log.e(TAG, "API error: " + code + " - " + message);
        UiUtils.showToast(context, "Service temporarily unavailable");
    }
}

