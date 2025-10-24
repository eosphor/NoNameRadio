package com.nonameradio.app.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * File utilities for cache operations and file management.
 * Contains all file-related operations extracted from Utils.java.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Get cached file path for URI
     */
    public static String getCacheFile(Context ctx, String theURI) {
        try {
            if (theURI == null || theURI.isEmpty()) {
                return null;
            }

            // Create MD5 hash of URI for filename
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(theURI.getBytes());
            byte[] digest = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String filename = sb.toString();

            File cacheDir = ctx.getCacheDir();
            File cacheFile = new File(cacheDir, filename);

            // Check if cache file exists and is readable
            if (cacheFile.exists() && cacheFile.canRead()) {
                return cacheFile.getAbsolutePath();
            }

            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache file for URI: " + theURI, e);
            return null;
        }
    }

    /**
     * Write content to cache file
     */
    public static void writeFileCache(Context ctx, String theURI, String content) {
        if (theURI == null || theURI.isEmpty() || content == null) {
            return;
        }

        try {
            // Create MD5 hash of URI for filename
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(theURI.getBytes());
            byte[] digest = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String filename = sb.toString();

            File cacheDir = ctx.getCacheDir();
            File cacheFile = new File(cacheDir, filename);

            // Write content to file
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(content.getBytes());
                fos.flush();
                Log.d(TAG, "Cached content for URI: " + theURI);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error writing cache file for URI: " + theURI, e);
        }
    }

    /**
     * Read cached content
     */
    public static String readFileCache(Context ctx, String theURI) {
        String cachePath = getCacheFile(ctx, theURI);
        if (cachePath == null) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(cachePath)) {
            byte[] data = new byte[fis.available()];
            fis.read(data);
            return new String(data);
        } catch (IOException e) {
            Log.e(TAG, "Error reading cache file", e);
            return null;
        }
    }

    /**
     * Clear all cache files
     */
    public static void clearCache(Context ctx) {
        try {
            File cacheDir = ctx.getCacheDir();
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            boolean deleted = file.delete();
                            if (!deleted) {
                                Log.w(TAG, "Failed to delete cache file: " + file.getName());
                            }
                        }
                    }
                }
                Log.d(TAG, "Cache cleared");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing cache", e);
        }
    }

    /**
     * Get cache size in bytes
     */
    public static long getCacheSize(Context ctx) {
        try {
            File cacheDir = ctx.getCacheDir();
            if (!cacheDir.exists()) {
                return 0;
            }

            long size = 0;
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    }
                }
            }
            return size;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating cache size", e);
            return 0;
        }
    }
}
