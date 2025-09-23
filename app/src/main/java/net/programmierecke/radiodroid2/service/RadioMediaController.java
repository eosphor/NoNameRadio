package net.programmierecke.radiodroid2.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import net.programmierecke.radiodroid2.station.DataRadioStation;

/**
 * MediaController wrapper for radio playback control.
 * Provides a simplified interface for controlling the MediaSessionService.
 */
public class RadioMediaController {
    private static final String TAG = "RadioMediaController";
    
    private final Context context;
    private MediaController mediaController;
    private ListenableFuture<MediaController> controllerFuture;
    
    public RadioMediaController(Context context) {
        this.context = context;
    }
    
    public void initialize() {
        SessionToken sessionToken = new SessionToken(
                context,
                new ComponentName(context, RadioMediaSessionService.class)
        );
        
        controllerFuture = new MediaController.Builder(context, sessionToken).buildAsync();
        
        controllerFuture.addListener(() -> {
            try {
                mediaController = controllerFuture.get();
                Log.d(TAG, "MediaController initialized");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize MediaController", e);
            }
        }, MoreExecutors.directExecutor());
    }
    
    public void playStation(DataRadioStation station) {
        if (mediaController == null) {
            Log.w(TAG, "MediaController not initialized");
            return;
        }
        
        MediaItem mediaItem = new MediaItem.Builder()
                .setMediaId(station.StationUuid)
                .setUri(station.playableUrl)
                .setMediaMetadata(station.toMediaMetadata())
                .build();
        
        mediaController.setMediaItem(mediaItem);
        mediaController.prepare();
        mediaController.play();
    }
    
    public void pause() {
        if (mediaController != null) {
            mediaController.pause();
        }
    }
    
    public void resume() {
        if (mediaController != null) {
            mediaController.play();
        }
    }
    
    public void stop() {
        if (mediaController != null) {
            mediaController.stop();
        }
    }
    
    public boolean isPlaying() {
        return mediaController != null && mediaController.isPlaying();
    }
    
    public void setVolume(float volume) {
        if (mediaController != null) {
            mediaController.setVolume(volume);
        }
    }
    
    public void release() {
        if (controllerFuture != null) {
            MediaController.releaseFuture(controllerFuture);
            controllerFuture = null;
        }
        mediaController = null;
    }
    
    public MediaController getMediaController() {
        return mediaController;
    }
}
