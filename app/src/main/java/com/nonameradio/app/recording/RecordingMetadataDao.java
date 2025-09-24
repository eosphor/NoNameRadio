package com.nonameradio.app.recording;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for RecordingMetadata
 */
@Dao
public interface RecordingMetadataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RecordingMetadata recording);

    @Update
    void update(RecordingMetadata recording);

    @Delete
    void delete(RecordingMetadata recording);

    @Query("SELECT * FROM recording_metadata WHERE id = :id")
    RecordingMetadata getById(long id);

    @Query("SELECT * FROM recording_metadata WHERE fileName = :fileName ORDER BY startTime DESC LIMIT 1")
    RecordingMetadata getByFileName(String fileName);

    @Query("SELECT * FROM recording_metadata WHERE contentUri = :contentUri ORDER BY startTime DESC LIMIT 1")
    RecordingMetadata getByContentUri(String contentUri);

    @Query("SELECT * FROM recording_metadata ORDER BY startTime DESC")
    List<RecordingMetadata> getAll();

    @Query("SELECT * FROM recording_metadata WHERE completed = 1 ORDER BY startTime DESC")
    List<RecordingMetadata> getCompleted();

    @Query("SELECT * FROM recording_metadata WHERE completed = 0 ORDER BY startTime DESC")
    List<RecordingMetadata> getInProgress();

    @Query("DELETE FROM recording_metadata WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM recording_metadata WHERE fileName = :fileName")
    int deleteByFileName(String fileName);

    @Query("DELETE FROM recording_metadata WHERE contentUri = :contentUri")
    void deleteByContentUri(String contentUri);

    @Query("DELETE FROM recording_metadata")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM recording_metadata")
    int getCount();

    @Query("SELECT COUNT(*) FROM recording_metadata WHERE completed = 1")
    int getCompletedCount();
}
