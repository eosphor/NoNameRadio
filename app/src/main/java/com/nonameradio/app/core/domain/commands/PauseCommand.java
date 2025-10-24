package com.nonameradio.app.core.domain.commands;

import com.nonameradio.app.core.domain.interfaces.IPlayerService;

public class PauseCommand implements IPlayerCommand {
    private final IPlayerService playerService;

    public PauseCommand(IPlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public void execute() {
        playerService.pause();
    }

    @Override
    public void undo() {
        // Cannot undo pause easily, so do nothing
    }

    @Override
    public String getDescription() {
        return "Pause playback";
    }
}

