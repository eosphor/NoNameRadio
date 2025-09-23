package net.programmierecke.radiodroid2.service;

import android.content.Context;
import android.util.Log;

import net.programmierecke.radiodroid2.RadioDroidApp;
import net.programmierecke.radiodroid2.players.RadioPlayer;
import net.programmierecke.radiodroid2.players.PlayState;
import net.programmierecke.radiodroid2.station.DataRadioStation;

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
        this.radioPlayer = ((RadioDroidApp) this.context).getRadioPlayer();
        
        // Initialize MediaController
        mediaController = new RadioMediaController(this.context);
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
        Log.d(TAG, "Playing station via MediaController: " + station.Name);
        
        // Use RadioPlayer directly for now
        radioPlayer.play(station, false);
        
        // Also send command via MediaController for system integration
        mediaController.play();
    }

    /**
     * Play a station (alias for playStation)
     */
    public void play(DataRadioStation station) {
        playStation(station);
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
        return radioPlayer.isPlaying();
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
        mediaController.toggleRecording();
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
     * Cleanup resources
     */
    public void destroy() {
        if (mediaController != null) {
            mediaController.disconnect();
        }
        instance = null;
    }
}
