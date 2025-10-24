package com.nonameradio.app.players;

import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.core.event.ShowLoadingEvent;
import com.nonameradio.app.core.event.HideLoadingEvent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nonameradio.app.CastHandler;
import com.nonameradio.app.HistoryManager;
import com.nonameradio.app.R;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.Utils;
import com.nonameradio.app.players.mpd.MPDClient;
import com.nonameradio.app.players.mpd.MPDServerData;
import com.nonameradio.app.players.mpd.tasks.MPDPlayTask;
import com.nonameradio.app.station.DataRadioStation;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;

/**
 * Resolves station URL and initiates playback.
 * Migrated from AsyncTask to CompletableFuture for modern async handling.
 * CRITICAL COMPONENT - handles main playback logic.
 */
public class PlayStationTask {
    private static final String TAG = "PlayStationTask";
    public interface PlayFunc {
        void play(String url);
    }

    public enum ExecutionResult {
        FAILURE,
        SUCCESS,
    }

    public interface PostExecuteTask {
        void onPostExecute(ExecutionResult executionResult);
    }

    private final PlayFunc playFunc;
    private final PostExecuteTask postExecuteTask;
    private final DataRadioStation stationToPlay;
    private final WeakReference<Context> contextWeakReference;
    
    private CompletableFuture<String> future;

    public PlayStationTask(@NonNull DataRadioStation stationToPlay, @NonNull Context ctx,
                           @NonNull PlayFunc playFunc, @Nullable PostExecuteTask postExecuteTask) {
        this.stationToPlay = stationToPlay;
        this.contextWeakReference = new WeakReference<>(ctx);
        this.playFunc = playFunc;
        this.postExecuteTask = postExecuteTask;
    }

    public static PlayStationTask playMPD(MPDClient mpdClient, MPDServerData mpdServerData, DataRadioStation stationToPlay, Context ctx) {
        return new PlayStationTask(stationToPlay, ctx, url -> mpdClient.enqueueTask(mpdServerData, new MPDPlayTask(url, null)), null);
    }

    public static PlayStationTask playExternal(DataRadioStation stationToPlay, Context ctx) {
        return new PlayStationTask(stationToPlay, ctx, url -> {
            Intent share = new Intent(Intent.ACTION_VIEW);
            share.setDataAndType(Uri.parse(url), "audio/*");
            ctx.startActivity(share);
        }, null);
    }

    public static PlayStationTask playCAST(DataRadioStation stationToPlay, Context ctx) {
        NoNameRadioApp app = (NoNameRadioApp) ctx.getApplicationContext();
        CastHandler castHandler = app.getCastHandler();
        return new PlayStationTask(stationToPlay, ctx, url -> castHandler.playRemote(stationToPlay.Name, url, stationToPlay.IconUrl), null);
    }

    public CompletableFuture<String> executeAsync() {
        Context ctx = contextWeakReference.get();
        if (ctx == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Show loading indicator on main thread
        EventBus.post(ShowLoadingEvent.INSTANCE);

        // Add to history immediately
        NoNameRadioApp app = (NoNameRadioApp) ctx.getApplicationContext();
        HistoryManager historyManager = app.getHistoryManager();
        historyManager.add(stationToPlay);

        // Background work: resolve station URL
        future = com.nonameradio.app.core.utils.AsyncExecutor.submitIOTask(() -> {
            Context context = contextWeakReference.get();
            if (context == null) {
                throw new IllegalStateException("Context is null");
            }

            NoNameRadioApp application = (NoNameRadioApp) context.getApplicationContext();

            // Refresh station UUID if needed
            if (!stationToPlay.hasValidUuid()) {
                if (!stationToPlay.refresh(application.getHttpClient(), context)) {
                    throw new IllegalStateException("Failed to refresh station UUID");
                }
            }

            // Resolve real station link
            String realLink = Utils.getRealStationLink(application.getHttpClient(), 
                                                      context.getApplicationContext(), 
                                                      stationToPlay.StationUuid);
            if (realLink == null || realLink.isEmpty()) {
                throw new IllegalStateException("Failed to resolve station URL");
            }

            return realLink;
        });

        // Handle successful URL resolution
        future.thenAccept(resolvedUrl -> {
            com.nonameradio.app.core.utils.UiHandler.post(() -> {
                Context context = contextWeakReference.get();
                if (context == null) {
                    return;
                }

                EventBus.post(HideLoadingEvent.INSTANCE);

                // Set playable URL and start playback
                stationToPlay.playableUrl = resolvedUrl;
                playFunc.play(resolvedUrl);

                if (postExecuteTask != null) {
                    postExecuteTask.onPostExecute(ExecutionResult.SUCCESS);
                }
            });
        }).exceptionally(throwable -> {
            // Handle errors on main thread
            com.nonameradio.app.core.utils.UiHandler.post(() -> {
                Context context = contextWeakReference.get();
                if (context == null) {
                    return;
                }

                EventBus.post(HideLoadingEvent.INSTANCE);

                Log.e(TAG, "Error loading station", throwable);

                Toast toast = Toast.makeText(context.getApplicationContext(),
                        context.getResources().getText(R.string.error_station_load), 
                        Toast.LENGTH_SHORT);
                toast.show();

                if (postExecuteTask != null) {
                    postExecuteTask.onPostExecute(ExecutionResult.FAILURE);
                }
            });

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