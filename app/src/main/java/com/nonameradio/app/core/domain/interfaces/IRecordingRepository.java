package com.nonameradio.app.core.domain.interfaces;

import com.nonameradio.app.recording.DataRecording;
import java.util.List;

public interface IRecordingRepository {
    List<DataRecording> getRecordings();
    void saveRecording(DataRecording recording);
    void deleteRecording(DataRecording recording);
    DataRecording getRecordingByName(String name);
    void updateRecording(DataRecording recording);
}

