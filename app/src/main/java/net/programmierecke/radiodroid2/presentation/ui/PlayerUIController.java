package net.programmierecke.radiodroid2.presentation.ui;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import net.programmierecke.radiodroid2.FragmentPlayerFull;
import net.programmierecke.radiodroid2.R;
import net.programmierecke.radiodroid2.core.domain.interfaces.IPlayerService;
import net.programmierecke.radiodroid2.core.utils.UiUtils;
import net.programmierecke.radiodroid2.station.DataRadioStation;

public class PlayerUIController {
    private final FragmentPlayerFull fragment;
    private final IPlayerService playerService;

    public PlayerUIController(FragmentPlayerFull fragment, IPlayerService playerService) {
        this.fragment = fragment;
        this.playerService = playerService;
    }

    public void updatePlayPauseButton() {
        boolean isPlaying = playerService.isPlaying();
        // TODO: Update play/pause button when integrated with UI
    }

    public void updateRecordingButton() {
        boolean isRecording = playerService.isRecording();
        // TODO: Update recording button when integrated with UI
    }

    public void updateRecordingUI() {
        // Update recording status display
        // This will be implemented when we refactor the recording UI
        updateRecordingButton();
    }

    public void updateStationInfo(DataRadioStation station) {
        if (station == null) return;
        // TODO: Update station info when integrated with UI
    }

    public void showError(String message) {
        UiUtils.showToast(fragment.getContext(), message);
    }

    public void showLoading(boolean show) {
        // TODO: Show/hide loading when integrated with UI
    }

    private String buildStationInfo(DataRadioStation station) {
        StringBuilder info = new StringBuilder();

        if (station.Country != null && !station.Country.isEmpty()) {
            info.append(station.Country);
        }

        if (station.Bitrate > 0) {
            if (info.length() > 0) info.append(" • ");
            info.append(station.Bitrate).append(" kbps");
        }

        return info.toString();
    }
}
