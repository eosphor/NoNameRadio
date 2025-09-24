package com.nonameradio.app.players;
import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.core.event.ShowLoadingEvent;
import com.nonameradio.app.core.event.HideLoadingEvent;
import com.nonameradio.app.core.event.EventBus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.nonameradio.app.ActivityMain;
import com.nonameradio.app.CastHandler;
import com.nonameradio.app.FavouriteManager;
import com.nonameradio.app.HistoryManager;
import com.nonameradio.app.R;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.Utils;
import com.nonameradio.app.players.mpd.MPDClient;
import com.nonameradio.app.players.mpd.MPDServerData;
import com.nonameradio.app.players.mpd.tasks.MPDPlayTask;
import com.nonameradio.app.station.DataRadioStation;

import java.lang.ref.WeakReference;

public class PlayStationTask extends AsyncTask<Void, Void, String> {
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

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Context ctx = contextWeakReference.get();
        if (ctx == null) {
            return;
        }

        EventBus.post(ShowLoadingEvent.INSTANCE);

        NoNameRadioApp app = (NoNameRadioApp) ctx.getApplicationContext();

        HistoryManager historyManager = app.getHistoryManager();
        historyManager.add(stationToPlay);

        // Do not auto-add to favourites on play. Favourite should change only on explicit heart click.
    }

    @Override
    protected String doInBackground(Void... params) {
        Context ctx = contextWeakReference.get();
        if (ctx != null) {
            NoNameRadioApp app = (NoNameRadioApp) ctx.getApplicationContext();

            if (!stationToPlay.hasValidUuid()) {
                if (!stationToPlay.refresh(app.getHttpClient(), ctx)) {
                    return null;
                }
            }

            if (isCancelled()) {
                return null;
            }

            return Utils.getRealStationLink(app.getHttpClient(), ctx.getApplicationContext(), stationToPlay.StationUuid);
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Context ctx = contextWeakReference.get();
        if (ctx == null) {
            return;
        }

        EventBus.post(HideLoadingEvent.INSTANCE);

        if (result != null) {
            stationToPlay.playableUrl = result;
            playFunc.play(result);
        } else {
            Toast toast = Toast.makeText(ctx.getApplicationContext(),
                    ctx.getResources()
                            .getText(R.string.error_station_load), Toast.LENGTH_SHORT);
            toast.show();
        }

        if (postExecuteTask != null) {
            postExecuteTask.onPostExecute(result != null ? ExecutionResult.SUCCESS : ExecutionResult.FAILURE);
        }

        super.onPostExecute(result);
    }
}