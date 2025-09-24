package com.nonameradio.app.core.event;

/**
 * Event for showing loading indicator
 */
public final class ShowLoadingEvent {
    public static final ShowLoadingEvent INSTANCE = new ShowLoadingEvent();

    private ShowLoadingEvent() {
        // Private constructor for singleton
    }
}
