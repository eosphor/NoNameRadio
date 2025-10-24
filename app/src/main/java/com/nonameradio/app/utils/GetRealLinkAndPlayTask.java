package com.nonameradio.app.utils;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.nonameradio.app.IPlayerService;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.Utils;
import com.nonameradio.app.station.DataRadioStation;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;

import okhttp3.OkHttpClient;

/**
 * Resolves real playable URL for a station and starts playback.
 * Migrated from AsyncTask to CompletableFuture for modern async handling.
 */
public class GetRealLinkAndPlayTask {
    private static final String TAG = "GetRealLinkAndPlayTask";
    
    private final WeakReference<Context> contextRef;
    private final DataRadioStation station;
    private final WeakReference<IPlayerService> playerServiceRef;
    private final OkHttpClient httpClient;
    
    private CompletableFuture<String> future;

    public GetRealLinkAndPlayTask(Context context, DataRadioStation station, IPlayerService playerService) {
        this.contextRef = new WeakReference<>(context);
        this.station = station;
        this.playerServiceRef = new WeakReference<>(playerService);

        NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();
        httpClient = app.getHttpClient();
    }

    public CompletableFuture<String> executeAsync() {
        future = com.nonameradio.app.core.utils.AsyncExecutor.submitIOTask(() -> {
            Context context = contextRef.get();
            if (context == null) {
                throw new IllegalStateException("Context is null");
            }
            
            String realLink = Utils.getRealStationLink(httpClient, context.getApplicationContext(), station.StationUuid);
            if (realLink == null || realLink.isEmpty()) {
                throw new IllegalStateException("Failed to resolve station URL");
            }
            
            return realLink;
        });

        future.thenAccept(resolvedUrl -> {
            // Execute on main thread
            com.nonameradio.app.core.utils.UiHandler.post(() -> {
                IPlayerService playerService = playerServiceRef.get();
                if (playerService != null) {
                    try {
                        station.playableUrl = resolvedUrl;
                        playerService.SetStation(station);
                        playerService.Play(false);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error setting station", e);
                    }
                }
            });
        }).exceptionally(throwable -> {
            Log.e(TAG, "Error resolving station URL", throwable);
            return null;
        });

        return future;
    }

    /**
     * Cancel the task if it's still running
     */
    public void cancel(boolean mayInterruptIfRunning) {
        if (future != null) {
            future.cancel(mayInterruptIfRunning);
        }
    }
}
