package com.nonameradio.app.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.nonameradio.app.history.TrackHistoryDao;
import com.nonameradio.app.history.TrackHistoryEntry;
import com.nonameradio.app.recording.RecordingMetadataDao;
import com.nonameradio.app.recording.RecordingMetadata;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.nonameradio.app.history.TrackHistoryEntry.MAX_UNKNOWN_TRACK_DURATION;

@Database(entities = {TrackHistoryEntry.class, RecordingMetadata.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class NoNameRadioDatabase extends RoomDatabase {
    public abstract TrackHistoryDao songHistoryDao();
    public abstract RecordingMetadataDao recordingMetadataDao();

    private static volatile NoNameRadioDatabase INSTANCE;

    private final Executor queryExecutor = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "NoNameRadioDatabase Executor"));

    public static NoNameRadioDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NoNameRadioDatabase.class) {
                if (INSTANCE == null) {
                      INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                             NoNameRadioDatabase.class, "nonameradio_database")
                            .addCallback(CALLBACK)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public Executor getQueryExecutor() {
        return queryExecutor;
    }

    private static final RoomDatabase.Callback CALLBACK = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            INSTANCE.queryExecutor.execute(() -> {
                // App may have been terminated without notice so we should set last track history entry's
                // end time to something reasonable.
                INSTANCE.songHistoryDao().setLastHistoryItemEndTimeRelative(MAX_UNKNOWN_TRACK_DURATION);
            });
        }
    };

}
