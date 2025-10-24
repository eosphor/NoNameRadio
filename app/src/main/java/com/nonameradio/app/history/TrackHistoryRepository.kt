package com.nonameradio.app.history

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nonameradio.app.database.NoNameRadioDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.Date
import java.util.concurrent.Executor

/**
 * Repository for Track History with Paging 3 support.
 * 
 * Migrated to Kotlin to support Flow.cachedIn() which prevents
 * "Attempt to collect twice from pageEventFlow" crash.
 * 
 * @author NoNameRadio Team
 * @since 0.87.0
 */
class TrackHistoryRepository(application: Application) {
    
    /**
     * Callback interface for Java interoperability
     */
    interface GetItemCallback {
        /**
         * Will be run in the DB thread
         *
         * @param trackHistoryEntry fetched entry
         * @param dao could be used to immediately do changes in the DB
         */
        fun onItemFetched(trackHistoryEntry: TrackHistoryEntry?, dao: TrackHistoryDao)
    }
    
    private val dao: TrackHistoryDao
    private val queryExecutor: Executor
    
    // Repository-level CoroutineScope for cachedIn()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    val allHistoryPaged: LiveData<PagingData<TrackHistoryEntry>>

    // For performance reasons we don't want to enforce history limit on every insert
    private var insertsToTruncateLeft = 0

    init {
        val db = NoNameRadioDatabase.getDatabase(application)
        dao = db.songHistoryDao()
        queryExecutor = db.queryExecutor

        val config = PagingConfig(
            pageSize = HISTORY_PAGE_SIZE,
            prefetchDistance = HISTORY_PAGE_SIZE,
            enablePlaceholders = true,
            initialLoadSize = HISTORY_PAGE_SIZE * 2,
            maxSize = 200
        )

        val pager = Pager(
            config = config,
            pagingSourceFactory = { dao.getAllHistoryPositional() }
        )

        // cachedIn() prevents "collect twice" crash on Fragment recreation
        allHistoryPaged = pager.flow
            .cachedIn(repositoryScope)
            .asLiveData()
    }

    fun insert(historyEntry: TrackHistoryEntry) {
        queryExecutor.execute {
            dao.insert(historyEntry)
            if (insertsToTruncateLeft == 0) {
                insertsToTruncateLeft = TRUNCATE_FREQUENCY
                dao.truncateHistory(TrackHistoryEntry.MAX_HISTORY_ITEMS_IN_TABLE)
            } else {
                insertsToTruncateLeft--
            }
        }
    }

    fun update(trackHistoryEntry: TrackHistoryEntry) {
        queryExecutor.execute {
            dao.update(trackHistoryEntry)
        }
    }

    fun deleteHistory() {
        queryExecutor.execute {
            dao.deleteHistory()
        }
    }

    fun getLastInsertedHistoryItem(callback: GetItemCallback) {
        queryExecutor.execute {
            val item = dao.getLastInsertedHistoryItem()
            callback.onItemFetched(item, dao)
        }
    }

    fun setTrackArtUrl(uid: Int, artUrl: String) {
        queryExecutor.execute {
            dao.setTrackArtUrl(uid, artUrl)
        }
    }

    companion object {
        private const val HISTORY_PAGE_SIZE = 15
        private const val TRUNCATE_FREQUENCY = 20
    }
}

