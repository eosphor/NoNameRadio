package net.programmierecke.radiodroid2.service;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.programmierecke.radiodroid2.RadioDroidApp;
import net.programmierecke.radiodroid2.players.PlayState;
import net.programmierecke.radiodroid2.players.selector.PlayerType;
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
        }
    }
    
    /**
     * Play a station
     */
    public static void play(DataRadioStation station) {
        if (mediaControllerHelper != null) {
            mediaControllerHelper.play(station);
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized");
        }
    }
    
    /**
     * Pause playback
     */
    public static void pause() {
        if (mediaControllerHelper != null) {
            mediaControllerHelper.pause();
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized");
        }
    }
    
    /**
     * Stop playback
     */
    public static void stop() {
        if (mediaControllerHelper != null) {
            mediaControllerHelper.stop();
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
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized - isPlaying");
            return false;
        }
    }
    
    /**
     * Get current station
     */
    public static DataRadioStation getCurrentStation() {
        if (mediaControllerHelper != null) {
            return mediaControllerHelper.getCurrentStation();
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized - getCurrentStation");
            return null;
        }
    }
    
    /**
     * Get playback state
     */
    public static PlayState getPlayerState() {
        if (mediaControllerHelper != null) {
            return mediaControllerHelper.getPlayState();
        } else {
            Log.e(TAG, "MediaSessionUtil not initialized - getPlayerState");
            return PlayState.Idle;
        }
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
        getStationIcon(imageView, iconUrl, null);
    }

    public static void getStationIcon(ImageView imageView, String iconUrl, @Nullable Drawable placeholder) {
        if (mediaControllerHelper != null) {
            ImageLoader.loadStationIcon(mediaControllerHelper.getContext(), iconUrl, imageView, placeholder);
        } else if (placeholder != null) {
            imageView.setImageDrawable(placeholder);
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
     * Start service (compatibility method)
     */
    public static void startService(Context context) {
        initialize(context);
    }
    
    /**
     * Bind service (compatibility method)
     */
    public static void bindService(Context context) {
        initialize(context);
    }
    
    /**
     * Shutdown service (compatibility method)
     */
    public static void shutdownService() {
        destroy();
    }
    
    /**
     * Check if service is bound (compatibility method)
     */
    public static boolean isServiceBound() {
        return isConnected();
    }
    
    /**
     * Set station (compatibility method)
     */
    public static void setStation(DataRadioStation station) {
        // For MediaSession, we don't need to set station separately
        // It's handled automatically when playing
    }
    
    /**
     * Skip to next (compatibility method)
     */
    public static void skipToNext() {
        // Not implemented for radio - no next/previous concept
        Log.d(TAG, "skipToNext not applicable for radio streams");
    }
    
    /**
     * Skip to previous (compatibility method)
     */
    public static void skipToPrevious() {
        // Not implemented for radio - no next/previous concept
        Log.d(TAG, "skipToPrevious not applicable for radio streams");
    }
    
    /**
     * Pause with reason (compatibility method)
     */
    public static void pause(net.programmierecke.radiodroid2.service.PauseReason pauseReason) {
        pause();
    }
    
    /**
     * Resume playback (compatibility method)
     */
    public static void resume() {
        // For radio, resume means play current station
        DataRadioStation currentStation = getCurrentStation();
        if (currentStation != null) {
            play(currentStation);
        }
    }
    
    /**
     * Clear timer (compatibility method)
     */
    public static void clearTimer() {
        // Timer functionality not implemented in MediaSession yet
        Log.d(TAG, "clearTimer not implemented in MediaSession");
    }
    
    /**
     * Add timer (compatibility method)
     */
    public static void addTimer(int secondsAdd) {
        // Timer functionality not implemented in MediaSession yet
        Log.d(TAG, "addTimer not implemented in MediaSession");
    }
    
    /**
     * Get timer seconds (compatibility method)
     */
    public static long getTimerSeconds() {
        // Timer functionality not implemented in MediaSession yet
        return 0;
    }
    
    /**
     * Get station ID (compatibility method)
     */
    public static String getStationId() {
        DataRadioStation station = getCurrentStation();
        return station != null ? station.StationUuid : "";
    }
    
    /**
     * Start recording (compatibility method)
     */
    public static void startRecording() {
        // Recording functionality delegated to RadioPlayer
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            try {
                app.getRadioPlayer().startRecording(null); // No listener for now
            } catch (Exception e) {
                Log.e(TAG, "Failed to start recording", e);
            }
        }
    }

    /**
     * Stop recording (compatibility method)
     */
    public static void stopRecording() {
        // Recording functionality delegated to RadioPlayer
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            app.getRadioPlayer().stopRecording();
        }
    }
    
    /**
     * Get current record filename (compatibility method)
     */
    public static String getCurrentRecordFileName() {
        // Recording functionality delegated to RadioPlayer
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            return app.getRadioPlayer().getCurrentRecordFileName();
        }
        return null;
    }
    
    /**
     * Check if HLS (compatibility method)
     */
    public static boolean getIsHls() {
        // HLS detection delegated to RadioPlayer
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            return app.getRadioPlayer().isHls();
        }
        return false;
    }
    
    /**
     * Get last play start time (compatibility method)
     */
    public static long getLastPlayStartTime() {
        // Play start time delegated to RadioPlayer
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            return app.getRadioPlayer().getLastPlayStartTime();
        }
        return 0;
    }
    
    /**
     * Get pause reason (compatibility method)
     */
    public static net.programmierecke.radiodroid2.service.PauseReason getPauseReason() {
        // Pause reason not tracked in MediaSession
        return net.programmierecke.radiodroid2.service.PauseReason.USER;
    }
    
    /**
     * Enable MPD (compatibility method)
     */
    public static void enableMPD(String hostname, int port) {
        // MPD functionality delegated to RadioPlayer
        if (mediaControllerHelper != null) {
            RadioDroidApp app = (RadioDroidApp) mediaControllerHelper.getContext();
            app.getMpdClient().setMPDEnabled(true);
        }
    }
    
    /**
     * Warn about metered connection (compatibility method)
     */
    public static void warnAboutMeteredConnection(PlayerType playerType) {
        // Metered connection warning not implemented in MediaSession
        Log.d(TAG, "warnAboutMeteredConnection not implemented in MediaSession");
    }
    
    /**
     * Check if notification is active (compatibility method)
     */
    public static boolean isNotificationActive() {
        // Notification status not tracked in MediaSession
        return isPlaying();
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
