package com.nonameradio.app.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.nonameradio.app.ActivityMain;
import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.core.event.PlayerStateChangeEvent;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.history.TrackHistoryEntry;
import com.nonameradio.app.history.TrackHistoryRepository;
import com.nonameradio.app.players.PlayState;
import com.nonameradio.app.players.RadioPlayer;
import com.nonameradio.app.station.DataRadioStation;

/**
 * Manager for MediaSession integration with RadioPlayer
 * Handles synchronization between RadioPlayer state and MediaSession
 */
public class MediaSessionManager implements RadioPlayer.PlayerListener {
    
    private static final String TAG = "MediaSessionManager";
    
    private final Context context;
    private final RadioPlayer radioPlayer;
    private final TrackHistoryRepository trackHistoryRepository;
    private RadioMediaSessionService mediaSessionService;
    private RadioMediaController mediaController;
    
    public MediaSessionManager(Context context, RadioPlayer radioPlayer) {
        this.context = context;
        this.radioPlayer = radioPlayer;

        NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();
        this.trackHistoryRepository = app.getTrackHistoryRepository();

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
        
        // Notify UI components about state change via EventBus
        EventBus.post(new PlayerStateChangeEvent(status));
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
    public void foundShoutcastStream(com.nonameradio.app.station.live.ShoutcastInfo bitrate, boolean isHls) {
        // Not needed for MediaSession
    }
    
    @Override
    public void foundLiveStreamInfo(com.nonameradio.app.station.live.StreamLiveInfo liveInfo) {
        if (liveInfo == null) {
            return;
        }

        if (mediaSessionService != null) {
            DataRadioStation currentStation = radioPlayer.getCurrentStation();
            if (currentStation != null) {
                mediaSessionService.updateSessionMetadata(currentStation);
            }
        }

        if (trackHistoryRepository == null) {
            return;
        }

        final DataRadioStation currentStation = radioPlayer.getCurrentStation();
        if (currentStation == null) {
            return;
        }

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        java.util.Date currentTime = calendar.getTime();

        trackHistoryRepository.getLastInsertedHistoryItem((trackHistoryEntry, dao) -> {
            if (trackHistoryEntry != null && trackHistoryEntry.title.equals(liveInfo.getTitle())) {
                trackHistoryEntry.endTime = new java.util.Date(0);
                dao.update(trackHistoryEntry);
            } else {
                dao.setCurrentPlayingTrackEndTime(currentTime);

                TrackHistoryEntry newEntry = new TrackHistoryEntry();
                newEntry.stationUuid = currentStation.StationUuid;
                newEntry.stationIconUrl = currentStation.IconUrl;
                newEntry.artist = liveInfo.getArtist();
                newEntry.track = liveInfo.getTrack();
                newEntry.title = liveInfo.getTitle();
                newEntry.startTime = currentTime;
                newEntry.endTime = new java.util.Date(0);

                trackHistoryRepository.insert(newEntry);
            }
        });
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
