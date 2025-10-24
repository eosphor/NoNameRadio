package com.nonameradio.app.core.event;

import androidx.annotation.Nullable;

/**
 * Event fired when a station should be played by its ID.
 * Replaces MediaSessionCallback.BROADCAST_PLAY_STATION_BY_ID broadcast.
 * Used primarily for Android Auto and external playback requests.
 *
 * @author NoNameRadio Team
 * @version 1.0.0
 * @since 0.86.904
 */
public class PlayStationByIdEvent {
    @Nullable
    public final String stationId;

    public PlayStationByIdEvent(@Nullable String stationId) {
        this.stationId = stationId;
    }
}

