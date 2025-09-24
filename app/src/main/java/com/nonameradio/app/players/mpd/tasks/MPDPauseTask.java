package com.nonameradio.app.players.mpd.tasks;

import androidx.annotation.Nullable;

import com.nonameradio.app.players.mpd.MPDAsyncTask;

public class MPDPauseTask extends MPDAsyncTask {
    public MPDPauseTask(@Nullable FailureCallback failureCallback) {
        setStages(
                new MPDAsyncTask.ReadStage[]{
                        okReadStage(),
                        statusReadStage(false)
                },
                new MPDAsyncTask.WriteStage[]{
                        (task, bufferedWriter) -> {
                            bufferedWriter.write("command_list_begin\npause 1\nstatus\ncommand_list_end\n");
                            return true;
                        }}, failureCallback);
    }
}
