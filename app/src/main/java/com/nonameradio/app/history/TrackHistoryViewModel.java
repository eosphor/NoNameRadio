package com.nonameradio.app.history;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.nonameradio.app.NoNameRadioApp;

public class TrackHistoryViewModel extends AndroidViewModel {
    private final TrackHistoryRepository repository;

    public TrackHistoryViewModel(Application application) {
        super(application);

        NoNameRadioApp radioDroidApp = getApplication();
        repository = radioDroidApp.getTrackHistoryRepository();
    }

    public LiveData<PagedList<TrackHistoryEntry>> getAllHistoryPaged() {
        return repository.getAllHistoryPaged();
    }
}
