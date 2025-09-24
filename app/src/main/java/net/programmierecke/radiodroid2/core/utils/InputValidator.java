package net.programmierecke.radiodroid2.core.utils;

import android.text.TextUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Input validation utilities for user data and API inputs.
 * Provides centralized validation logic to prevent common input errors.
 *
 * @author NoNameRadio Team
 * @version 1.0.0
 * @since 0.86.903
 */
public class InputValidator {

    private static final Pattern URL_PATTERN =
        Pattern.compile("^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:/.*)?$");

    private static final int MAX_STATION_NAME_LENGTH = 200;
    private static final int MAX_COUNTRY_LENGTH = 100;
    private static final int MAX_LANGUAGE_LENGTH = 50;
    private static final int MAX_TAG_LENGTH = 50;

    /**
     * Validate radio station name
     */
    public static boolean isValidStationName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        String trimmed = name.trim();
        return !trimmed.isEmpty() &&
               trimmed.length() <= MAX_STATION_NAME_LENGTH &&
               !containsInvalidCharacters(trimmed);
    }

    /**
     * Validate URL format
     */
    public static boolean isValidUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        String trimmed = url.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        // Add protocol if missing
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://" + trimmed;
        }

        try {
            new URL(trimmed);
            return URL_PATTERN.matcher(trimmed).matches();
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Validate stream URL (supports various streaming protocols)
     */
    public static boolean isValidStreamUrl(String url) {
        if (!isValidUrl(url)) {
            return false;
        }

        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".m3u") ||
               lowerUrl.endsWith(".pls") ||
               lowerUrl.endsWith(".m3u8") ||
               lowerUrl.contains(".m3u") ||
               lowerUrl.contains(".pls") ||
               isValidStreamingUrl(url);
    }

    /**
     * Validate country name
     */
    public static boolean isValidCountry(String country) {
        if (TextUtils.isEmpty(country)) {
            return true; // Country is optional
        }

        String trimmed = country.trim();
        return trimmed.length() <= MAX_COUNTRY_LENGTH &&
               !containsInvalidCharacters(trimmed);
    }

    /**
     * Validate language code
     */
    public static boolean isValidLanguage(String language) {
        if (TextUtils.isEmpty(language)) {
            return true; // Language is optional
        }

        String trimmed = language.trim();
        return trimmed.length() <= MAX_LANGUAGE_LENGTH &&
               !containsInvalidCharacters(trimmed);
    }

    /**
     * Validate bitrate value
     */
    public static boolean isValidBitrate(int bitrate) {
        return bitrate >= 0 && bitrate <= 999999; // Reasonable bitrate range
    }

    /**
     * Validate tag
     */
    public static boolean isValidTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return true; // Tags are optional
        }

        String trimmed = tag.trim();
        return trimmed.length() <= MAX_TAG_LENGTH &&
               !containsInvalidCharacters(trimmed);
    }

    /**
     * Validate search query
     */
    public static boolean isValidSearchQuery(String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }

        String trimmed = query.trim();
        return trimmed.length() >= 1 &&
               trimmed.length() <= 100 &&
               !containsOnlySpecialCharacters(trimmed);
    }

    /**
     * Sanitize station name (remove dangerous characters)
     */
    public static String sanitizeStationName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }

        return name.trim()
                  .replaceAll("[<>\"'&]", "")
                  .substring(0, Math.min(name.length(), MAX_STATION_NAME_LENGTH));
    }

    /**
     * Sanitize URL
     */
    public static String sanitizeUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        String trimmed = url.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://" + trimmed;
        }

        return trimmed;
    }

    /**
     * Check if URL is a valid streaming URL
     */
    private static boolean isValidStreamingUrl(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.contains(".mp3") ||
               lowerUrl.contains(".aac") ||
               lowerUrl.contains(".ogg") ||
               lowerUrl.contains(".flac") ||
               lowerUrl.contains(".m4a") ||
               lowerUrl.contains(".wma") ||
               lowerUrl.contains(".wav") ||
               lowerUrl.contains("/stream") ||
               lowerUrl.contains("/listen");
    }

    /**
     * Check for invalid characters
     */
    private static boolean containsInvalidCharacters(String input) {
        return input.contains("<") ||
               input.contains(">") ||
               input.contains("\"") ||
               input.contains("'") ||
               input.contains("&") ||
               input.contains("\n") ||
               input.contains("\r") ||
               input.contains("\t");
    }

    /**
     * Check if string contains only special characters
     */
    private static boolean containsOnlySpecialCharacters(String input) {
        return input.matches("[^a-zA-Z0-9]+");
    }

    /**
     * Validate email format (for future use)
     */
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                           "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}
