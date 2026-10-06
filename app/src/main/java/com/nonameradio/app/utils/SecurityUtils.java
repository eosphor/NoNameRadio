package com.nonameradio.app.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;

/**
 * Security utilities for secure network operations
 * Replaces insecure implementations in Utils.java
 */
public class SecurityUtils {
    private static final String TAG = "SecurityUtils";
    
    // Whitelist of allowed domains for external requests
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
        "radio-browser.info",
        "www.radio-browser.info",
        "de1.api.radio-browser.info",
        "fr1.api.radio-browser.info"
    );
    
    // Pattern for valid UUID format
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    
    /**
     * Create an OkHttpClient restricted to modern TLS.
     * Certificate chain and hostname validation are left to the platform defaults (system trust
     * store + standard hostname verification) - never replace them with custom implementations.
     * @param context Application context (currently unused, kept for future extensibility)
     * @return Configured OkHttpClient
     */
    @NonNull
    public static OkHttpClient createSecureHttpClient(@Nullable Context context) {
        return new OkHttpClient.Builder()
            .connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS))
            .build();
    }

    /**
     * Validate if a domain is in the allowed list
     */
    private static boolean isAllowedDomain(@NonNull String hostname) {
        // Remove port if present
        String domain = hostname.split(":")[0].toLowerCase(Locale.ROOT);

        // Explicit whitelist, plus any API mirror under radio-browser.info (de1, nl1, at1, ...)
        return ALLOWED_DOMAINS.contains(domain) || domain.endsWith(".radio-browser.info");
    }
    
    /**
     * Validate station UUID format to prevent SSRF attacks
     * @param stationUuid UUID to validate
     * @return true if valid UUID format
     */
    public static boolean isValidStationUuid(@Nullable String stationUuid) {
        if (stationUuid == null || stationUuid.isEmpty()) {
            return false;
        }
        
        return UUID_PATTERN.matcher(stationUuid).matches();
    }
    
    /**
     * Validate and sanitize URL to prevent SSRF attacks
     * @param url URL to validate
     * @return true if URL is safe
     */
    public static boolean isValidUrl(@Nullable String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        try {
            URL parsedUrl = new URL(url);
            String hostname = parsedUrl.getHost();
            
            if (hostname == null || hostname.isEmpty()) {
                return false;
            }
            
            // Check against allowed domains
            return isAllowedDomain(hostname);
            
        } catch (MalformedURLException e) {
            Log.w(TAG, "Invalid URL format: " + url, e);
            return false;
        }
    }
    
    /**
     * Sanitize input string to prevent injection attacks
     * @param input Input string to sanitize
     * @return Sanitized string
     */
    @NonNull
    public static String sanitizeInput(@Nullable String input) {
        if (input == null) {
            return "";
        }
        
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'&;]", "").trim();
    }
    
    /**
     * Validate station data before processing
     * @param station Station data to validate
     * @return true if station data is valid
     */
    public static boolean isValidStationData(@Nullable Object station) {
        if (station == null) {
            return false;
        }
        
        // Add more validation as needed
        return true;
    }
    
    /**
     * Create a secure hash using SHA-256 instead of MD5
     * @param input Input string to hash
     * @return SHA-256 hash
     */
    @Nullable
    public static String createSecureHash(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating secure hash", e);
            return null;
        }
    }
}
