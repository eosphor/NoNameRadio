package com.nonameradio.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;

import java.util.Map;

/**
 * System utilities for device and context operations.
 * Contains system-related operations extracted from Utils.java.
 */
public class SystemUtils {
    private static final String TAG = "SystemUtils";

    /**
     * Check if running on TV
     */
    public static boolean isRunningOnTV(Context context) {
        try {
            android.app.UiModeManager uiModeManager =
                (android.app.UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
            return uiModeManager.getCurrentModeType() == android.content.res.Configuration.UI_MODE_TYPE_TELEVISION;
        } catch (Exception e) {
            Log.e(TAG, "Error checking TV mode", e);
            return false;
        }
    }

    /**
     * Check if permission is granted
     */
    public static boolean hasPermission(Context context, String permission) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            } else {
                return PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking permission: " + permission, e);
            return false;
        }
    }

    /**
     * Parse integer with default value
     */
    public static int parseIntWithDefault(String number, int defaultVal) {
        if (number == null || number.trim().isEmpty()) {
            return defaultVal;
        }

        try {
            return Integer.parseInt(number.trim());
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed to parse int: " + number + ", using default: " + defaultVal);
            return defaultVal;
        }
    }

    /**
     * Check WiFi connection using modern NetworkCapabilities API (minSdk 24+).
     */
    public static boolean hasWifiConnection(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;

            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        } catch (Exception e) {
            Log.e(TAG, "Error checking WiFi connection", e);
            return false;
        }
    }

    /**
     * Check any network connection using modern NetworkCapabilities API (minSdk 24+).
     */
    public static boolean hasAnyConnection(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;

            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Exception e) {
            Log.e(TAG, "Error checking network connection", e);
            return false;
        }
    }

    /**
     * Check if bottom navigation is enabled
     */
    public static boolean bottomNavigationEnabled(Context context) {
        try {
            // This would check shared preferences for bottom navigation setting
            // For now, return true for phones, false for TV
            return !isRunningOnTV(context);
        } catch (Exception e) {
            Log.e(TAG, "Error checking bottom navigation", e);
            return true;
        }
    }

    /**
     * Format string with named arguments
     */
    public static String formatStringWithNamedArgs(String format, Map<String, String> args) {
        if (format == null || args == null) {
            return format;
        }

        String result = format;
        for (Map.Entry<String, String> entry : args.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }

        return result;
    }

    /**
     * Get device manufacturer and model
     */
    public static String getDeviceInfo() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    /**
     * Get Android version
     */
    public static String getAndroidVersion() {
        return "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
    }

    /**
     * Check if device is emulator
     */
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic") ||
               Build.FINGERPRINT.startsWith("unknown") ||
               Build.MODEL.contains("google_sdk") ||
               Build.MODEL.contains("Emulator") ||
               Build.MODEL.contains("Android SDK built for x86") ||
               Build.MANUFACTURER.contains("Genymotion") ||
               (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
               "google_sdk".equals(Build.PRODUCT);
    }
}
