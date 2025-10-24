package com.nonameradio.app.core.event;

/**
 * Event fired when player timer updates (every second during playback).
 * Replaces PlayerService.PLAYER_SERVICE_TIMER_UPDATE broadcast.
 *
 * @author NoNameRadio Team
 * @version 1.0.0
 * @since 0.86.904
 */
public class TimerUpdateEvent {
    /**
     * Singleton instance to avoid creating new objects every second.
     * Timer updates happen frequently, so we reuse the same instance.
     */
    public static final TimerUpdateEvent INSTANCE = new TimerUpdateEvent();

    private TimerUpdateEvent() {
        // Private constructor for singleton
    }
}

