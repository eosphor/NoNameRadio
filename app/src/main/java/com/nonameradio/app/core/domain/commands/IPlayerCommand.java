package com.nonameradio.app.core.domain.commands;

public interface IPlayerCommand {
    void execute();
    void undo();
    String getDescription();
}

