package com.nonameradio.app.core.event;

/**
 * Event for hiding loading indicator
 */
public final class HideLoadingEvent {
    public static final HideLoadingEvent INSTANCE = new HideLoadingEvent();

    private HideLoadingEvent() {
        // Private constructor for singleton
    }
}
