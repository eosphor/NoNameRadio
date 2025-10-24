package com.nonameradio.app.service;

import android.content.Context;
import android.util.Log;
import android.content.Intent;
import android.os.Build;

import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.ActivityMain;
import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.core.event.PlayerStateChangeEvent;
import com.nonameradio.app.core.event.MetaUpdateEvent;
import com.nonameradio.app.players.RadioPlayer;
import com.nonameradio.app.players.PlayState;
import com.nonameradio.app.station.DataRadioStation;
import com.nonameradio.app.recording.RecordingsManager;

/**
 * Helper class for UI integration with MediaController
 * Provides simple interface for UI components to control playback
 */
public class MediaControllerHelper {
    
    private static final String TAG = "MediaControllerHelper";
    
    private static MediaControllerHelper instance;
    private RadioMediaController mediaController;
    private RadioPlayer radioPlayer;
    private Context context;
    
    private MediaControllerHelper(Context context) {
        this.context = context.getApplicationContext();
        this.radioPlayer = ((NoNameRadioApp) this.context).getRadioPlayer();
        
        // Initialize MediaController
        mediaController = new RadioMediaController(this.context);
        
        // Set up playback state listener to notify UI
        mediaController.setPlaybackStateListener(new RadioMediaController.PlaybackStateListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                Log.d(TAG, "MediaSession playback state changed: " + state);
                
                // Notify UI components about state change via EventBus
                PlayState playState = convertPlaybackState(state);
                EventBus.post(new PlayerStateChangeEvent(playState));
            }
            
            @Override
            public void onMetadataChanged(android.support.v4.media.MediaMetadataCompat metadata) {
                Log.d(TAG, "MediaSession metadata changed");
                
                // Notify UI components about metadata change via EventBus
                EventBus.post(MetaUpdateEvent.INSTANCE);
            }
        });
        
        mediaController.connect();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized MediaControllerHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MediaControllerHelper(context);
        }
        return instance;
    }
    
    /**
     * Play a station
     */
    public void playStation(DataRadioStation station) {
        playStation(station, false);
    }

    /**
     * Play a station with alarm flag
     */
    public void playStation(DataRadioStation station, boolean isAlarm) {
        Log.d(TAG, "Playing station via MediaController: " + station.Name + ", isAlarm: " + isAlarm);

        // Сначала добавим станцию в историю, чтобы PlayerService мог её найти
        NoNameRadioApp app = (NoNameRadioApp) context;
        app.getHistoryManager().add(station);

        // Start PlayerService directly to ensure notifications are shown
        Intent serviceIntent = new Intent(context, PlayerService.class);
        serviceIntent.putExtra("stationid", station.StationUuid);
        serviceIntent.putExtra("isAlarm", isAlarm);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        // MediaController.play() НЕ вызываем здесь, так как PlayerService сам начнёт воспроизведение
        // Вызов mediaController.play() приводил к race condition с RadioSessionCallback.onPlay()
    }

    /**
     * Play a station (alias for playStation)
     */
    public void play(DataRadioStation station) {
        playStation(station, false);
    }
    
    /**
     * Pause playback
     */
    public void pausePlayback() {
        Log.d(TAG, "Pausing playback via MediaController");
        mediaController.pause();
    }

    /**
     * Pause playback (alias for pausePlayback)
     */
    public void pause() {
        pausePlayback();
    }
    
    /**
     * Resume playback
     */
    public void resumePlayback() {
        Log.d(TAG, "Resuming playback via MediaController");
        mediaController.play();
    }
    
    /**
     * Stop playback
     */
    public void stopPlayback() {
        Log.d(TAG, "Stopping playback via MediaController");
        mediaController.stop();
    }

    /**
     * Stop playback (alias for stopPlayback)
     */
    public void stop() {
        stopPlayback();
    }
    
    /**
     * Toggle playback (play if paused, pause if playing)
     */
    public void togglePlayback() {
        if (radioPlayer.isPlaying()) {
            pausePlayback();
        } else {
            resumePlayback();
        }
    }
    
    /**
     * Check if currently playing
     */
    public boolean isPlaying() {
        // Use MediaSession as primary source of truth for cross-process consistency
        if (mediaController.isConnected()) {
            int playbackState = mediaController.getPlaybackState();
            boolean mediaSessionPlaying = (playbackState == android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING);
            
            // Also check local RadioPlayer for debugging
            boolean radioPlayerPlaying = radioPlayer.isPlaying();
            
            // Log for debugging
            Log.d(TAG, "isPlaying check - MediaSession: " + mediaSessionPlaying + 
                      " (state=" + playbackState + "), RadioPlayer: " + radioPlayerPlaying);
            
            // Return MediaSession state as primary source for cross-process consistency
            Log.d(TAG, "isPlaying() returning: " + mediaSessionPlaying);
            return mediaSessionPlaying;
        } else {
            // Fallback to RadioPlayer only if MediaController is not connected
            boolean radioPlayerPlaying = radioPlayer.isPlaying();
            Log.d(TAG, "MediaController not connected, using RadioPlayer: " + radioPlayerPlaying);
            Log.d(TAG, "isPlaying() returning: " + radioPlayerPlaying);
            return radioPlayerPlaying;
        }
    }
    
    /**
     * Get current station
     */
    public DataRadioStation getCurrentStation() {
        return radioPlayer.getCurrentStation();
    }
    
    /**
     * Get playback state
     */
    public PlayState getPlayState() {
        return radioPlayer.getPlayState();
    }
    
    /**
     * Add station to favorites
     */
    public void addToFavorites(DataRadioStation station) {
        Log.d(TAG, "Adding station to favorites: " + station.Name);
        mediaController.addToFavorites(station);
    }
    
    /**
     * Toggle recording
     */
    public void toggleRecording() {
        Log.d(TAG, "Toggling recording via MediaController");
        NoNameRadioApp app = (NoNameRadioApp) context;
        RecordingsManager recordingsManager = app.getRecordingsManager();
        RadioPlayer player = app.getRadioPlayer();
        if (player.isRecording()) {
            recordingsManager.stopRecording(player);
        } else {
            recordingsManager.record(context, player);
        }
    }
    
    /**
     * Get context
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * Check if MediaController is connected
     */
    public boolean isConnected() {
        return mediaController.isConnected();
    }
    
    /**
     * Convert MediaSession playback state to PlayState
     */
    private PlayState convertPlaybackState(int mediaSessionState) {
        switch (mediaSessionState) {
            case android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING:
                return PlayState.Playing;
            case android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED:
                return PlayState.Paused;
            case android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED:
                return PlayState.Idle;
            case android.support.v4.media.session.PlaybackStateCompat.STATE_ERROR:
                return PlayState.Idle; // Map error to idle
            default:
                return PlayState.Idle;
        }
    }
    
    /**
     * Cleanup resources
     */
    public void destroy() {
        if (mediaController != null) {
            mediaController.disconnect();
        }
        instance = null;
    }
}
