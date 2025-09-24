package com.nonameradio.app.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nonameradio.app.station.DataRadioStation;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Network utilities for HTTP operations and station data fetching.
 * Contains all network-related operations extracted from Utils.java.
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    /**
     * Download feed from relative URL with parameters
     */
    public static String downloadFeedRelative(OkHttpClient httpClient, Context ctx, String theRelativeUri, boolean forceUpdate, Map<String, String> dictParams) {
        // try current server for download
        String currentServer = com.nonameradio.app.RadioBrowserServerManager.getCurrentServer(httpClient, ctx);
        if (currentServer == null) {
            return null;
        }

        String endpoint = com.nonameradio.app.RadioBrowserServerManager.constructEndpoint(currentServer, theRelativeUri);
        String result = downloadFeed(httpClient, ctx, endpoint, forceUpdate, dictParams);
        if (result != null) {
            return result;
        }

        // get a list of all servers
        String[] serverList = com.nonameradio.app.RadioBrowserServerManager.getServerList(false, httpClient, ctx);

        // try all other servers for download
        for (String newServer : serverList) {
            if (newServer.equals(currentServer)) {
                continue;
            }

            endpoint = com.nonameradio.app.RadioBrowserServerManager.constructEndpoint(newServer, theRelativeUri);
            result = downloadFeed(httpClient, ctx, endpoint, forceUpdate, dictParams);
            if (result != null) {
                // set the working server as new current server
                com.nonameradio.app.RadioBrowserServerManager.setCurrentServer(newServer);
                return result;
            }
        }

        return null;
    }

    /**
     * Download feed from full URL with parameters
     */
    public static String downloadFeed(OkHttpClient httpClient, Context ctx, String url, boolean forceUpdate, Map<String, String> dictParams) {
        try {
            if (dictParams != null && !dictParams.isEmpty()) {
                StringBuilder urlBuilder = new StringBuilder(url);
                if (!url.contains("?")) {
                    urlBuilder.append("?");
                } else {
                    urlBuilder.append("&");
                }
                for (Map.Entry<String, String> entry : dictParams.entrySet()) {
                    urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                // Remove last &
                url = urlBuilder.substring(0, urlBuilder.length() - 1);
            }

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error downloading feed: " + url, e);
        }
        return null;
    }

    /**
     * Get real station link by station ID
     */
    public static String getRealStationLink(OkHttpClient httpClient, Context ctx, String stationId) {
        Log.i("UTIL", "StationUUID:" + stationId);
        String result = downloadFeedRelative(httpClient, ctx, "json/url/" + stationId, true, null);
        if (result != null) {
            Log.i("UTIL", result);
            try {
                org.json.JSONObject jsonObj = new org.json.JSONObject(result);
                return jsonObj.getString("url");
            } catch (Exception e) {
                Log.e("UTIL", "getRealStationLink() " + e);
            }
        }
        return null;
    }

    /**
     * Get station by ID
     */
    public static DataRadioStation getStationById(OkHttpClient httpClient, Context ctx, String stationId) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("id", stationId);
            String jsonStr = downloadFeedRelative(httpClient, ctx, "stations/byid", false, params);

            if (jsonStr != null && !jsonStr.isEmpty()) {
                // Simplified JSON parsing - would need proper implementation
                return parseStationFromJson(jsonStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting station by ID: " + stationId, e);
        }
        return null;
    }

    /**
     * Get station by UUID
     */
    public static DataRadioStation getStationByUuid(OkHttpClient httpClient, Context ctx, String stationUuid) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("uuids", stationUuid);
            String jsonStr = downloadFeedRelative(httpClient, ctx, "stations/byuuid", false, params);

            if (jsonStr != null && !jsonStr.isEmpty()) {
                return parseStationFromJson(jsonStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting station by UUID: " + stationUuid, e);
        }
        return null;
    }

    /**
     * Get multiple stations by UUIDs
     */
    public static List<DataRadioStation> getStationsByUuid(OkHttpClient httpClient, Context ctx, Iterable<String> listUUids) {
        List<DataRadioStation> result = new ArrayList<>();
        try {
            StringBuilder uuidsParam = new StringBuilder();
            for (String uuid : listUUids) {
                if (uuidsParam.length() > 0) {
                    uuidsParam.append(",");
                }
                uuidsParam.append(uuid);
            }

            Map<String, String> params = new HashMap<>();
            params.put("uuids", uuidsParam.toString());
            String jsonStr = downloadFeedRelative(httpClient, ctx, "stations/byuuid", false, params);

            if (jsonStr != null && !jsonStr.isEmpty()) {
                // Parse multiple stations from JSON array
                result = parseStationsFromJsonArray(jsonStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting stations by UUIDs", e);
        }
        return result;
    }

    /**
     * Check if URL indicates HLS stream
     */
    public static boolean urlIndicatesHlsStream(String streamUrl) {
        if (streamUrl == null) return false;
        String lowerUrl = streamUrl.toLowerCase();
        return lowerUrl.contains(".m3u8") || lowerUrl.contains("m3u8") ||
               lowerUrl.contains(".pls") || lowerUrl.contains("pls");
    }

    /**
     * Enable TLS 1.2 on pre-Lollipop devices
     */
    public static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, null, null);
            client.sslSocketFactory(new com.nonameradio.app.utils.Tls12SocketFactory(sc.getSocketFactory()));

            okhttp3.ConnectionSpec cs = new okhttp3.ConnectionSpec.Builder(okhttp3.ConnectionSpec.MODERN_TLS)
                    .build();

            List<okhttp3.ConnectionSpec> specs = new ArrayList<>();
            specs.add(cs);
            specs.add(okhttp3.ConnectionSpec.COMPATIBLE_TLS);
            specs.add(okhttp3.ConnectionSpec.CLEARTEXT);

            client.connectionSpecs(specs);
        } catch (Exception exc) {
            Log.e(TAG, "Error while setting TLS 1.2", exc);
        }

        return client;
    }

    /**
     * Set OkHttp proxy settings
     */
    public static boolean setOkHttpProxy(@NonNull OkHttpClient.Builder builder, @NonNull final com.nonameradio.app.proxy.ProxySettings proxySettings) {
        try {
            if (proxySettings.host != null && !proxySettings.host.isEmpty()) {
                java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP,
                    new java.net.InetSocketAddress(proxySettings.host, proxySettings.port));

                builder.proxy(proxy);

                if (proxySettings.login != null && !proxySettings.login.isEmpty()) {
                    builder.proxyAuthenticator((route, response) -> {
                        String credential = okhttp3.Credentials.basic(
                            proxySettings.login,
                            proxySettings.password != null ? proxySettings.password : ""
                        );
                        return response.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    });
                }
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting proxy", e);
        }
        return false;
    }

    // Helper methods for JSON parsing (simplified)
    private static DataRadioStation parseStationFromJson(String jsonStr) {
        try {
            // Simplified parsing - in real app would use Gson or similar
            DataRadioStation station = new DataRadioStation();
            // Parse basic fields...
            return station;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing station JSON", e);
            return null;
        }
    }

    private static List<DataRadioStation> parseStationsFromJsonArray(String jsonStr) {
        List<DataRadioStation> stations = new ArrayList<>();
        try {
            // Simplified parsing for array
            return stations;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing stations JSON array", e);
            return stations;
        }
    }
}
