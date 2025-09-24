package com.nonameradio.app.recording;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.nonameradio.app.BuildConfig;
import com.nonameradio.app.R;
import com.nonameradio.app.Utils;
import com.nonameradio.app.database.NoNameRadioDatabase;
import com.nonameradio.app.service.MediaSessionUtil;
import com.nonameradio.app.station.DataRadioStation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;

/* TODO: Actually have info about recording by storing them in the database and matching with files on disk.
 */
public class RecordingsManager {
    private final static String TAG = "Recordings";
    private final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final DateFormat timeFormatter = new SimpleDateFormat("HH-mm", Locale.US);
    private final Context context;
    private final NoNameRadioDatabase database;

    // Minimum disk space required for recording (100 MB)
    private static final long MIN_DISK_SPACE_MB = 100;

    private class RecordingsObservable extends Observable {
        @Override
        public synchronized boolean hasChanged() {
            return true;
        }
    }

    private final Observable savedRecordingsObservable = new RecordingsObservable();

    private class RunningRecordableListener implements RecordableListener {
        private final RunningRecordingInfo runningRecordingInfo;
        private boolean ended;

        private RunningRecordableListener(@NonNull RunningRecordingInfo runningRecordingInfo) {
            this.runningRecordingInfo = runningRecordingInfo;
        }

        @Override
        public void onBytesAvailable(byte[] buffer, int offset, int length) {
            try {
                runningRecordingInfo.getOutputStream().write(buffer, offset, length);
                runningRecordingInfo.setBytesWritten(runningRecordingInfo.getBytesWritten() + length);
                runningRecordingInfo.updateLinkedRecordingSize();
            } catch (IOException e) {
                Log.e(TAG, "Failed to write bytes", e);
                runningRecordingInfo.getRecordable().stopRecording();
            }
        }

        @Override
        public void onRecordingEnded() {
            if (ended) {
                return;
            }

            ended = true;

            try {
                runningRecordingInfo.getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            RecordingsManager.this.stopRecording(runningRecordingInfo.getRecordable());
            runningRecordingInfo.updateLinkedRecordingFinished();
        }
    }

    private final Map<Recordable, RunningRecordingInfo> runningRecordings = new HashMap<>();
    private final ArrayList<DataRecording> savedRecordings = new ArrayList<>();
    
    public RecordingsManager(Context context) {
        this.context = context;
        this.database = NoNameRadioDatabase.getDatabase(context);
    }

    public void record(@NonNull Context context, @NonNull Recordable recordable) {
        if (!recordable.canRecord()) {
            return;
        }

        if (!runningRecordings.containsKey(recordable)) {
            RunningRecordingInfo info = new RunningRecordingInfo();

            info.setRecordable(recordable);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

            final String fileNameFormat = prefs.getString("record_name_formatting", context.getString(R.string.settings_record_name_formatting_default));

            final Map<String, String> formattingArgs = new HashMap<>(recordable.getRecordNameFormattingArgs());

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            final Date currentTime = calendar.getTime();

            String dateStr = dateFormatter.format(currentTime);
            String timeStr = timeFormatter.format(currentTime);

            formattingArgs.put("date", dateStr);
            formattingArgs.put("time", timeStr);

            final int recordNum = prefs.getInt("record_num", 1);
            formattingArgs.put("index", Integer.toString(recordNum));

            final String recordTitle = Utils.formatStringWithNamedArgs(fileNameFormat, formattingArgs);

            info.setTitle(recordTitle);
            info.setFileName(String.format("%s.%s", recordTitle, recordable.getExtension()));

            // Create and save recording metadata
            RecordingMetadata metadata = createRecordingMetadata(info, recordable);
            saveRecordingMetadata(metadata);
            info.setMetadata(metadata);

            // Check available disk space before recording (minimum 100MB)
            if (!hasEnoughDiskSpace(context, MIN_DISK_SPACE_MB)) {
                Log.w(TAG, "Not enough disk space for recording: " + info.getFileName());
                showDiskSpaceWarning(context);
                return;
            }

            OutputStream outputStream;
            try {
                outputStream = createOutputStream(info, recordable.getExtension());
                info.setOutputStream(outputStream);
            } catch (IOException e) {
                Log.e(TAG, "Failed to create output stream for recording: " + info.getFileName(), e);
                return;
            }

            recordable.startRecording(new RunningRecordableListener(info));

            runningRecordings.put(recordable, info);
            // Don't add to savedRecordings yet - will be added after finalization

            prefs.edit().putInt("record_num", recordNum + 1).apply();
        }
    }

    private DataRecording toDataRecording(RunningRecordingInfo info) {
        DataRecording dr = new DataRecording();
        dr.Name = info.getFileName();
        dr.Time = new Date();
        dr.ContentUri = info.getMediaStoreUri();
        if (dr.ContentUri == null && dr.Name != null) {
            dr.AbsolutePath = getRecordDir() + "/" + dr.Name;
        }
        dr.InProgress = true;
        info.setLinkedDataRecording(dr);
        return dr;
    }

    public void stopRecording(@NonNull Recordable recordable) {
        recordable.stopRecording();

        RunningRecordingInfo info = runningRecordings.remove(recordable);
        
        // Finalize MediaStore entry if using modern storage API
        if (info != null && info.getMediaStoreUri() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            finalizeMediaStoreEntry(info);
        } else {
            // For legacy storage or if MediaStore finalization failed, update list immediately
            updateRecordingsList();
        }
    }

    public RunningRecordingInfo getRecordingInfo(Recordable recordable) {
        return runningRecordings.get(recordable);
    }

    public Map<Recordable, RunningRecordingInfo> getRunningRecordings() {
        return Collections.unmodifiableMap(runningRecordings);
    }

    public List<DataRecording> getSavedRecordings() {
        return new ArrayList<>(savedRecordings);
    }

    public void deleteRecording(@NonNull DataRecording recording) {
        boolean deleted = false;

        try {
            if (recording.ContentUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                deleted = resolver.delete(recording.ContentUri, null, null) > 0;
            }

            if (!deleted) {
                String absolutePath = recording.AbsolutePath;
                if (absolutePath == null) {
                    absolutePath = getRecordDir() + "/" + recording.Name;
                }

                if (absolutePath != null) {
                    File file = new File(absolutePath);
                    if (file.exists()) {
                        deleted = file.delete();
                    }
                }
            }
        } catch (SecurityException se) {
            Log.e(TAG, "Failed to delete recording", se);
        }

        if (!deleted) {
            Log.w(TAG, "Could not delete recording: " + recording.Name);
        } else {
            // Delete metadata from database
            deleteRecordingMetadata(recording.Name);
        }

        updateRecordingsList();
    }

    public Observable getSavedRecordingsObservable() {
        return savedRecordingsObservable;
    }

    /**
     * Creates an OutputStream for recording files.
     * Uses MediaStore API for Android 10+ and legacy file API for older versions.
     */
    private OutputStream createOutputStream(RunningRecordingInfo info, String extension) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore API for Android 10+
            return createOutputStreamWithMediaStore(info, extension);
        } else {
            // Use legacy file API for older Android versions
            return createOutputStreamLegacy(info.getFileName());
        }
    }

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.Q)
    private OutputStream createOutputStreamWithMediaStore(RunningRecordingInfo info, String extension) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, info.getFileName());
        contentValues.put(MediaStore.Audio.Media.MIME_TYPE, getMimeTypeForExtension(extension));
        contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/Recordings");
        contentValues.put(MediaStore.Audio.Media.IS_PENDING, 1); // Mark as pending during recording
        
        Uri uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (uri == null) {
            throw new IOException("Failed to create MediaStore entry for recording");
        }
        
        // Store the URI so we can finalize it later
        info.setMediaStoreUri(uri);
        
        return resolver.openOutputStream(uri);
    }
    
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.Q)
    private void finalizeMediaStoreEntry(RunningRecordingInfo info) {
        try {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0); // Mark as completed

            int updated = resolver.update(info.getMediaStoreUri(), contentValues, null, null);
            if (updated > 0) {
                Log.d(TAG, "Successfully finalized MediaStore entry for: " + info.getFileName());

                // Mark recording as finished and add to saved recordings
                info.updateLinkedRecordingFinished();
                DataRecording finishedRecording = toDataRecording(info);
                finishedRecording.InProgress = false; // Ensure it's marked as finished
                savedRecordings.add(0, finishedRecording);
                savedRecordingsObservable.notifyObservers();

                // Update recording metadata with final information
                updateRecordingMetadataOnCompletion(info);
            } else {
                Log.w(TAG, "Failed to finalize MediaStore entry for: " + info.getFileName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finalizing MediaStore entry for: " + info.getFileName(), e);
        }
    }
    
    private OutputStream createOutputStreamLegacy(String fileName) throws IOException {
        String pathRecordings = getRecordDirLegacy();
        String filePath = pathRecordings + "/" + fileName;
        return new FileOutputStream(filePath);
    }
    
    private String getMimeTypeForExtension(String extension) {
        switch (extension.toLowerCase(Locale.ROOT)) {
            case "mp3":
                return "audio/mpeg";
            case "aac":
                return "audio/aac";
            case "m4a":
                return "audio/mp4";
            case "wav":
                return "audio/wav";
            default:
                return "audio/mpeg"; // Default to mp3
        }
    }

    public static String getRecordDir() {
        return getRecordDirLegacy();
    }
    
    private static String getRecordDirLegacy() {
        String pathRecordings = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/Recordings";
        File folder = new File(pathRecordings);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.e(TAG, "could not create dir:" + pathRecordings);
            }
        }
        return pathRecordings;
    }

    public void updateRecordingsList() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Updating recordings list");
        }

        savedRecordings.clear();

        // Track which files were found in file system
        Set<String> foundFiles = new HashSet<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore API for Android 10+
            updateRecordingsListFromMediaStore();
            foundFiles.addAll(getFoundFilesFromMediaStore());
        } else {
            // Use legacy file system for older Android versions
            updateRecordingsListFromFiles();
            foundFiles.addAll(getFoundFilesFromFileSystem());
        }

        // Synchronize with database metadata
        synchronizeWithDatabaseMetadata(foundFiles);

        Collections.sort(savedRecordings, (o1, o2) -> Long.compare(o2.Time.getTime(), o1.Time.getTime()));
        savedRecordingsObservable.notifyObservers();
    }
    
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.Q)
    private void updateRecordingsListFromMediaStore() {
        try {
            ContentResolver resolver = context.getContentResolver();
            
            String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.RELATIVE_PATH,
                MediaStore.Audio.Media.DATA
            };
            
            String selection = MediaStore.Audio.Media.RELATIVE_PATH + " LIKE ?";
            String[] selectionArgs = {"%" + Environment.DIRECTORY_MUSIC + "/Recordings%"};
            
            String sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
            
            try (android.database.Cursor cursor = resolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder)) {
                
                if (cursor != null) {
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                    int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);
                    int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                    while (cursor.moveToNext()) {
                        String fileName = cursor.getString(nameColumn);

                        // Try to get metadata from database first
                        RecordingMetadata metadata = getRecordingMetadata(fileName);

                        DataRecording dr = new DataRecording();

                        if (metadata != null && metadata.completed) {
                            // Use metadata for enhanced recording info
                            dr.Name = fileName;
                            dr.Time = metadata.getStartDate();
                            dr.SizeBytes = metadata.fileSizeBytes;
                            dr.ContentUri = metadata.contentUri != null ? Uri.parse(metadata.contentUri) : null;
                        } else {
                            // Fallback to MediaStore data
                            long dateModified = cursor.getLong(dateColumn) * 1000; // Convert to milliseconds
                            dr.Name = fileName;
                            dr.Time = new Date(dateModified);
                            dr.SizeBytes = cursor.getLong(sizeColumn);
                            dr.ContentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    cursor.getLong(idColumn));
                        }

                        if (dataColumn >= 0) {
                            String absolutePath = cursor.getString(dataColumn);
                            if (absolutePath != null && !absolutePath.isEmpty()) {
                                dr.AbsolutePath = absolutePath;
                            }
                        }

                        dr.InProgress = false; // All recordings from MediaStore are completed
                        savedRecordings.add(dr);

                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Found MediaStore recording: " + fileName +
                                (metadata != null ? " (with metadata)" : " (no metadata)"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying MediaStore for recordings", e);
            // Fallback to file system method
            updateRecordingsListFromFiles();
        }
    }
    
    private void updateRecordingsListFromFiles() {
        String path = getRecordDir();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Updating recordings from " + path);
        }

        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                String fileName = f.getName();

                // Try to get metadata from database first
                RecordingMetadata metadata = getRecordingMetadata(fileName);

                DataRecording dr = new DataRecording();

                if (metadata != null && metadata.completed) {
                    // Use metadata for enhanced recording info
                    dr.Name = fileName;
                    dr.Time = metadata.getStartDate();
                    dr.SizeBytes = metadata.fileSizeBytes;
                    dr.AbsolutePath = f.getAbsolutePath();
                } else {
                    // Fallback to file system data
                    dr.Name = fileName;
                    dr.Time = new Date(f.lastModified());
                    dr.SizeBytes = f.length();
                    dr.AbsolutePath = f.getAbsolutePath();
                }

                dr.InProgress = false; // All recordings from file system are completed
                savedRecordings.add(dr);

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Found file system recording: " + fileName +
                        (metadata != null ? " (with metadata)" : " (no metadata)"));
                }
            }
        } else {
            Log.e(TAG, "Could not enumerate files in recordings directory");
        }
    }
    
    /**
     * Checks if the app has permission to record audio to storage.
     * @return true if recording is allowed, false otherwise
     */
    public boolean hasRecordingPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires READ_MEDIA_AUDIO permission
            return Utils.hasPermission(context, android.Manifest.permission.READ_MEDIA_AUDIO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 can use MediaStore without explicit permissions for app-created content
            return true;
        } else {
            // Android 9 and below require WRITE_EXTERNAL_STORAGE
            return Utils.hasPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }
    
    /**
     * Gets the permissions needed for recording based on Android version.
     * @return array of permission strings needed
     */
    public String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{android.Manifest.permission.READ_MEDIA_AUDIO};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{}; // No explicit permissions needed for MediaStore
        } else {
            return new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
    }

    /**
     * Checks if there's enough disk space for recording
     * @param context Android context
     * @param requiredMb Minimum required space in MB
     * @return true if enough space is available
     */
    private boolean hasEnoughDiskSpace(Context context, long requiredMb) {
        try {
            android.os.StatFs stat = new android.os.StatFs(Environment.getExternalStorageDirectory().getPath());
            long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
            long mbAvailable = bytesAvailable / (1024 * 1024);

            Log.d(TAG, "Available disk space: " + mbAvailable + " MB, required: " + requiredMb + " MB");
            return mbAvailable >= requiredMb;
        } catch (Exception e) {
            Log.e(TAG, "Failed to check disk space", e);
            // If we can't check, assume it's OK to avoid blocking recording
            return true;
        }
    }

    /**
     * Shows a warning toast about insufficient disk space
     * @param context Android context
     */
    private void showDiskSpaceWarning(Context context) {
        try {
            android.widget.Toast.makeText(context,
                context.getString(R.string.error_insufficient_disk_space),
                android.widget.Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to show disk space warning", e);
        }
    }

    /**
     * Creates recording metadata from recording info
     */
    private RecordingMetadata createRecordingMetadata(RunningRecordingInfo info, Recordable recordable) {
        RecordingMetadata metadata = new RecordingMetadata();
        metadata.fileName = info.getFileName();
        metadata.title = info.getTitle();
        metadata.format = recordable.getExtension();

        // Get current station information
        DataRadioStation currentStation = MediaSessionUtil.getCurrentStation();
        if (currentStation != null) {
            metadata.stationName = currentStation.Name;
            metadata.stationUrl = currentStation.StreamUrl;
        }

        // Get audio metadata from Shoutcast info
        try {
            com.nonameradio.app.station.live.ShoutcastInfo shoutcastInfo = MediaSessionUtil.getShoutcastInfo();
            if (shoutcastInfo != null) {
                metadata.bitrate = shoutcastInfo.bitrate;
                metadata.sampleRate = shoutcastInfo.sampleRate;
                metadata.channels = shoutcastInfo.channels;
                metadata.genre = shoutcastInfo.audioGenre;
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get Shoutcast info for recording metadata", e);
        }

        return metadata;
    }

    /**
     * Saves recording metadata to database
     */
    private void saveRecordingMetadata(RecordingMetadata metadata) {
        try {
            database.getQueryExecutor().execute(() -> {
                long id = database.recordingMetadataDao().insert(metadata);
                metadata.id = id;
                Log.d(TAG, "Saved recording metadata: " + metadata.fileName + " (id: " + id + ")");
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to save recording metadata", e);
        }
    }

    /**
     * Updates recording metadata in database
     */
    private void updateRecordingMetadata(RecordingMetadata metadata) {
        try {
            database.getQueryExecutor().execute(() -> {
                database.recordingMetadataDao().update(metadata);
                Log.d(TAG, "Updated recording metadata: " + metadata.fileName);
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to update recording metadata", e);
        }
    }

    /**
     * Deletes recording metadata from database
     */
    private void deleteRecordingMetadata(String fileName) {
        try {
            database.getQueryExecutor().execute(() -> {
                int deleted = database.recordingMetadataDao().deleteByFileName(fileName);
                if (deleted > 0) {
                    Log.d(TAG, "Deleted recording metadata for: " + fileName);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete recording metadata", e);
        }
    }

    /**
     * Gets set of files found during MediaStore scan
     */
    private Set<String> getFoundFilesFromMediaStore() {
        Set<String> foundFiles = new HashSet<>();
        try {
            ContentResolver resolver = context.getContentResolver();

            String[] projection = {MediaStore.Audio.Media.DISPLAY_NAME};
            String selection = MediaStore.Audio.Media.RELATIVE_PATH + " LIKE ?";
            String[] selectionArgs = {"%" + Environment.DIRECTORY_MUSIC + "/Recordings%"};

            try (android.database.Cursor cursor = resolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null)) {

                if (cursor != null) {
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                    while (cursor.moveToNext()) {
                        String fileName = cursor.getString(nameColumn);
                        if (fileName != null) {
                            foundFiles.add(fileName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting found files from MediaStore", e);
        }
        return foundFiles;
    }

    /**
     * Gets set of files found during file system scan
     */
    private Set<String> getFoundFilesFromFileSystem() {
        Set<String> foundFiles = new HashSet<>();
        try {
            String path = getRecordDir();
            File folder = new File(path);
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        foundFiles.add(f.getName());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting found files from file system", e);
        }
        return foundFiles;
    }

    /**
     * Synchronizes database metadata with found files
     * Removes metadata for files that no longer exist
     */
    private void synchronizeWithDatabaseMetadata(Set<String> foundFiles) {
        try {
            List<RecordingMetadata> allMetadata = getAllRecordingMetadata();

            for (RecordingMetadata metadata : allMetadata) {
                if (metadata != null && metadata.fileName != null) {
                    // If metadata exists but file doesn't exist, remove metadata
                    if (!foundFiles.contains(metadata.fileName)) {
                        Log.w(TAG, "Removing orphaned metadata for non-existent file: " + metadata.fileName);
                        deleteRecordingMetadata(metadata.fileName);
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Synchronized " + allMetadata.size() + " metadata entries with " + foundFiles.size() + " found files");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error synchronizing database metadata", e);
        }
    }

    /**
     * Gets recording metadata by file name
     */
    public RecordingMetadata getRecordingMetadata(String fileName) {
        try {
            return database.recordingMetadataDao().getByFileName(fileName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get recording metadata", e);
            return null;
        }
    }

    /**
     * Gets all recording metadata
     */
    public List<RecordingMetadata> getAllRecordingMetadata() {
        try {
            return database.recordingMetadataDao().getAll();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get all recording metadata", e);
            return new ArrayList<>();
        }
    }

    /**
     * Updates recording metadata when recording is completed
     */
    private void updateRecordingMetadataOnCompletion(RunningRecordingInfo info) {
        RecordingMetadata metadata = info.getMetadata();
        if (metadata == null) {
            Log.w(TAG, "No metadata found for completed recording: " + info.getFileName());
            return;
        }

        // Update final metadata
        metadata.endTime = System.currentTimeMillis();
        metadata.durationMs = metadata.endTime - metadata.startTime;
        metadata.fileSizeBytes = info.getBytesWritten();
        metadata.completed = true;

        // Update content URI if available
        if (info.getMediaStoreUri() != null) {
            metadata.contentUri = info.getMediaStoreUri().toString();
        }

        // Save updated metadata
        updateRecordingMetadata(metadata);
    }

}
