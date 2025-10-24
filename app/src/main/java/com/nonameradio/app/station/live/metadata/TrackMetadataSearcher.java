package com.nonameradio.app.station.live.metadata;

import androidx.annotation.NonNull;

import com.nonameradio.app.station.live.metadata.lastfm.LfmMetadataSearcher;

import okhttp3.OkHttpClient;

public class TrackMetadataSearcher {
    private final LfmMetadataSearcher lfmMetadataSearcher;

    public TrackMetadataSearcher(OkHttpClient httpClient) {
        lfmMetadataSearcher = new LfmMetadataSearcher(httpClient);
    }


    public void fetchTrackMetadata(String LastFMApiKey, String artist, @NonNull String track, @NonNull TrackMetadataCallback trackMetadataCallback) {
        lfmMetadataSearcher.fetchTrackMetadata(LastFMApiKey, artist, track, trackMetadataCallback);
    }
}
