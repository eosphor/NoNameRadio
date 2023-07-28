/*
 * UpdateHelper.kt
 * Implements the UpdateHelper class
 * A UpdateHelper provides methods to update a single station or the whole collection of stations
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-22 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package com.jamal2367.urlradio.helpers

import android.content.Context
import android.util.Log
import com.jamal2367.urlradio.Keys
import com.jamal2367.urlradio.core.Collection
import com.jamal2367.urlradio.core.Station
import com.jamal2367.urlradio.search.RadioBrowserResult
import com.jamal2367.urlradio.search.RadioBrowserSearch
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main


/*
 * UpdateHelper class
 */
class UpdateHelper(
    private val context: Context,
    private val updateHelperListener: UpdateHelperListener,
    private var collection: Collection
) : RadioBrowserSearch.RadioBrowserSearchListener {


    /* Define log tag */
    private val TAG: String = UpdateHelper::class.java.simpleName


    /* Main class variables */
    private var radioBrowserSearchCounter: Int = 0
    private var remoteStationLocationsList: MutableList<String> = mutableListOf()


    /* Listener Interface */
    interface UpdateHelperListener {
        fun onStationUpdated(
            collection: Collection,
            positionPriorUpdate: Int,
            positionAfterUpdate: Int
        )
    }


    /* Overrides onRadioBrowserSearchResults from RadioBrowserSearchListener */
    override fun onRadioBrowserSearchResults(results: Array<RadioBrowserResult>) {
        if (results.isNotEmpty()) {
            CoroutineScope(IO).launch {
                // get station from results
                val station: Station = results[0].toStation()
                // detect content type
                val deferred: Deferred<NetworkHelper.ContentType> =
                    async(Dispatchers.Default) { NetworkHelper.detectContentTypeSuspended(station.getStreamUri()) }
                // wait for result
                val contentType: NetworkHelper.ContentType = deferred.await()
                // update content type
                station.streamContent = contentType.type
                // get position
                val positionPriorUpdate =
                    CollectionHelper.getStationPositionFromRadioBrowserStationUuid(
                        collection,
                        station.radioBrowserStationUuid
                    )
                // update (and sort) collection
                collection = CollectionHelper.updateStation(context, collection, station)
                // get new position
                val positionAfterUpdate: Int =
                    CollectionHelper.getStationPositionFromRadioBrowserStationUuid(
                        collection,
                        station.radioBrowserStationUuid
                    )
                // hand over results
                withContext(Main) {
                    updateHelperListener.onStationUpdated(
                        collection,
                        positionPriorUpdate,
                        positionAfterUpdate
                    )
                }
                // decrease counter
                radioBrowserSearchCounter--
                // all downloads from radio browser succeeded
                if (radioBrowserSearchCounter == 0 && remoteStationLocationsList.isNotEmpty()) {
                    // direct download of playlists
                    DownloadHelper.downloadPlaylists(
                        context,
                        remoteStationLocationsList.toTypedArray()
                    )
                }
            }
        }
    }


    /* Updates the whole collection of stations */
    fun updateCollection() {
        PreferencesHelper.saveLastUpdateCollection()
        collection.stations.forEach { station ->
            when {
                station.radioBrowserStationUuid.isNotEmpty() -> {
                    // increase counter
                    radioBrowserSearchCounter++
                    // request download from radio browser
                    downloadFromRadioBrowser(station.radioBrowserStationUuid)
                }
                station.remoteStationLocation.isNotEmpty() -> {
                    // add playlist link to list for later(!) download in onRadioBrowserSearchResults
                    remoteStationLocationsList.add(station.remoteStationLocation)
                }
                else -> {
                    Log.w(TAG, "Unable to update station: ${station.name}.")
                }
            }
        }
        // special case: collection contained only playlist files
        if (radioBrowserSearchCounter == 0) {
            // direct download of playlists
            DownloadHelper.downloadPlaylists(context, remoteStationLocationsList.toTypedArray())
        }
    }


    /* Get updated station from radio browser - results are handled by onRadioBrowserSearchResults */
    private fun downloadFromRadioBrowser(radioBrowserStationUuid: String) {
        val radioBrowserSearch = RadioBrowserSearch(this)
        radioBrowserSearch.searchStation(context, radioBrowserStationUuid, Keys.SEARCH_TYPE_BY_UUID)
    }

}
