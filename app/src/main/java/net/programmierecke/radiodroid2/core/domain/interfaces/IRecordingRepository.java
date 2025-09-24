package net.programmierecke.radiodroid2.core.domain.interfaces;

import net.programmierecke.radiodroid2.recording.DataRecording;
import java.util.List;

public interface IRecordingRepository {
    List<DataRecording> getRecordings();
    void saveRecording(DataRecording recording);
    void deleteRecording(DataRecording recording);
    DataRecording getRecordingByName(String name);
    void updateRecording(DataRecording recording);
}

