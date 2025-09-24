package com.nonameradio.app.recording;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.nonameradio.app.database.Converters;

import java.util.Date;
import java.util.Locale;

/**
 * Entity for storing recording metadata in the database
 */
@Entity(tableName = "recording_metadata")
@TypeConverters({Converters.class})
public class RecordingMetadata {
    @PrimaryKey(autoGenerate = true)
    public long id;

    // File information
    public String fileName;
    public String filePath;
    public String contentUri;

    // Recording details
    public String stationName;
    public String stationUrl;
    public long startTime; // timestamp when recording started
    public long endTime; // timestamp when recording ended
    public Long durationMs; // recording duration in milliseconds

    // Audio metadata
    public String title;
    public String artist;
    public String album;
    public String genre;
    public Integer bitrate; // kbps
    public Integer sampleRate; // Hz
    public Integer channels; // 1 for mono, 2 for stereo
    public String format; // mp3, aac, etc.

    // File size and status
    public Long fileSizeBytes;
    public boolean completed;

    // Constructor
    public RecordingMetadata() {
        this.startTime = System.currentTimeMillis();
        this.completed = false;
    }

    // Getters and setters for better data access
    public String getDisplayName() {
        return title != null && !title.isEmpty() ? title : fileName;
    }

    public String getDurationString() {
        if (durationMs == null || durationMs <= 0) return "00:00";

        long totalSeconds = durationMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    public String getFileSizeString() {
        if (fileSizeBytes == null || fileSizeBytes <= 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = fileSizeBytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format(Locale.ROOT, "%.1f %s", size, units[unitIndex]);
    }

    public Date getStartDate() {
        return new Date(startTime);
    }

    public Date getEndDate() {
        return endTime > 0 ? new Date(endTime) : null;
    }
}
