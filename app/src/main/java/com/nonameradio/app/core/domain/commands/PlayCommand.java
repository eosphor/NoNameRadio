package com.nonameradio.app.core.domain.commands;

import com.nonameradio.app.core.domain.interfaces.IPlayerService;
import com.nonameradio.app.station.DataRadioStation;

public class PlayCommand implements IPlayerCommand {
    private final IPlayerService playerService;
    private final DataRadioStation station;

    public PlayCommand(IPlayerService playerService, DataRadioStation station) {
        this.playerService = playerService;
        this.station = station;
    }

    @Override
    public void execute() {
        playerService.play(station);
    }

    @Override
    public void undo() {
        playerService.pause();
    }

    @Override
    public String getDescription() {
        return "Play station: " + (station != null ? station.Name : "Unknown");
    }
}

