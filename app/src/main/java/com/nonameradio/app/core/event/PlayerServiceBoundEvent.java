package com.nonameradio.app.core.event;

/**
 * Event fired when PlayerService is bound and ready.
 * Replaces PlayerService.PLAYER_SERVICE_BOUND broadcast.
 *
 * @author NoNameRadio Team
 * @version 1.0.0
 * @since 0.86.904
 */
public class PlayerServiceBoundEvent {
    /**
     * Singleton instance to avoid creating new objects.
     */
    public static final PlayerServiceBoundEvent INSTANCE = new PlayerServiceBoundEvent();

    private PlayerServiceBoundEvent() {
        // Private constructor for singleton
    }
}

