package com.nonameradio.app.core.domain.interfaces;

import com.nonameradio.app.station.DataRadioStation;

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

