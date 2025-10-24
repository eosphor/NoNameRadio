package com.nonameradio.app.data.repository.impl;

import com.nonameradio.app.core.domain.interfaces.IRecordingRepository;
import com.nonameradio.app.recording.DataRecording;
import com.nonameradio.app.recording.RecordingsManager;
import java.util.ArrayList;
import java.util.List;

public class RecordingRepository implements IRecordingRepository {
    private final RecordingsManager recordingsManager;

    public RecordingRepository(RecordingsManager recordingsManager) {
        this.recordingsManager = recordingsManager;
    }

    @Override
    public List<DataRecording> getRecordings() {
        return new ArrayList<>(recordingsManager.getSavedRecordings());
    }

    @Override
    public void saveRecording(DataRecording recording) {
        // Recordings are automatically saved by RecordingsManager
        // This method is for interface compatibility
    }

    @Override
    public void deleteRecording(DataRecording recording) {
        if (recording != null) {
            recordingsManager.deleteRecording(recording);
        }
    }

    @Override
    public DataRecording getRecordingByName(String name) {
        if (name == null) return null;

        List<DataRecording> recordings = recordingsManager.getSavedRecordings();
        for (DataRecording recording : recordings) {
            if (name.equals(recording.Name)) {
                return recording;
            }
        }
        return null;
    }

    @Override
    public void updateRecording(DataRecording recording) {
        // Recordings are automatically updated by RecordingsManager
        // This method is for interface compatibility
    }
}

