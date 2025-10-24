package com.nonameradio.app.players.exoplayer;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import androidx.media3.common.C;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.Metadata;
import androidx.media3.extractor.metadata.icy.IcyHeaders;
import androidx.media3.extractor.metadata.icy.IcyInfo;
import androidx.media3.extractor.metadata.id3.Id3Frame;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.TransferListener;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy;
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy.LoadErrorInfo;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import com.nonameradio.app.BuildConfig;
import com.nonameradio.app.R;
import com.nonameradio.app.Utils;
import com.nonameradio.app.players.PlayState;
import com.nonameradio.app.players.PlayerWrapper;
import com.nonameradio.app.recording.RecordableListener;
import com.nonameradio.app.station.live.ShoutcastInfo;
import com.nonameradio.app.station.live.StreamLiveInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ExoPlayerWrapper implements PlayerWrapper, Player.Listener {

    final private String TAG = "ExoPlayerWrapper";

    private ExoPlayer player;
    private PlayListener stateListener;

    private String streamUrl;

    //private DefaultBandwidthMeter bandwidthMeter;

    private final RecordingCoordinator recordingCoordinator = new RecordingCoordinator();

    private long totalTransferredBytes;
    private long currentPlaybackTransferredBytes;

    private Context context;
    private boolean isHls;
    private boolean isPlayingFlag;

    private Handler playerThreadHandler;

    private long lastPlayStartTime = 0;

    private Runnable fullStopTask;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    public void playRemote(@NonNull OkHttpClient httpClient, @NonNull String streamUrl, @NonNull Context context, boolean isAlarm) {
        // I don't know why, but it is still possible that streamUrl is null,
        // I still get exceptions from this from google
        if (!streamUrl.equals(this.streamUrl)) {
            currentPlaybackTransferredBytes = 0;
        }

        this.context = context;
        this.streamUrl = streamUrl;

        cancelStopTask();

        stateListener.onStateChanged(PlayState.PrePlaying);

        if (player != null) {
            player.stop();
        }

        // Always recreate player with our custom MediaSourceFactory for recording
        if (player != null) {
            player.release();
        }

        // Create OkHttpDataSource.Factory with custom OkHttpClient
        OkHttpDataSource.Factory httpDataSourceFactory = new OkHttpDataSource.Factory(httpClient)
                .setUserAgent("NoNameRadio");

        DataSource.Factory recordingAwareFactory = new RecordingDataSourceFactory(httpDataSourceFactory);

        // Create DefaultMediaSourceFactory with our recording-aware data source
        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(recordingAwareFactory)
                .setLoadErrorHandlingPolicy(new CustomLoadErrorHandlingPolicy());

        // Create player with our custom MediaSourceFactory
        player = new ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();

        player.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(isAlarm ? C.USAGE_ALARM : C.USAGE_MEDIA).build(), false);

        player.addListener(this);
        player.addAnalyticsListener(new AnalyticEventListener());

        if (playerThreadHandler == null) {
            playerThreadHandler = new Handler(Looper.getMainLooper());
        }

        isHls = Utils.urlIndicatesHlsStream(streamUrl);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        final int retryTimeout = prefs.getInt("settings_retry_timeout", 10);
        final int retryDelay = prefs.getInt("settings_retry_delay", 100);

        // Create MediaItem and set it directly to player
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(streamUrl));

        player.setMediaItem(mediaItem);
        player.prepare();
                player.setPlayWhenReady(true);
                
                // Record play start time
                lastPlayStartTime = System.currentTimeMillis();
                
                // Report playback start event
                try {
                    YandexMetrica.reportEvent("radio_playback_started", "{\"url\":\"" + streamUrl + "\",\"is_hls\":" + isHls + "}");
                } catch (Exception e) {
                    Log.w("ExoPlayerWrapper", "Failed to report playback start event", e);
                }

        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        if (networkCallback == null && connectivityManager != null) {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                    .build();

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    if (fullStopTask != null && player != null && Utils.hasAnyConnection(context)) {
                        Log.i(TAG, "Regained connection. Resuming playback.");
                        cancelStopTask();
                        // Recreate MediaItem and resume playback
                        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(streamUrl));
                        player.setMediaItem(mediaItem);
                        player.prepare();
                        player.setPlayWhenReady(true);
                    }
                }
            };

            connectivityManager.registerNetworkCallback(request, networkCallback);
        }

        // State changed will be called when audio session id is available.
    }

    @Override
    public void pause() {
        Log.i(TAG, "Pause. Stopping exoplayer.");

        cancelStopTask();
        recordingCoordinator.stop(false, false);

        if (player != null) {
            if (connectivityManager != null && networkCallback != null) {
                try { connectivityManager.unregisterNetworkCallback(networkCallback); } catch (Exception ignored) {}
                networkCallback = null;
            }
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    public void stop() {
        Log.i(TAG, "Stopping exoplayer.");
        
        // Report playback stop event
        try {
            YandexMetrica.reportEvent("radio_playback_stopped");
        } catch (Exception e) {
            Log.w("ExoPlayerWrapper", "Failed to report playback stop event", e);
        }

        cancelStopTask();
        recordingCoordinator.stop(false, false);

        if (player != null) {
            if (connectivityManager != null && networkCallback != null) {
                try { connectivityManager.unregisterNetworkCallback(networkCallback); } catch (Exception ignored) {}
                networkCallback = null;
            }
            player.stop();
            player.release();
            player = null;
        }

        recordingCoordinator.stop(false, false);
    }

    @Override
    public boolean isPlaying() {
        return player != null && isPlayingFlag;
    }

    @Override
    public long getBufferedMs() {
        if (player != null) {
            return (int) (player.getBufferedPosition() - player.getCurrentPosition());
        }

        return 0;
    }

    @Override
    public int getAudioSessionId() {
        if (player != null) {
            return player.getAudioSessionId();
        }
        return 0;
    }

    @Override
    public long getTotalTransferredBytes() {
        return totalTransferredBytes;
    }

    @Override
    public long getCurrentPlaybackTransferredBytes() {
        return currentPlaybackTransferredBytes;
    }

    @Override
    public String getCurrentRecordFileName() {
        // For now, return null - recording filename not tracked in ExoPlayer
        return null;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public void setVolume(float newVolume) {
        if (player != null) {
            player.setVolume(newVolume);
        }
    }

    @Override
    public void setStateListener(PlayListener listener) {
        stateListener = listener;
    }

    @Override
    public void onMetadata(@NonNull Metadata metadata) {
        if (BuildConfig.DEBUG) Log.d(TAG, "META: " + metadata);
        final int length = metadata.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                final Metadata.Entry entry = metadata.get(i);
                if (entry == null) {
                    continue;
                }
                if (entry instanceof IcyInfo icyInfo) {
                    Log.d(TAG, "IcyInfo: " + icyInfo);
                    if (icyInfo.title != null) {
                        Map<String, String> rawMetadata = new HashMap<>() {{
                            put("StreamTitle", icyInfo.title);
                        }};
                        StreamLiveInfo streamLiveInfo = new StreamLiveInfo(rawMetadata);
                        if (stateListener != null) {
                            stateListener.onDataSourceStreamLiveInfo(streamLiveInfo);
                        }
                    }
                } else if (entry instanceof IcyHeaders icyHeaders) {
                    Log.d(TAG, "IcyHeaders: " + icyHeaders);
                    ShoutcastInfo shoutcastInfo = new ShoutcastInfo(icyHeaders);
                    if (stateListener != null) {
                        stateListener.onDataSourceShoutcastInfo(shoutcastInfo, isHls);
                    }
                } else if (entry instanceof Id3Frame id3Frame) {
                    Log.d(TAG, "id3 metadata: " + id3Frame);
                }
            }
        }
    }

    void resumeWhenNetworkConnected() {
        playerThreadHandler.post(() -> {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            int resumeWithin = sharedPref.getInt("settings_resume_within", 60);
            if (resumeWithin > 0) {
                Log.d(TAG, "Trying to resume playback within " + resumeWithin + "s.");

                // We want user to be able to paused during connection loss.
                // TODO: Find a way to notify user that even if current state is Playing
                //       we are actually trying to reconnect.
                //stateListener.onStateChanged(PlayState.Paused);

                cancelStopTask();

                fullStopTask = () -> {
                    stop();
                    stateListener.onPlayerError(R.string.giving_up_resume);

                    ExoPlayerWrapper.this.fullStopTask = null;
                };
                playerThreadHandler.postDelayed(fullStopTask, resumeWithin * 1000L);

                stateListener.onPlayerWarning(R.string.warning_no_network_trying_resume);
            } else {
                stop();

                stateListener.onPlayerError(R.string.error_stream_reconnect_timeout);
            }
        });
    }

    @Override
    public boolean canRecord() {
        return player != null;
    }

    @Override
    public void startRecording(@NonNull RecordableListener recordableListener) {
        if (player == null) {
            Log.w(TAG, "startRecording: player not ready");
            recordableListener.onRecordingEnded();
            return;
        }

        recordingCoordinator.start(recordableListener);
    }

    @Override
    public void stopRecording() {
        recordingCoordinator.stop(false, false);
    }

    @Override
    public boolean isRecording() {
        return recordingCoordinator.isActive();
    }

    @Override
    public Map<String, String> getRecordNameFormattingArgs() {
        return null;
    }

    @Override
    public String getExtension() {
        return isHls ? "ts" : "mp3";
    }

    private void cancelStopTask() {
        if (fullStopTask != null) {
            playerThreadHandler.removeCallbacks(fullStopTask);
            fullStopTask = null;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        // Do nothing
    }

    @Override
    public void onPlayerErrorChanged(PlaybackException error) {
        Log.d(TAG, "Player error: ", error);
        // Stop playing since it is either irrecoverable error in the player or our data source failed to reconnect.
        if (fullStopTask != null) {
            stop();
            stateListener.onPlayerError(R.string.error_play_stream);
        }
    }

    @Override
    public void onPlaybackParametersChanged(@NonNull PlaybackParameters playbackParameters) {
        // Do nothing
    }

    final class CustomLoadErrorHandlingPolicy extends DefaultLoadErrorHandlingPolicy {
        final int MIN_RETRY_DELAY_MS = 10;
        final int MAX_RETRY_DELAY_MS = 30000; // 30 seconds max
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        // We need to read the retry delay here on each error again because the user might change
        // this value between retries and experiment with different vales to get the best result for
        // the specific situation. We also need to make sure that a sensible minimum value is chosen.
        int getSanitizedRetryDelaySettingsMs() {
            return Math.max(sharedPrefs.getInt("settings_retry_delay", 100), MIN_RETRY_DELAY_MS);
        }

        @Override
        public long getRetryDelayMsFor(LoadErrorInfo loadErrorInfo) {
            int retryDelay = getSanitizedRetryDelaySettingsMs();
            IOException exception = loadErrorInfo.exception;
            int errorCount = loadErrorInfo.errorCount;

            // Exponential backoff with jitter for better retry behavior
            if (errorCount > 1) {
                retryDelay = Math.min(retryDelay * (1 << Math.min(errorCount - 1, 8)), MAX_RETRY_DELAY_MS);
                // Add jitter to prevent thundering herd
                retryDelay = retryDelay + (int)(Math.random() * retryDelay * 0.1);
            }

            if (exception instanceof androidx.media3.datasource.HttpDataSource.InvalidContentTypeException) {
                stateListener.onPlayerError(R.string.error_play_stream);
                return C.TIME_UNSET; // Immediately surface error if we cannot play content type
            }

            if (!Utils.hasAnyConnection(context)) {
                int resumeWithinS = sharedPrefs.getInt("settings_resume_within", 60);
                if (resumeWithinS > 0) {
                    resumeWhenNetworkConnected();
                    retryDelay = 1000 * resumeWithinS + retryDelay;
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Providing retry delay of " + retryDelay + "ms " +
                        "error count: " + errorCount + ", " +
                        "exception " + exception.getClass() + ", " +
                        "message: " + exception.getMessage());
            }
            return retryDelay;
        }

        @Override
        public int getMinimumLoadableRetryCount(int dataType) {
            // Increase retry count for better resilience
            int baseRetries = sharedPrefs.getInt("settings_retry_timeout", 10) * 1000 / getSanitizedRetryDelaySettingsMs() + 1;
            return Math.min(baseRetries * 2, 50); // Cap at 50 retries
        }
    }

    private class AnalyticEventListener implements AnalyticsListener {
        @Override
        public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
            isPlayingFlag = playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING;

            switch (playbackState) {
                case Player.STATE_READY:
                    cancelStopTask();
                    stateListener.onStateChanged(PlayState.Playing);
                    break;
                case Player.STATE_BUFFERING:
                    stateListener.onStateChanged(PlayState.PrePlaying);
                    break;
            }

        }

        @Override
        public void onTimelineChanged(@NonNull EventTime eventTime, int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(@NonNull EventTime eventTime, @NonNull PlaybackParameters playbackParameters) {

        }

        @Override
        public void onRepeatModeChanged(@NonNull EventTime eventTime, int repeatMode) {

        }

        @Override
        public void onShuffleModeChanged(@NonNull EventTime eventTime, boolean shuffleModeEnabled) {

        }

        @Override
        public void onBandwidthEstimate(@NonNull EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {

        }

        @Override
        public void onSurfaceSizeChanged(@NonNull EventTime eventTime, int width, int height) {

        }

        @Override
        public void onMetadata(@NonNull EventTime eventTime, @NonNull Metadata metadata) {

        }

        @Override
        public void onAudioAttributesChanged(@NonNull EventTime eventTime, @NonNull AudioAttributes audioAttributes) {

        }

        @Override
        public void onVolumeChanged(@NonNull EventTime eventTime, float volume) {

        }

        @Override
        public void onDroppedVideoFrames(@NonNull EventTime eventTime, int droppedFrames, long elapsedMs) {

        }

        @Override
        public void onDrmKeysLoaded(@NonNull EventTime eventTime) {

        }

        @Override
        public void onDrmSessionManagerError(@NonNull EventTime eventTime, @NonNull Exception error) {

        }

        @Override
        public void onDrmKeysRestored(@NonNull EventTime eventTime) {

        }

        @Override
        public void onDrmKeysRemoved(@NonNull EventTime eventTime) {

        }

        @Override
        public void onDrmSessionReleased(@NonNull EventTime eventTime) {

        }
    }

    @Override
    public boolean isHls() {
        return isHls;
    }

    @Override
    public long getLastPlayStartTime() {
        return lastPlayStartTime;
    }

    /**
     * NetworkCallback for modern network monitoring
     */
    private class NetworkStateCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull Network network) {
            Log.d(TAG, "Network available: " + network);
            // Network is available, ExoPlayer will automatically retry
        }

        @Override
        public void onLost(@NonNull Network network) {
            Log.d(TAG, "Network lost: " + network);
            // Network lost, ExoPlayer will handle retry through CustomLoadErrorHandlingPolicy
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            Log.d(TAG, "Network capabilities changed: " + network + ", capabilities: " + networkCapabilities);
            // Network capabilities changed, ExoPlayer will handle accordingly
        }
    }

    private class RecordingDataSourceFactory implements DataSource.Factory {
        private final DataSource.Factory upstreamFactory;

        RecordingDataSourceFactory(DataSource.Factory upstreamFactory) {
            this.upstreamFactory = upstreamFactory;
        }

        @Override
        public DataSource createDataSource() {
            return new RecordingDataSource(upstreamFactory.createDataSource());
        }
    }

    private class RecordingDataSource implements DataSource {
        private final DataSource upstream;

        RecordingDataSource(DataSource upstream) {
            this.upstream = upstream;
        }

        @Override
        public void addTransferListener(TransferListener transferListener) {
            upstream.addTransferListener(transferListener);
        }

        @Override
        public long open(DataSpec dataSpec) throws IOException {
            return upstream.open(dataSpec);
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int bytesRead = upstream.read(buffer, offset, length);
            if (bytesRead > 0) {
                recordingCoordinator.onBytesRead(buffer, offset, bytesRead);
            }
            return bytesRead;
        }

        @Override
        public Uri getUri() {
            return upstream.getUri();
        }

        @Override
        public void close() throws IOException {
            try {
                recordingCoordinator.stop(false, true);
            } finally {
                upstream.close();
            }
        }

        @Override
        public Map<String, List<String>> getResponseHeaders() {
            return upstream.getResponseHeaders();
        }
    }

    private class RecordingCoordinator {
        private static final long MAX_DURATION_MS = TimeUnit.MINUTES.toMillis(60);

        private final Handler handler = new Handler(Looper.getMainLooper());

        private RecordableListener listener;
        private long startElapsedMs;
        private boolean active;
        private final Runnable timeoutRunnable = this::handleTimeout;

        void start(@NonNull RecordableListener newListener) {
            if (active) {
                Log.w(TAG, "Recording already active, restarting");
                stop(false, true);
            }

            listener = newListener;
            startElapsedMs = SystemClock.elapsedRealtime();
            active = true;
            handler.postDelayed(timeoutRunnable, MAX_DURATION_MS);
        }

        void stop(boolean dueToLimit) {
            if (!active) {
                return;
            }

            handler.removeCallbacks(timeoutRunnable);

            RecordableListener finishingListener = listener;
            listener = null;
            active = false;

            deliverStopCallback(finishingListener, dueToLimit);
        }

        void stop(boolean dueToLimit, boolean suppressedListener) {
            if (!active) {
                return;
            }

            handler.removeCallbacks(timeoutRunnable);

            RecordableListener finishingListener = listener;
            listener = null;
            active = false;

            if (!suppressedListener) {
                deliverStopCallback(finishingListener, dueToLimit);
            } else if (dueToLimit && stateListener != null) {
                stateListener.onPlayerWarning(R.string.recording_limit_reached);
            }
        }

        boolean isActive() {
            return active;
        }

        void onBytesRead(byte[] buffer, int offset, int length) {
            if (!active || listener == null) {
                return;
            }

            long elapsed = SystemClock.elapsedRealtime() - startElapsedMs;
            if (elapsed >= MAX_DURATION_MS) {
                stop(true);
                return;
            }

            try {
                listener.onBytesAvailable(buffer, offset, length);
            } catch (Exception e) {
                Log.e(TAG, "Recording write failed", e);
                stop(false);
            }
        }

        private void handleTimeout() {
            stop(true);
        }

        private void deliverStopCallback(RecordableListener finishingListener, boolean dueToLimit) {
            if (finishingListener != null) {
                try {
                    finishingListener.onRecordingEnded();
                } catch (Exception e) {
                    Log.e(TAG, "Recording end callback failed", e);
                }
            }

            if (dueToLimit && stateListener != null) {
                stateListener.onPlayerWarning(R.string.recording_limit_reached);
            }
        }
    }
}
