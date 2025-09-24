package com.nonameradio.app.core.di;

import com.nonameradio.app.core.domain.interfaces.IPlayerService;
import com.nonameradio.app.station.DataRadioStation;

/**
 * Stub implementation of IPlayerService for refactored architecture.
 * Will be properly implemented when integrating with existing PlayerService.
 */
public class PlayerServiceWrapper implements IPlayerService {

    @Override
    public void play(DataRadioStation station) {
        // TODO: Implement when integrating with PlayerService
    }

    @Override
    public void pause() {
        // TODO: Implement when integrating with PlayerService
    }

    @Override
    public void stop() {
        // TODO: Implement when integrating with PlayerService
    }

    @Override
    public boolean isPlaying() {
        // TODO: Implement when integrating with PlayerService
        return false;
    }

    @Override
    public boolean isRecording() {
        // TODO: Implement when integrating with PlayerService
        return false;
    }

    @Override
    public void startRecording() {
        // TODO: Implement when integrating with PlayerService
    }

    @Override
    public void stopRecording() {
        // TODO: Implement when integrating with PlayerService
    }

    @Override
    public DataRadioStation getCurrentStation() {
        // TODO: Implement when integrating with PlayerService
        return null;
    }

    @Override
    public long getCurrentPosition() {
        // TODO: Implement when integrating with PlayerService
        return 0;
    }
}
