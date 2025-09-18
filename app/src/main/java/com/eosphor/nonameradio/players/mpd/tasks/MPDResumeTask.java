package com.eosphor.nonameradio.players.mpd.tasks;

import androidx.annotation.Nullable;

import com.eosphor.nonameradio.players.mpd.MPDAsyncTask;

public class MPDResumeTask extends MPDAsyncTask {
    public MPDResumeTask(@Nullable FailureCallback failureCallback) {
        setStages(
                new ReadStage[]{
                        okReadStage(),
                        statusReadStage(false)
                },
                new WriteStage[]{
                        (task, bufferedWriter) -> {
                            bufferedWriter.write("command_list_begin\npause 0\nstatus\ncommand_list_end\n");
                            return true;
                        }}, failureCallback);
    }
}
