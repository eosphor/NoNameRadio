package com.nonameradio.app.core.event;

/**
 * Event fired when player metadata updates (track info, station name, etc).
 * Replaces PlayerService.PLAYER_SERVICE_META_UPDATE broadcast.
 *
 * @author NoNameRadio Team
 * @version 1.0.0
 * @since 0.86.904
 */
public class MetaUpdateEvent {
    /**
     * Singleton instance to avoid creating new objects for frequent updates.
     * Metadata updates happen frequently, so we reuse the same instance.
     */
    public static final MetaUpdateEvent INSTANCE = new MetaUpdateEvent();

    private MetaUpdateEvent() {
        // Private constructor for singleton
    }
}

