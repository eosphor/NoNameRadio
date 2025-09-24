package net.programmierecke.radiodroid2.core.domain.commands;

import net.programmierecke.radiodroid2.core.domain.interfaces.IPlayerService;
import net.programmierecke.radiodroid2.station.DataRadioStation;

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

