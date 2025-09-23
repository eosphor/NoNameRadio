package net.programmierecke.radiodroid2.service;

import android.content.Intent;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import net.programmierecke.radiodroid2.players.RadioPlayer;
import net.programmierecke.radiodroid2.players.PlayState;
import net.programmierecke.radiodroid2.station.DataRadioStation;

/**
 * Media3 Session-based service for radio playback.
 * Replaces the old PlayerService with modern Media3 Session architecture.
 */
public class RadioMediaSessionService extends MediaSessionService implements RadioPlayer.PlayerListener {
    private static final String TAG = "RadioMediaSessionService";
    
    private MediaSession mediaSession;
    private RadioPlayer radioPlayer;
    private DataRadioStation currentStation;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize ExoPlayer
        ExoPlayer exoPlayer = new ExoPlayer.Builder(this).build();
        
        // Initialize RadioPlayer wrapper
        radioPlayer = new RadioPlayer(this);
        radioPlayer.setPlayerListener(this);
        
        // Create MediaSession
        mediaSession = new MediaSession.Builder(this, exoPlayer)
                .build();
    }
    
    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.getPlayer().release();
            mediaSession = null;
        }
        if (radioPlayer != null) {
            radioPlayer.destroy();
            radioPlayer = null;
        }
        super.onDestroy();
    }
    
    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }
    
    // RadioPlayer.PlayerListener implementation
    @Override
    public void onStateChanged(PlayState state, int audioSessionId) {
        Log.d(TAG, "State changed: " + state);
        // Update MediaSession state based on RadioPlayer state
        updateMediaSessionState(state);
    }
    
    @Override
    public void onPlayerWarning(int messageId) {
        Log.w(TAG, "Player warning: " + messageId);
    }
    
    @Override
    public void onPlayerError(int messageId) {
        Log.e(TAG, "Player error: " + messageId);
    }
    
    @Override
    public void onBufferedTimeUpdate(long bufferedMs) {
        // Handle buffering updates if needed
    }
    
    @Override
    public void foundShoutcastStream(net.programmierecke.radiodroid2.station.live.ShoutcastInfo bitrate, boolean isHls) {
        // Handle Shoutcast info
    }
    
    @Override
    public void foundLiveStreamInfo(net.programmierecke.radiodroid2.station.live.StreamLiveInfo liveInfo) {
        // Update MediaSession metadata with live info
        updateMediaSessionMetadata(liveInfo);
    }
    
    private void updateMediaSessionState(PlayState state) {
        if (mediaSession == null) return;
        
        Player player = mediaSession.getPlayer();
        switch (state) {
            case Playing:
                player.setPlayWhenReady(true);
                break;
            case Paused:
                player.setPlayWhenReady(false);
                break;
            case Idle:
                player.stop();
                break;
            case PrePlaying:
                // Buffering state
                break;
        }
    }
    
    private void updateMediaSessionMetadata(net.programmierecke.radiodroid2.station.live.StreamLiveInfo liveInfo) {
        if (mediaSession == null || currentStation == null) return;
        
        MediaItem currentItem = mediaSession.getPlayer().getCurrentMediaItem();
        if (currentItem != null) {
            MediaItem updatedItem = currentItem.buildUpon()
                    .setMediaMetadata(currentItem.mediaMetadata.buildUpon()
                            .setTitle(liveInfo.getTrack())
                            .setArtist(liveInfo.getArtist())
                            .setAlbumTitle(currentStation.Name)
                            .build())
                    .build();
            
            mediaSession.getPlayer().replaceMediaItem(0, updatedItem);
        }
    }
    
    public void setCurrentStation(DataRadioStation station) {
        this.currentStation = station;
    }
    
    public void playStation(DataRadioStation station, boolean isAlarm) {
        setCurrentStation(station);
        radioPlayer.play(station, isAlarm);
    }
    
    public void pause() {
        radioPlayer.pause();
    }
    
    public void resume() {
        radioPlayer.play(currentStation, false);
    }
    
    public void stop() {
        radioPlayer.stop();
    }
    
    public boolean isPlaying() {
        return radioPlayer.isPlaying();
    }
}
