package net.programmierecke.radiodroid2.core.domain.commands;

public interface IPlayerCommand {
    void execute();
    void undo();
    String getDescription();
}

