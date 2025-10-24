package com.nonameradio.app;

import com.nonameradio.app.core.event.EventBus;
import com.nonameradio.app.core.event.ShowLoadingEvent;
import com.nonameradio.app.core.event.HideLoadingEvent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import okhttp3.OkHttpClient;

public class FragmentBase extends Fragment {
    private static final String TAG = "FragmentBase";

    private String relativeUrl;
    private String urlResult;

    private boolean isCreated = false;

    private CompletableFuture<String> downloadTask = null;

    public FragmentBase() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isCreated = true;

        if (relativeUrl == null) {
            Bundle bundle = this.getArguments();
            relativeUrl = bundle.getString("url");
        }

        DownloadUrl(false);
    }

    @Override
    public void onDestroy() {
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }

        super.onDestroy();
    }

    protected String getUrlResult() {
        return urlResult;
    }

    protected boolean hasUrl() {
        return !TextUtils.isEmpty(relativeUrl);
    }

    /*
    public void SetDownloadUrl(String theUrl) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "new relativeUrl " + theUrl);
        }
        relativeUrl = theUrl;
        DownloadUrl(false);
    }
     */

    public void DownloadUrl(final boolean forceUpdate) {
        DownloadUrl(forceUpdate, true);
    }

    public void DownloadUrl(final boolean forceUpdate, final boolean displayProgress) {
        if (!isCreated) {
            return;
        }
        if (downloadTask != null){
            downloadTask.cancel(true);
            downloadTask = null;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        final boolean show_broken = sharedPref.getBoolean("show_broken", false);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Download relativeUrl:" + relativeUrl);
        }

        if (TextUtils.isGraphic(relativeUrl)) {
            String cache = Utils.getCacheFile(getActivity(), relativeUrl);
            if (cache == null || forceUpdate) {
                if (getContext() != null && displayProgress) {
                    EventBus.post(ShowLoadingEvent.INSTANCE);
                }

                NoNameRadioApp app = (NoNameRadioApp) getActivity().getApplication();
                final OkHttpClient httpClient = app.getHttpClient();

                downloadTask = com.nonameradio.app.core.utils.AsyncExecutor.submitIOTask(() -> {
                    HashMap<String, String> p = new HashMap<String, String>();
                    p.put("hidebroken", ""+(!show_broken));
                    return Utils.downloadFeedRelative(httpClient, getActivity(), relativeUrl, forceUpdate, p);
                });

                downloadTask.thenAccept(result -> {
                    com.nonameradio.app.core.utils.UiHandler.post(() -> {
                        DownloadFinished();
                        EventBus.post(HideLoadingEvent.INSTANCE);
                        
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Download finished:" + relativeUrl);
                        }
                        
                        if (result != null) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "Download OK:" + relativeUrl);
                            }
                            urlResult = result;
                            RefreshListGui();
                        } else {
                            try {
                                if (getContext() != null) {
                                    Toast toast = Toast.makeText(getContext(), 
                                                                getResources().getText(R.string.error_list_update), 
                                                                Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            } catch(Exception e){
                                Log.e(TAG, "Error showing toast", e);
                            }
                        }
                    });
                }).exceptionally(throwable -> {
                    com.nonameradio.app.core.utils.UiHandler.post(() -> {
                        DownloadFinished();
                        EventBus.post(HideLoadingEvent.INSTANCE);
                        Log.e(TAG, "Error downloading: " + relativeUrl, throwable);
                        
                        try {
                            if (getContext() != null) {
                                Toast toast = Toast.makeText(getContext(), 
                                                            getResources().getText(R.string.error_list_update), 
                                                            Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        } catch(Exception e){
                            Log.e(TAG, "Error showing error toast", e);
                        }
                    });
                    return null;
                });
            } else {
                urlResult = cache;
                DownloadFinished();
                RefreshListGui();

            }
        } else {
            RefreshListGui();
        }
    }

    protected void RefreshListGui() {
    }

    protected void DownloadFinished() {
    }
}