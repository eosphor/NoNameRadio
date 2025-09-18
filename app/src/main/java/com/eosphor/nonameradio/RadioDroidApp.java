package com.eosphor.nonameradio;

import android.app.UiModeManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import coil.Coil;
import coil.ImageLoader;
import coil.request.CachePolicy;

import dagger.hilt.android.HiltAndroidApp;

import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.AppMetricaConfig;

import com.eosphor.nonameradio.alarm.RadioAlarmManager;
import com.eosphor.nonameradio.history.TrackHistoryRepository;
import com.eosphor.nonameradio.players.mpd.MPDClient;
import com.eosphor.nonameradio.station.live.metadata.TrackMetadataSearcher;
import com.eosphor.nonameradio.proxy.ProxySettings;
import com.eosphor.nonameradio.recording.RecordingsManager;
import com.eosphor.nonameradio.utils.TvChannelManager;
import com.eosphor.nonameradio.repository.PlayStationRepository;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@HiltAndroidApp
public class RadioDroidApp extends MultiDexApplication {

    private HistoryManager historyManager;
    private FavouriteManager favouriteManager;
    private RecordingsManager recordingsManager;
    private FallbackStationsManager fallbackStationsManager;
    private RadioAlarmManager alarmManager;
    private TvChannelManager tvChannelManager;

    private TrackHistoryRepository trackHistoryRepository;

    private MPDClient mpdClient;

    private CastHandler castHandler;

    private TrackMetadataSearcher trackMetadataSearcher;

    private PlayStationRepository playStationRepository;

    private ConnectionPool connectionPool;
    private OkHttpClient httpClient;

    private Interceptor testsInterceptor;

    public class UserAgentInterceptor implements Interceptor {

        private final String userAgent;

        public UserAgentInterceptor(String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", userAgent)
                    .build();
            return chain.proceed(requestWithUserAgent);
        }
    }

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Инициализация AppMetrica
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder("620825a5-2d14-47ce-af59-acb3618c547e")
                .withLogs()
                .build();
        AppMetrica.activate(this, config);

        GoogleProviderHelper.use(getBaseContext());

        connectionPool = new ConnectionPool();

        rebuildHttpClient();

        // Инициализация Coil
        ImageLoader imageLoader = new ImageLoader.Builder(this)
                .okHttpClient(httpClient)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build();
        Coil.setImageLoader(imageLoader);

        CountryCodeDictionary.getInstance().load(this);
        CountryFlagsLoader.getInstance();

        historyManager = new HistoryManager(this);
        favouriteManager = new FavouriteManager(this);
        fallbackStationsManager = new FallbackStationsManager(this);
        recordingsManager = new RecordingsManager();
        alarmManager = new RadioAlarmManager(this);

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            tvChannelManager = new TvChannelManager(this);
            favouriteManager.addObserver(tvChannelManager);
        }

        trackHistoryRepository = new TrackHistoryRepository(this);

        mpdClient = new MPDClient(this);

        castHandler = new CastHandler();

        trackMetadataSearcher = new TrackMetadataSearcher(httpClient);

        playStationRepository = new PlayStationRepository(httpClient);

        recordingsManager.updateRecordingsList();
    }

    public void setTestsInterceptor(Interceptor testsInterceptor) {
        this.testsInterceptor = testsInterceptor;
    }

    public void rebuildHttpClient() {
        OkHttpClient.Builder builder = newHttpClient()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new UserAgentInterceptor("RadioDroid2/" + BuildConfig.VERSION_NAME));

        httpClient = builder.build();
    }

    public FallbackStationsManager getFallbackStationsManager() {
        return fallbackStationsManager;
    }
   
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public FavouriteManager getFavouriteManager() {
        return favouriteManager;
    }

    public RecordingsManager getRecordingsManager() {
        return recordingsManager;
    }

    public RadioAlarmManager getAlarmManager() {
        return alarmManager;
    }

    public TrackHistoryRepository getTrackHistoryRepository() {
        return trackHistoryRepository;
    }

    public MPDClient getMpdClient() {
        return mpdClient;
    }

    public CastHandler getCastHandler() {
        return castHandler;
    }

    public TrackMetadataSearcher getTrackMetadataSearcher() {
        return trackMetadataSearcher;
    }

    public PlayStationRepository getPlayStationRepository() {
        return playStationRepository;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public OkHttpClient.Builder newHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().connectionPool(connectionPool);

        if (testsInterceptor != null) {
            builder.addInterceptor(testsInterceptor);
        }

        if (!setCurrentOkHttpProxy(builder)) {
            Toast toast = Toast.makeText(this, getResources().getString(R.string.ignore_proxy_settings_invalid), Toast.LENGTH_SHORT);
            toast.show();
        }
        return Utils.enableTls12OnPreLollipop(builder);
    }

    public OkHttpClient.Builder newHttpClientWithoutProxy() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().connectionPool(connectionPool);

        if (testsInterceptor != null) {
            builder.addInterceptor(testsInterceptor);
        }

        return Utils.enableTls12OnPreLollipop(builder);
    }

    public boolean setCurrentOkHttpProxy(@NonNull OkHttpClient.Builder builder) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ProxySettings proxySettings = ProxySettings.fromPreferences(sharedPref);
        if (proxySettings != null) {
            if (!Utils.setOkHttpProxy(builder, proxySettings)) {
                // proxy settings are not valid
                return false;
            }
        }
        return true;
    }
}
