package com.nonameradio.app.core.event;

import androidx.annotation.Nullable;

/**
 * Event fired when radio station local info changes.
 * Replaces DataRadioStation.RADIO_STATION_LOCAL_INFO_CHAGED broadcast.
 *
 * @author NoNameRadio Team
 * @version 1.0.0
 * @since 0.86.904
 */
public class RadioStationChangedEvent {
    @Nullable
    public final String stationUuid;

    public RadioStationChangedEvent(@Nullable String stationUuid) {
        this.stationUuid = stationUuid;
    }
}

