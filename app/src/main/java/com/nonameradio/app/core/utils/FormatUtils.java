package com.nonameradio.app.core.utils;

import android.content.Context;
import android.text.TextUtils;
import com.nonameradio.app.station.DataRadioStation;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class FormatUtils {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:/.*)?$"
    );

    public static String formatStationName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "Unknown Station";
        }
        return name.trim();
    }

    public static String formatStationUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        // Ensure URL has protocol
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        return url.trim();
    }

    public static String formatStationInfo(DataRadioStation station) {
        if (station == null) {
            return "";
        }

        StringBuilder info = new StringBuilder();

        if (!TextUtils.isEmpty(station.Country)) {
            info.append(station.Country);
        }

        if (!TextUtils.isEmpty(station.Language)) {
            if (info.length() > 0) info.append(" • ");
            info.append(station.Language);
        }

        if (station.Bitrate > 0) {
            if (info.length() > 0) info.append(" • ");
            info.append(station.Bitrate).append(" kbps");
        }

        return info.toString();
    }

    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String formatDuration(long milliseconds) {
        if (milliseconds < 0) return "00:00";

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        } else {
            return String.format("%02d:%02d", minutes, seconds % 60);
        }
    }

    public static String formatStationNameWithFallback(DataRadioStation station) {
        if (station == null) {
            return "Unknown Station";
        }

        String name = formatStationName(station.Name);
        if (!TextUtils.isEmpty(name) && !name.equals("Unknown Station")) {
            return name;
        }

        // Fallback to other fields
        if (!TextUtils.isEmpty(station.HomePageUrl)) {
            return extractDomainFromUrl(station.HomePageUrl);
        }

        return "Station " + (station.StationId != null ? station.StationId : "Unknown");
    }

    private static String extractDomainFromUrl(String url) {
        if (TextUtils.isEmpty(url)) return "";

        try {
            // Simple domain extraction
            int start = url.indexOf("://");
            if (start != -1) {
                start += 3;
            } else {
                start = 0;
            }

            int end = url.indexOf('/', start);
            if (end == -1) {
                end = url.length();
            }

            String domain = url.substring(start, end);
            // Remove www. prefix if present
            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }

            return domain;
        } catch (Exception e) {
            return url;
        }
    }

    public static String formatStringWithNamedArgs(String template, Map<String, String> args) {
        if (TextUtils.isEmpty(template) || args == null) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, String> entry : args.entrySet()) {
            String placeholder = "%" + entry.getKey() + "%";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }

        return result;
    }
}
