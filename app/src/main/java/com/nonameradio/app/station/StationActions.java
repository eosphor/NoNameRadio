package com.nonameradio.app.station;
import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.core.event.HideLoadingEvent;
import com.nonameradio.app.core.event.EventBus;

import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import com.nonameradio.app.ActivityMain;
import com.nonameradio.app.FavouriteManager;
import com.nonameradio.app.R;
import com.nonameradio.app.RadioBrowserServerManager;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.Utils;
import com.nonameradio.app.alarm.TimePickerFragment;
import com.nonameradio.app.players.selector.PlayerType;
import com.nonameradio.app.views.ItemListDialog;

import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;

public class StationActions {
    private static final String TAG = "StationActions";

    public static void setAsAlarm(final @NonNull FragmentActivity activity, final @NonNull DataRadioStation station) {
        final NoNameRadioApp app = (NoNameRadioApp) activity.getApplicationContext();

        final TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setCallback((timePicker, hourOfDay, minute) -> {
            Log.i(TAG, String.format("Alarm time picked %d:%d", hourOfDay, minute));
            app.getAlarmManager().add(station, hourOfDay, minute);
        });
        newFragment.show(activity.getSupportFragmentManager(), "timePicker");
    }

    public static void showWebLinks(final @NonNull FragmentActivity activity, final @NonNull DataRadioStation station) {
        ItemListDialog.create(activity, new int[]{
                R.string.action_station_visit_website, R.string.action_station_copy_stream_url, R.string.action_station_share
        }, resourceId -> {
            switch (resourceId) {
                case R.string.action_station_visit_website: {
                    openStationHomeUrl(activity, station);
                    break;
                }
                case R.string.action_station_copy_stream_url: {
                    retrieveAndCopyStreamUrlToClipboard(activity, station);
                    break;
                }
                case R.string.action_station_share: {
                    share(activity, station);
                    break;
                }
            }
        }).show();
    }

    static void openStationHomeUrl(final @NonNull FragmentActivity activity, final @NonNull DataRadioStation station) {
        if (!TextUtils.isEmpty(station.HomePageUrl)) {
            Uri stationUrl = Uri.parse(station.HomePageUrl);
            if (stationUrl != null) {
                Intent newIntent = new Intent(Intent.ACTION_VIEW, stationUrl);
                activity.startActivity(newIntent);
            }
        }
    }

    private static void retrieveAndCopyStreamUrlToClipboard(final @NonNull Context context, final @NonNull DataRadioStation station) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ActivityMain.ACTION_SHOW_LOADING));

        final WeakReference<Context> contextRef = new WeakReference<>(context);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Context ctx = contextRef.get();
                if (ctx == null) {
                    return null;
                }

                final NoNameRadioApp app = (NoNameRadioApp) ctx.getApplicationContext();
                final OkHttpClient httpClient = app.getHttpClient();

                return Utils.getRealStationLink(httpClient, app, station.StationUuid);
            }

            @Override
            protected void onPostExecute(String result) {
                Context ctx = contextRef.get();
                if (ctx == null) {
                    super.onPostExecute(result);
                    return;
                }

                EventBus.post(HideLoadingEvent.INSTANCE);

                if (result != null) {
                    ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        ClipData clip = ClipData.newPlainText("Stream Url", result);
                        clipboard.setPrimaryClip(clip);

                        CharSequence toastText = ctx.getResources().getText(R.string.notify_stream_url_copied);
                        Toast.makeText(ctx.getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Clipboard is NULL!");
                        // TODO: toast general error
                    }
                } else {
                    CharSequence toastText = ctx.getResources().getText(R.string.error_station_load);
                    Toast.makeText(ctx.getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                }
                super.onPostExecute(result);
            }
        }.execute();
    }

    public static void markAsFavourite(final @NonNull Context context, final @NonNull DataRadioStation station) {
        final NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();
        app.getFavouriteManager().add(station);

        Toast toast = Toast.makeText(context, context.getString(R.string.notify_starred), Toast.LENGTH_SHORT);
        toast.show();

        vote(context, station);
    }

    public static void removeFromFavourites(final @NonNull Context context, final @Nullable View view, final @NonNull DataRadioStation station) {
        final NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();
        final FavouriteManager favouriteManager = app.getFavouriteManager();
        final int removedIdx = favouriteManager.remove(station.StationUuid);

        if (view != null) {
            final View viewAttachTo = view.getRootView().findViewById(R.id.fragment_player_small);

            Snackbar snackbar = Snackbar
                    .make(viewAttachTo, R.string.notify_station_removed_from_list, 6000);
            snackbar.setAnchorView(viewAttachTo);
            snackbar.setAction(R.string.action_station_removed_from_list_undo, view1 -> favouriteManager.restore(station, removedIdx));
            snackbar.show();
        }
    }

    public static void share(final @NonNull Context context, final @NonNull DataRadioStation station) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ActivityMain.ACTION_SHOW_LOADING));

        final WeakReference<Context> contextRef = new WeakReference<>(context);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Context ctx = contextRef.get();
                if (ctx == null) {
                    return null;
                }

                final NoNameRadioApp app = (NoNameRadioApp) ctx.getApplicationContext();
                final OkHttpClient httpClient = app.getHttpClient();

                return Utils.getRealStationLink(httpClient, app, station.StationUuid);
            }

            @Override
            protected void onPostExecute(String result) {
                Context ctx = contextRef.get();
                if (ctx == null) {
                    return;
                }

                EventBus.post(HideLoadingEvent.INSTANCE);

                if (result != null) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_SUBJECT, station.Name);
                    share.putExtra(Intent.EXTRA_TEXT, result);
                    String title = ctx.getResources().getString(R.string.share_action);
                    Intent chooser = Intent.createChooser(share, title);

                    ctx.startActivity(chooser);
                } else {
                    Toast toast = Toast.makeText(ctx.getApplicationContext(), ctx.getResources().getText(R.string.error_station_load), Toast.LENGTH_SHORT);
                    toast.show();
                }
                super.onPostExecute(result);
            }
        }.execute();
    }

    public static void playInNoNameRadio(final @NonNull Context context, final @NonNull DataRadioStation station) {
        NoNameRadioApp app = (NoNameRadioApp) context.getApplicationContext();

        Utils.playAndWarnIfMetered(app, station,
                PlayerType.INTERNAL, () -> Utils.play(app, station));
    }

    private static void vote(final @NonNull Context context, final @NonNull DataRadioStation station) {
        final WeakReference<Context> contextRef = new WeakReference<>(context);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Context ctx = contextRef.get();
                if (ctx == null) {
                    return null;
                }

                final NoNameRadioApp app = (NoNameRadioApp) ctx.getApplicationContext();
                final OkHttpClient httpClient = app.getHttpClient();

                return Utils.downloadFeedRelative(httpClient, ctx, "json/vote/" + station.StationUuid, true, null);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
            }
        }.execute();
    }
}
