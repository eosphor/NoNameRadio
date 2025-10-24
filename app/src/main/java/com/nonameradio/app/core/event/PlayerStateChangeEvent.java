package com.nonameradio.app.core.event;

import com.nonameradio.app.players.PlayState;

/**
 * Event fired when player state changes.
 * Replaces PLAYER_SERVICE_STATE_CHANGE broadcast.
 */
public class PlayerStateChangeEvent {
    private final PlayState state;

    public PlayerStateChangeEvent(PlayState state) {
        this.state = state;
    }

    public PlayState getState() {
        return state;
    }
}

