package com.nonameradio.app.core.domain.controllers;

import com.nonameradio.app.core.domain.commands.IPlayerCommand;
import com.nonameradio.app.core.domain.commands.PauseCommand;
import com.nonameradio.app.core.domain.commands.PlayCommand;
import com.nonameradio.app.core.domain.commands.RecordCommand;
import com.nonameradio.app.core.domain.interfaces.IPlayerService;
import com.nonameradio.app.station.DataRadioStation;
import java.util.ArrayList;
import java.util.List;

public class PlayerController {
    private final IPlayerService playerService;
    private final List<IPlayerCommand> commandHistory = new ArrayList<>();
    private final int maxHistorySize = 10;

    public PlayerController(IPlayerService playerService) {
        this.playerService = playerService;
    }

    public void play(DataRadioStation station) {
        PlayCommand command = new PlayCommand(playerService, station);
        executeCommand(command);
    }

    public void pause() {
        PauseCommand command = new PauseCommand(playerService);
        executeCommand(command);
    }

    public void startRecording(DataRadioStation station) {
        RecordCommand command = new RecordCommand(playerService, station);
        executeCommand(command);
    }

    public void stopRecording() {
        playerService.stopRecording();
        // Note: Stop recording doesn't need to be undoable
    }

    public void stop() {
        playerService.stop();
    }

    public boolean isPlaying() {
        return playerService.isPlaying();
    }

    public boolean isRecording() {
        return playerService.isRecording();
    }

    public DataRadioStation getCurrentStation() {
        return playerService.getCurrentStation();
    }

    public void undo() {
        if (!commandHistory.isEmpty()) {
            IPlayerCommand lastCommand = commandHistory.remove(commandHistory.size() - 1);
            lastCommand.undo();
        }
    }

    public void undoLast() {
        undo();
    }

    public List<String> getCommandHistory() {
        List<String> descriptions = new ArrayList<>();
        for (IPlayerCommand command : commandHistory) {
            descriptions.add(command.getDescription());
        }
        return descriptions;
    }

    public void clearHistory() {
        commandHistory.clear();
    }

    private void executeCommand(IPlayerCommand command) {
        command.execute();
        commandHistory.add(command);

        // Keep history size limited
        if (commandHistory.size() > maxHistorySize) {
            commandHistory.remove(0);
        }
    }
}

