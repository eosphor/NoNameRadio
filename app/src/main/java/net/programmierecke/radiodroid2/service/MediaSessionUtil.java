package net.programmierecke.radiodroid2.service;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import net.programmierecke.radiodroid2.RadioDroidApp;
import net.programmierecke.radiodroid2.players.PlayState;
import net.programmierecke.radiodroid2.station.DataRadioStation;
import net.programmierecke.radiodroid2.station.live.ShoutcastInfo;
import net.programmierecke.radiodroid2.station.live.StreamLiveInfo;
import net.programmierecke.radiodroid2.utils.ImageLoader;

/**
 * Utility class for MediaSession integration
 * Replaces PlayerServiceUtil for new MediaSession-based playback
 */
public class MediaSessionUtil {
    
    private static final String TAG = "MediaSessionUtil";
    private static MediaControllerHelper mediaControllerHelper;
    
    /**
     * Initialize MediaSessionUtil with context
     */
    public static void initialize(Context context) {
        if (mediaControllerHelper == null) {
            mediaControllerHelper = MediaControllerHelper.getInstance(context);
            Log.d(TAG, "MediaSessionUtil initialized");
        }
    }
    
    /**
     * Play a station
     */
    public static void play(DataRadioStation station) {
        if (mediaControllerHelper != null) {
            mediaControllerHelper.playStation(station);
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized");
        }
    }
    
    /**
     * Pause playback
     */
    public static void pause() {
        if (mediaControllerHelper != null) {
            mediaControllerHelper.pausePlayback();
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized");
        }
    }
    
    /**
     * Stop playback
     */
    public static void stop() {
        if (mediaControllerHelper != null) {
            mediaControllerHelper.stopPlayback();
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized");
        }
    }
    
    /**
     * Check if currently playing
     */
    public static boolean isPlaying() {
        if (mediaControllerHelper != null) {
            return mediaControllerHelper.isPlaying();
        }
        return false;
    }
    
    /**
     * Get current station
     */
    public static DataRadioStation getCurrentStation() {
        if (mediaControllerHelper != null) {
            return mediaControllerHelper.getCurrentStation();
        }
        return null;
    }
    
    /**
     * Get playback state
     */
    public static PlayState getPlayerState() {
        if (mediaControllerHelper != null) {
            return mediaControllerHelper.getPlayState();
        }
        return PlayState.Idle;
    }
    
    /**
     * Toggle playback (play if paused, pause if playing)
     */
    public static void togglePlayback() {
        if (mediaControllerHelper != null) {
            mediaControllerHelper.togglePlayback();
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized");
        }
    }
    
    /**
     * Add station to favorites
     */
    public static void addToFavorites(DataRadioStation station) {
        if (mediaControllerHelper != null) {
            mediaControllerHelper.addToFavorites(station);
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized");
        }
    }
    
    /**
     * Toggle recording
     */
    public static void toggleRecording() {
        if (mediaControllerHelper != null) {
            mediaControllerHelper.toggleRecording();
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized");
        }
    }
    
    /**
     * Check if recording
     */
    public static boolean isRecording() {
        // For now, delegate to RadioPlayer directly
        // In future, this could be managed through MediaSession
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            return app.getRadioPlayer().isRecording();
        }
        return false;
    }
    
    /**
     * Get station icon using ImageLoader
     */
    public static void getStationIcon(ImageView imageView, String iconUrl) {
        if (mediaControllerHelper != null) {
            ImageLoader.loadStationIcon(mediaControllerHelper.getContext(), iconUrl, imageView);
        }
    }
    
    /**
     * Get Shoutcast info (delegate to RadioPlayer)
     */
    public static ShoutcastInfo getShoutcastInfo() {
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            return app.getRadioPlayer().getShoutcastInfo();
        }
        return null;
    }
    
    /**
     * Get live stream info (delegate to RadioPlayer)
     */
    public static StreamLiveInfo getMetadataLive() {
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            return app.getRadioPlayer().getStreamLiveInfo();
        }
        return null;
    }
    
    /**
     * Get transferred bytes (delegate to RadioPlayer)
     */
    public static long getTransferredBytes() {
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            return app.getRadioPlayer().getCurrentPlaybackTransferredBytes();
        }
        return 0;
    }
    
    /**
     * Get buffered seconds (delegate to RadioPlayer)
     */
    public static long getBufferedSeconds() {
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            return app.getRadioPlayer().getBufferedSeconds();
        }
        return 0;
    }
    
    /**
     * Check if MediaSession is connected
     */
    public static boolean isConnected() {
        if (mediaControllerHelper != null) {
            return mediaControllerHelper.isConnected();
        }
        return false;
    }
    
    /**
     * Cleanup resources
     */
    public static void destroy() {
        if (mediaControllerHelper != null) {
            mediaControllerHelper.destroy();
            mediaControllerHelper = null;
        }
    }
}
