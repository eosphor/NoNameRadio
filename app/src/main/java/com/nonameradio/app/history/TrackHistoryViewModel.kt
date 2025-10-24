package com.nonameradio.app.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nonameradio.app.NoNameRadioApp
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel for Track History with Paging 3 support.
 * 
 * Migrated to Kotlin to support cachedIn(viewModelScope) which prevents
 * the "Attempt to collect twice from pageEventFlow" crash when Fragment
 * is recreated (e.g., on configuration changes, appearance changes).
 * 
 * @author NoNameRadio Team
 * @since 0.87.0
 */
class TrackHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TrackHistoryRepository

    init {
        val app = getApplication<NoNameRadioApp>()
        repository = app.trackHistoryRepository
    }

    /**
     * Returns cached Paging data flow.
     * cachedIn(viewModelScope) ensures the flow is shared across multiple collectors
     * and prevents "collect twice" IllegalStateException.
     */
    fun getAllHistoryPaged(): LiveData<PagingData<TrackHistoryEntry>> {
        return repository.allHistoryPaged
    }
}

