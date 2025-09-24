package net.programmierecke.radiodroid2.core.domain.commands;

import net.programmierecke.radiodroid2.core.domain.interfaces.IPlayerService;
import net.programmierecke.radiodroid2.station.DataRadioStation;

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

