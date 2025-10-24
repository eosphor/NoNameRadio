package com.nonameradio.app.service;

import android.content.ContentResolver;
import static androidx.media.utils.MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE;
import static androidx.media.utils.MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE;
import static androidx.media.utils.MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM;
import static androidx.media.utils.MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.request.target.CustomTarget;
import com.nonameradio.app.utils.ImageLoader;

import com.nonameradio.app.R;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.Utils;
import com.nonameradio.app.station.DataRadioStation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import static com.nonameradio.app.Utils.resourceToUri;


public class NoNameRadioBrowser {
    private static final String TAG = "NoNameRadioBrowser";
    private static final String MEDIA_ID_ROOT = "__ROOT__";
    private static final String MEDIA_ID_MUSICS_FAVORITE = "__FAVORITE__";
    private static final String MEDIA_ID_MUSICS_HISTORY = "__HISTORY__";
    private static final String MEDIA_ID_MUSICS_TOP = "__TOP__";
    private static final String MEDIA_ID_MUSICS_TOP_TAGS = "__TOP_TAGS__";

    private static final char LEAF_SEPARATOR = '|';

    private static final int IMAGE_LOAD_TIMEOUT_MS = 2000;

    private final NoNameRadioApp app;

    private final Map<String, DataRadioStation> stationIdToStation = new HashMap<>();

    private static class RetrieveStationsIconAndSendResult {
        private final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result;
        private final List<DataRadioStation> stations;
        private final WeakReference<Context> contextRef;
        private final Resources resources;

        private final Map<String, Bitmap> stationIdToIcon = new HashMap<>();
        private CountDownLatch countDownLatch;
        private final List<ImageLoader.BitmapTarget> imageLoadTargets = new ArrayList<>();

        RetrieveStationsIconAndSendResult(MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result, 
                                         List<DataRadioStation> stations, Context context) {
            this.result = result;
            this.stations = stations;
            this.contextRef = new WeakReference<>(context);
            this.resources = context.getApplicationContext().getResources();
        }

        CompletableFuture<Void> executeAsync() {
            Context context = contextRef.get();
            if (context == null) {
                return CompletableFuture.completedFuture(null);
            }

            // Initialize countdown latch for all stations
            countDownLatch = new CountDownLatch(stations.size());

            // Start loading icons for all stations (async via Glide)
            for (final DataRadioStation station : stations) {
                ImageLoader.BitmapTarget imageLoadTarget = new ImageLoader.BitmapTarget() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap) {
                        stationIdToIcon.put(station.StationUuid, bitmap);
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        if (errorDrawable instanceof BitmapDrawable) {
                            onBitmapLoaded(((BitmapDrawable) errorDrawable).getBitmap());
                        } else {
                            countDownLatch.countDown();
                        }
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        // No action needed
                    }
                };
                imageLoadTargets.add(imageLoadTarget);
                
                String iconUrl = station.hasIcon() ? station.IconUrl : resourceToUri(resources, R.drawable.ic_launcher).toString();
                ImageLoader.loadStationIconForBrowser(context, iconUrl, 128, Utils.useCircularIcons(context), imageLoadTarget);
            }

            // Wait for all images to load in background thread
            return com.nonameradio.app.core.utils.AsyncExecutor.submitIOTask(() -> {
                try {
                    countDownLatch.await(IMAGE_LOAD_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Log.w(TAG, "Image loading interrupted", e);
                    Thread.currentThread().interrupt();
                }
                return null;
            })
            .thenRun(() -> {
                // Build and send result on main thread
                com.nonameradio.app.core.utils.UiHandler.post(() -> {
                    sendResult();
                });
            });
        }

        private void sendResult() {
            Context context = contextRef.get();
            if (context == null) {
                result.sendResult(new ArrayList<>());
                return;
            }

            List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

            for (DataRadioStation station : stations) {
                Bitmap stationIcon = stationIdToIcon.get(station.StationUuid);

                if (stationIcon == null) {
                    stationIcon = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_launcher);
                }
                
                Bundle extras = new Bundle();
                extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, stationIcon);
                extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, stationIcon);
                
                MediaDescriptionCompat.Builder mediaItem = new MediaDescriptionCompat.Builder()
                        .setMediaId(MEDIA_ID_MUSICS_HISTORY + LEAF_SEPARATOR + station.StationUuid)
                        .setTitle(station.Name)
                        .setDescription(station.Country + " " + station.Country + " " + station.TagsAll)
                        .setExtras(extras);

                if (station.IconUrl != null && !station.IconUrl.isEmpty()) {
                    String iconUrl = station.IconUrl;
                    if (iconUrl.startsWith("http:")) {
                        iconUrl = iconUrl.replace("http:", "https:");
                    }
                    mediaItem.setIconUri(Uri.parse(iconUrl));
                } else {
                    mediaItem.setIconUri(resourceToUri(resources, R.drawable.ic_photo_24dp));
                }

                mediaItems.add(new MediaBrowserCompat.MediaItem(mediaItem.build(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
            }

            result.sendResult(mediaItems);
        }
    }

    public NoNameRadioBrowser(NoNameRadioApp app) {
        this.app = app;
    }

   @Nullable
    public MediaBrowserServiceCompat.BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
       SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext().getApplicationContext());
       Bundle extras = new Bundle();
       extras.putInt(DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE, DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM);
       if (sharedPref.getBoolean("load_icons", false) && sharedPref.getBoolean("icons_only_favorites_style", false)) {
           Log.d(TAG, "Setting grid style for playables");
           extras.putInt(
                   DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
                   DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM);
       } else {
           Log.d(TAG, "Setting list style for playables");
           extras.putInt(
                   DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
                   DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM);
       }
//       extras.putBoolean(CONTENT_STYLE_SUPPORTED, true);
       return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_ROOT, extras);
    }

    public void onLoadChildren(@NonNull String parentId, @NonNull MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        Resources resources = app.getResources();
        if (MEDIA_ID_ROOT.equals(parentId)) {
            result.sendResult(createBrowsableMediaItemsForRoot(resources));
            return;
        }

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        List<DataRadioStation> stations = null;

        switch (parentId) {
            case MEDIA_ID_MUSICS_FAVORITE: {
                stations = app.getFavouriteManager().getList();
                break;
            }
            case MEDIA_ID_MUSICS_HISTORY: {
                stations = app.getHistoryManager().getList();
                break;
            }
            case MEDIA_ID_MUSICS_TOP: {

                break;
            }
        }

        if (stations != null && !stations.isEmpty()) {
            stationIdToStation.clear();
            for (DataRadioStation station : stations) {
                stationIdToStation.put(station.StationUuid, station);
            }
            result.detach();
            new RetrieveStationsIconAndSendResult(result, stations, app).executeAsync();
        } else {
            result.sendResult(mediaItems);
        }

    }

    @Nullable
    public DataRadioStation getStationById(@NonNull String stationId) {
        return stationIdToStation.get(stationId);
    }

    private List<MediaBrowserCompat.MediaItem> createBrowsableMediaItemsForRoot(Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        mediaItems.add(new MediaBrowserCompat.MediaItem(new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_MUSICS_FAVORITE)
                .setTitle(resources.getString(R.string.nav_item_starred))
                .setIconUri(resourceToUri(resources, R.drawable.ic_heart_24dp))
                .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

        mediaItems.add(new MediaBrowserCompat.MediaItem(new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_MUSICS_HISTORY)
                .setTitle(resources.getString(R.string.nav_item_history))
                .setIconUri(resourceToUri(resources, R.drawable.ic_heart_24dp))
                .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

/*        mediaItems.add(new MediaBrowserCompat.MediaItem(new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_MUSICS_TOP)
                .setTitle(resources.getString(R.string.action_top_click))
                .setIconUri(resourceToUri(resources, R.drawable.ic_restore_black_24dp))
                .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));*/
        return mediaItems;
    }

    public static String stationIdFromMediaId(final String mediaId) {
        if (mediaId == null) {
            return "";
        }

        final int separatorIdx = mediaId.indexOf(LEAF_SEPARATOR);

        if (separatorIdx <= 0) {
            return mediaId;
        }

        return mediaId.substring(separatorIdx + 1);
    }
}
