package com.nonameradio.app.core.event;

/**
 * Event fired when media session needs update.
 * Replaces ACTION_MEDIASESSION_UPDATE broadcast.
 */
public class MediaSessionUpdateEvent {
    private final String stationId;

    public MediaSessionUpdateEvent(String stationId) {
        this.stationId = stationId;
    }

    public String getStationId() {
        return stationId;
    }
}

