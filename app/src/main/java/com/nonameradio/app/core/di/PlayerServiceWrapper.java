package com.nonameradio.app.core.di;

import android.content.Context;
import com.nonameradio.app.core.domain.interfaces.IPlayerService;
import com.nonameradio.app.players.selector.PlayerType;
import com.nonameradio.app.service.PlayerServiceUtil;
import com.nonameradio.app.station.DataRadioStation;

/**
 * Wrapper implementation of IPlayerService that integrates with existing PlayerService.
 * Uses PlayerServiceUtil for communication with the actual PlayerService.
 */
public class PlayerServiceWrapper implements IPlayerService {
    private final Context context;

    public PlayerServiceWrapper(Context context) {
        this.context = context.getApplicationContext();
        // Ensure service is bound
        PlayerServiceUtil.bindService(this.context);
    }

    @Override
    public void play(DataRadioStation station) {
        PlayerServiceUtil.play(station);
    }

    @Override
    public void pause() {
        PlayerServiceUtil.pause(null); // Use default pause reason
    }

    @Override
    public void stop() {
        PlayerServiceUtil.stop();
    }

    @Override
    public boolean isPlaying() {
        return PlayerServiceUtil.isPlaying();
    }

    @Override
    public boolean isRecording() {
        return PlayerServiceUtil.isRecording();
    }

    @Override
    public void startRecording() {
        PlayerServiceUtil.startRecording();
    }

    @Override
    public void stopRecording() {
        PlayerServiceUtil.stopRecording();
    }

    @Override
    public DataRadioStation getCurrentStation() {
        return PlayerServiceUtil.getCurrentStation();
    }

    @Override
    public long getCurrentPosition() {
        // For radio streams, return elapsed time since playback started
        if (PlayerServiceUtil.isPlaying()) {
            final long now = System.currentTimeMillis();
            final long startTime = PlayerServiceUtil.getLastPlayStartTime();
            if (startTime > 0) {
                return (now - startTime) / 1000; // Return seconds elapsed
            }
        }
        return 0;
    }
}
