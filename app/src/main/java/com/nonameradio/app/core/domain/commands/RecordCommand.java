package com.nonameradio.app.core.domain.commands;

import com.nonameradio.app.core.domain.interfaces.IPlayerService;
import com.nonameradio.app.station.DataRadioStation;

public class RecordCommand implements IPlayerCommand {
    private final IPlayerService playerService;
    private final DataRadioStation station;

    public RecordCommand(IPlayerService playerService, DataRadioStation station) {
        this.playerService = playerService;
        this.station = station;
    }

    @Override
    public void execute() {
        playerService.startRecording();
    }

    @Override
    public void undo() {
        playerService.stopRecording();
    }

    @Override
    public String getDescription() {
        return "Start recording: " + (station != null ? station.Name : "Unknown");
    }
}

