package net.programmierecke.radiodroid2.core.domain.interfaces;

import net.programmierecke.radiodroid2.station.DataRadioStation;

public interface IPlayerService {
    void play(DataRadioStation station);
    void pause();
    void stop();
    boolean isPlaying();
    boolean isRecording();
    void startRecording();
    void stopRecording();
    DataRadioStation getCurrentStation();
    long getCurrentPosition();
}

