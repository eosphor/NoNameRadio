package com.nonameradio.app.players;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nonameradio.app.station.live.ShoutcastInfo;
import com.nonameradio.app.station.live.StreamLiveInfo;
import com.nonameradio.app.recording.Recordable;

import okhttp3.OkHttpClient;

public interface PlayerWrapper extends Recordable {
    interface PlayListener {
        void onStateChanged(PlayState state);

        void onPlayerWarning(final int messageId);

        void onPlayerError(final int messageId);

        void onDataSourceShoutcastInfo(ShoutcastInfo shoutcastInfo, boolean isHls);

        void onDataSourceStreamLiveInfo(StreamLiveInfo liveInfo);
    }

    void playRemote(@NonNull OkHttpClient httpClient, @NonNull String streamUrl, @NonNull Context context, boolean isAlarm);

    void pause();

    void stop();

    boolean isPlaying();

    long getBufferedMs();

    int getAudioSessionId();

    long getTotalTransferredBytes();

    long getCurrentPlaybackTransferredBytes();

    String getCurrentRecordFileName();

    boolean isLocal();

    boolean isHls();

    long getLastPlayStartTime();

    void setVolume(float newVolume);

    void setStateListener(PlayListener listener);
}
