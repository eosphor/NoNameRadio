package com.nonameradio.app.service;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.nonameradio.app.BuildConfig;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.core.architecture.IM3UService;
import com.nonameradio.app.core.architecture.Result;
import com.nonameradio.app.station.DataRadioStation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.OkHttpClient;

/**
 * Service for M3U playlist operations.
 * Handles saving and loading M3U files with proper Android storage APIs.
 */
public class M3UService implements IM3UService {
    private static final String TAG = "M3UService";
    private static final String M3U_PREFIX = "#RADIOBROWSERUUID:";

    private final Context context;

    public M3UService(Context context) {
        this.context = context;
    }

    @Override
    public CompletableFuture<Result<Void>> saveM3U(String filePath, String fileName, List<DataRadioStation> stations) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean result = saveM3UInternal(filePath, fileName, stations);
                return result ? Result.success(null) : Result.error(new Exception("Failed to save M3U file"));
            } catch (Exception e) {
                Log.e(TAG, "Failed to save M3U file", e);
                return Result.error(e);
            }
        });
    }

    @Override
    public CompletableFuture<Result<List<DataRadioStation>>> loadM3U(String filePath, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<DataRadioStation> stations = loadM3UInternal(filePath, fileName);
                return stations != null ? Result.success(stations) : Result.error(new Exception("Failed to load M3U file"));
            } catch (Exception e) {
                Log.e(TAG, "Failed to load M3U file", e);
                return Result.error(e);
            }
        });
    }

    @Override
    public CompletableFuture<Result<List<DataRadioStation>>> loadM3UFromReader(Reader reader, String displayName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<DataRadioStation> stations = loadM3UReader(reader);
                return stations != null ? Result.success(stations) : Result.error(new Exception("Failed to load M3U from reader"));
            } catch (Exception e) {
                Log.e(TAG, "Failed to load M3U from reader", e);
                return Result.error(e);
            }
        });
    }

    @Override
    public String getSaveDir() {
        return getSaveDirStatic();
    }

    public static String getSaveDirStatic() {
        // For tests or modern Android versions, use app-specific storage
        if (BuildConfig.DEBUG || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Return null in this case to let the caller handle selecting an appropriate directory
            return null;
        } else {
            // Legacy approach for older Android versions in production
            String path = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            File folder = new File(path);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    Log.e(TAG, "could not create dir:" + path);
                }
            }
            return path;
        }
    }

    private boolean saveM3UInternal(String filePath, String fileName, List<DataRadioStation> stations) {
        try {
            // For tests, save to app-specific directory
            if (BuildConfig.DEBUG) {
                File privateFile = new File(context.getExternalFilesDir(null), fileName);
                BufferedWriter bw = new BufferedWriter(new FileWriter(privateFile, false));
                boolean result = saveM3UWriter(bw, stations);
                Log.d(TAG, "Saved to app-specific directory: " + privateFile.getAbsolutePath());
                return result;
            }

            // For Android 10+ use MediaStore API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return saveM3UUsingMediaStore(fileName, stations);
            }

            // Legacy approach for older Android versions
            File f = new File(filePath, fileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
            boolean result = saveM3UWriter(bw, stations);
            MediaScannerConnection.scanFile(context, new String[]{f.getAbsolutePath()}, null, null);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "File write failed: " + e);
            return false;
        }
    }

    private boolean saveM3UUsingMediaStore(String fileName, List<DataRadioStation> stations) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "audio/x-mpegurl");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri contentUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            } else {
                // Fallback for older versions - this shouldn't be reached since we check Q earlier
                return false;
            }
            Uri fileUri = context.getContentResolver().insert(contentUri, values);

            if (fileUri != null) {
                try (OutputStream os = context.getContentResolver().openOutputStream(fileUri)) {
                    if (os != null) {
                        OutputStreamWriter writer = new OutputStreamWriter(os);
                        boolean result = saveM3UWriter(writer, stations);

                        // Mark the file as no longer pending
                        values.clear();
                        values.put(MediaStore.Downloads.IS_PENDING, 0);
                        context.getContentResolver().update(fileUri, values, null, null);

                        return result;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "MediaStore save failed: " + e);
            return false;
        }
    }

    private boolean saveM3UWriter(Writer bw, List<DataRadioStation> stations) {
        try {
            bw.write("#EXTM3U\n");
            for (DataRadioStation station : stations) {
                bw.write(M3U_PREFIX + station.StationUuid + "\n");
                bw.write("#EXTINF:-1," + station.Name + "\n");
                bw.write(station.StreamUrl + "\n\n");
            }
            bw.flush();
            bw.close();

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception during M3U write: " + e);
            return false;
        }
    }

    private List<DataRadioStation> loadM3UInternal(String filePath, String fileName) {
        try {
            // Try app-specific directory first for tests
            if (BuildConfig.DEBUG) {
                File privateFile = new File(context.getExternalFilesDir(null), fileName);
                if (privateFile.exists() && privateFile.canRead()) {
                    Log.d(TAG, "Loading from app-specific directory: " + privateFile.getAbsolutePath());
                    FileReader fr = new FileReader(privateFile);
                    return loadM3UReader(fr);
                } else {
                    // Copy test file to app-specific directory for tests if we're in debug mode
                    File externalFile = new File(filePath, fileName);
                    if (externalFile.exists()) {
                        try {
                            // Copy the file to our app-specific directory
                            java.io.FileInputStream inStream = new java.io.FileInputStream(externalFile);
                            java.io.FileOutputStream outStream = new java.io.FileOutputStream(privateFile);
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inStream.read(buffer)) > 0) {
                                outStream.write(buffer, 0, length);
                            }
                            inStream.close();
                            outStream.close();
                            Log.d(TAG, "Copied test file to app-specific directory: " + privateFile.getAbsolutePath());

                            // Now read from the copied file
                            FileReader fr = new FileReader(privateFile);
                            return loadM3UReader(fr);
                        } catch (Exception e) {
                            Log.w(TAG, "Failed to copy file: " + e.getMessage());
                        }
                    }
                }
            }

            File f = new File(filePath, fileName);

            // Check if file exists
            if (!f.exists()) {
                Log.e(TAG, "File does not exist: " + f.getAbsolutePath());
                return null;
            }

            // For Android 10+ ensure we have proper access
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    // Try to access through MediaStore for downloads on Android 10+
                    Uri contentUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
                    String selection = MediaStore.Downloads.DISPLAY_NAME + "=?";
                    String[] selectionArgs = new String[] { fileName };
                    Cursor cursor = context.getContentResolver().query(contentUri, null, selection, selectionArgs, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID);
                        long id = cursor.getLong(idColumn);
                        Uri fileUri = ContentUris.withAppendedId(contentUri, id);

                        InputStreamReader reader = new InputStreamReader(context.getContentResolver().openInputStream(fileUri));
                        cursor.close();
                        return loadM3UReader(reader);
                    }

                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "MediaStore approach failed: " + e + ", falling back to direct file access");
                }
            }

            // Try direct file access as last resort
            try {
                FileReader fr = new FileReader(f);
                return loadM3UReader(fr);
            } catch (Exception e) {
                Log.e(TAG, "Direct file access failed: " + e);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "File read failed: " + e);
            return null;
        }
    }

    private List<DataRadioStation> loadM3UReader(Reader reader) {
        try {
            String line;
            final NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();
            final OkHttpClient httpClient = app.getHttpClient();
            ArrayList<String> listUuids = new ArrayList<>();

            BufferedReader br = new BufferedReader(reader);
            while ((line = br.readLine()) != null) {
                Log.v(TAG, "line: "+line);
                if (line.startsWith(M3U_PREFIX)) {
                    try {
                        String uuid = line.substring(M3U_PREFIX.length()).trim();
                        listUuids.add(uuid);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
            br.close();

            List<DataRadioStation> listStationsNew = com.nonameradio.app.Utils.getStationsByUuid(httpClient, context, listUuids);

            // sort list to have the same order as the initial save file
            List<DataRadioStation> listStationsSorted = new ArrayList<>();
            for (String uuid: listUuids) {
                if (listStationsNew == null) {
                    Log.w(TAG, "Failed to load stations from server");
                    return new ArrayList<>();
                }
                for (DataRadioStation s: listStationsNew){
                    if (uuid.equals(s.StationUuid)){
                        listStationsSorted.add(s);
                        break;
                    }
                }
            }
            if (listStationsSorted.isEmpty()) {
                Log.w(TAG, "No stations loaded from M3U file");
                return listStationsNew;
            }
            return listStationsSorted;
        } catch (Exception e) {
            Log.e(TAG, "File read failed: " + e);
            return null;
        }
    }
}
