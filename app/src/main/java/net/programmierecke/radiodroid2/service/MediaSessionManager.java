package net.programmierecke.radiodroid2.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import net.programmierecke.radiodroid2.players.RadioPlayer;
import net.programmierecke.radiodroid2.players.PlayState;
import net.programmierecke.radiodroid2.station.DataRadioStation;

/**
 * Manager for MediaSession integration with RadioPlayer
 * Handles synchronization between RadioPlayer state and MediaSession
 */
public class MediaSessionManager implements RadioPlayer.PlayerListener {
    
    private static final String TAG = "MediaSessionManager";
    
    private final Context context;
    private final RadioPlayer radioPlayer;
    private RadioMediaSessionService mediaSessionService;
    private RadioMediaController mediaController;
    
    public MediaSessionManager(Context context, RadioPlayer radioPlayer) {
        this.context = context;
        this.radioPlayer = radioPlayer;
        
        // Set this manager as listener for RadioPlayer
        radioPlayer.setPlayerListener(this);
        
        // Initialize MediaController for client-side control
        mediaController = new RadioMediaController(context);
        mediaController.connect();
    }
    
    /**
     * Set the MediaSessionService for server-side management
     */
    public void setMediaSessionService(RadioMediaSessionService service) {
        this.mediaSessionService = service;
    }
    
    /**
     * Play a station using MediaController
     */
    public void playStation(DataRadioStation station) {
        Log.d(TAG, "Playing station: " + station.Name);
        radioPlayer.play(station, false);
    }
    
    /**
     * Pause playback using MediaController
     */
    public void pausePlayback() {
        Log.d(TAG, "Pausing playback");
        mediaController.pause();
    }
    
    /**
     * Resume playback using MediaController
     */
    public void resumePlayback() {
        Log.d(TAG, "Resuming playback");
        mediaController.play();
    }
    
    /**
     * Stop playback using MediaController
     */
    public void stopPlayback() {
        Log.d(TAG, "Stopping playback");
        mediaController.stop();
    }
    
    /**
     * Get MediaController for UI integration
     */
    public RadioMediaController getMediaController() {
        return mediaController;
    }
    
    /**
     * Cleanup resources
     */
    public void destroy() {
        if (mediaController != null) {
            mediaController.disconnect();
        }
    }
    
    // RadioPlayer.PlayerListener implementation
    
    @Override
    public void onStateChanged(PlayState status, int audioSessionId) {
        Log.d(TAG, "RadioPlayer state changed: " + status);
        
        if (mediaSessionService != null) {
            // Convert RadioPlayer state to MediaSession state
            int mediaSessionState = convertPlayState(status);
            long position = 0; // Radio streams don't have position
            float speed = status == PlayState.Playing ? 1.0f : 0.0f;
            
            mediaSessionService.updatePlaybackState(mediaSessionState, position, speed);
            
            // Update metadata if we have a current station
            DataRadioStation currentStation = radioPlayer.getCurrentStation();
            if (currentStation != null) {
                mediaSessionService.updateSessionMetadata(currentStation);
            }
        }
    }
    
    @Override
    public void onPlayerWarning(int messageId) {
        Log.w(TAG, "Player warning: " + messageId);
    }
    
    @Override
    public void onPlayerError(int messageId) {
        Log.e(TAG, "Player error: " + messageId);
        
        if (mediaSessionService != null) {
            // Set error state in MediaSession
            mediaSessionService.updatePlaybackState(
                PlaybackStateCompat.STATE_ERROR, 
                0, 
                0.0f
            );
        }
    }
    
    @Override
    public void onBufferedTimeUpdate(long bufferedMs) {
        // Not needed for MediaSession
    }
    
    @Override
    public void foundShoutcastStream(net.programmierecke.radiodroid2.station.live.ShoutcastInfo bitrate, boolean isHls) {
        // Not needed for MediaSession
    }
    
    @Override
    public void foundLiveStreamInfo(net.programmierecke.radiodroid2.station.live.StreamLiveInfo liveInfo) {
        // Not needed for MediaSession
    }
    
    /**
     * Convert RadioPlayer.PlayState to MediaSession PlaybackState
     */
    private int convertPlayState(PlayState playState) {
        switch (playState) {
            case Idle:
                return PlaybackStateCompat.STATE_STOPPED;
            case PrePlaying:
                return PlaybackStateCompat.STATE_BUFFERING;
            case Playing:
                return PlaybackStateCompat.STATE_PLAYING;
            case Paused:
                return PlaybackStateCompat.STATE_PAUSED;
            default:
                return PlaybackStateCompat.STATE_NONE;
        }
    }
}
