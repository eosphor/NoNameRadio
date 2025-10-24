package com.nonameradio.app.core.event;

import com.nonameradio.app.players.selector.PlayerType;

/**
 * Event fired when metered connection is detected.
 * Replaces PLAYER_SERVICE_METERED_CONNECTION broadcast.
 */
public class MeteredConnectionEvent {
    private final PlayerType playerType;

    public MeteredConnectionEvent(PlayerType playerType) {
        this.playerType = playerType;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }
}

