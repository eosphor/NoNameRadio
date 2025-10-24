package com.nonameradio.app.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.tvprovider.media.tv.*
import com.nonameradio.app.ActivityMain
import com.nonameradio.app.R
import com.nonameradio.app.NoNameRadioApp
import com.nonameradio.app.service.MediaSessionCallback
import java.io.IOException
import java.util.*

private const val INVALID_CONTENT_ID: Long = -1

fun <T : BaseProgram> Cursor.asSequence(fromCursor: (Cursor) -> T): Sequence<T> {
    moveToFirst()
    return generateSequence {
        if (this.isAfterLast) {
            close()
            null
        } else {
            val next = fromCursor(this)
            moveToNext()
            next
        }
    }
}

@SuppressLint("RestrictedApi")
class TvChannelManager(val app: NoNameRadioApp) {
    private val helper = PreviewChannelHelper(app)
    private var channelId = INVALID_CONTENT_ID

    init {
        if (Build.VERSION.SDK_INT >= 26) {
            channelId = helper.allChannels.firstOrNull()?.id ?: createDefaultChannel()
        }
    }

    private fun createDefaultChannel(): Long = try {
        val channel = with(PreviewChannel.Builder()) {
            setDisplayName(app.getString(R.string.app_name))
            setDescription(app.getString(R.string.app_name))
            setAppLinkIntent(Intent(app, ActivityMain::class.java))
            AppCompatResources.getDrawable(app, R.drawable.ic_launcher)?.toBitmap()?.also { logo ->
                setLogo(logo)
            }
            build()
        }
        helper.publishChannel(channel)
    } catch (e: IOException) {
        Log.e(TAG, "Failed to create a channel", e)
        INVALID_CONTENT_ID
    }

    @RequiresApi(26)
    private fun getPreviewPrograms(): Sequence<PreviewProgram> = try {
        val cursor = app.contentResolver.query(
                TvContractCompat.PreviewPrograms.CONTENT_URI,
                PreviewProgram.PROJECTION,
                null,
                null,
                null)
        cursor?.asSequence {
            PreviewProgram.fromCursor(it)
        } ?: emptySequence()
    } catch (e: IllegalArgumentException) {
        emptySequence()
    }

    @SuppressLint("NewApi")
    fun publishStarred() {
        if (channelId == INVALID_CONTENT_ID) return

        val starredPrograms = getPreviewPrograms().map {
            Pair(it.contentId, it)
        }.toMap().toMutableMap()

        for (station in app.favouriteManager.list) {
            val existingProgram = starredPrograms[station.StationUuid]
            val programBuilder = existingProgram?.let {
                PreviewProgram.Builder(it)
            } ?: PreviewProgram.Builder()

            val intent = Intent(
                    MediaSessionCallback.ACTION_PLAY_STATION_BY_UUID,
                    null,
                    app,
                    ActivityMain::class.java
            ).putExtra(MediaSessionCallback.EXTRA_STATION_UUID, station.StationUuid)

            val program = with(programBuilder) {
                setChannelId(channelId)
                setContentId(station.StationUuid)
                setType(TvContractCompat.PreviewPrograms.TYPE_STATION)
                setTitle(station.Name)
                setPosterArtUri(station.IconUrl.toUri())
                setLogoUri(station.IconUrl.toUri())
                setIntent(intent)
                setLive(true)
                build()
            }

            if (existingProgram == null) {
                Log.d(TAG, "Adding $station")
                helper.publishPreviewProgram(program)
            } else {
                Log.d(TAG, "Updating $station")
                helper.updatePreviewProgram(existingProgram.id, program)
                starredPrograms.remove(existingProgram.contentId)
            }
        }

        for (program in starredPrograms.values) {
            Log.d(TAG, "Deleting programme $program")
            helper.deletePreviewProgram(program.id)
        }
        if (app.favouriteManager.list.any()) {
            Log.d(TAG, "Requesting channel to be browseable")
            TvContractCompat.requestChannelBrowsable(app, channelId)
        }
    }

    companion object {
        private val TAG = TvChannelManager::class.java.simpleName
    }
}
