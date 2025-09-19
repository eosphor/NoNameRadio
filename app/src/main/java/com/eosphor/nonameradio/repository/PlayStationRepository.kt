package com.eosphor.nonameradio.repository

import android.content.Context
import com.eosphor.nonameradio.station.DataRadioStation
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayStationRepository @Inject constructor(
    private val httpClient: OkHttpClient
) {
    
    enum class ExecutionResult {
        SUCCESS,
        FAILURE
    }
    
    suspend fun getPlayableUrl(
        station: DataRadioStation,
        context: Context
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("PlayStationRepository", "Getting playable URL for station: ${station.Name}")
            android.util.Log.d("PlayStationRepository", "playableUrl: '${station.playableUrl}'")
            android.util.Log.d("PlayStationRepository", "StreamUrl: '${station.StreamUrl}'")
            
            // Если у станции уже есть прямая ссылка для воспроизведения
            if (!station.playableUrl.isNullOrEmpty()) {
                android.util.Log.d("PlayStationRepository", "Using playableUrl: ${station.playableUrl}")
                return@withContext Result.success(station.playableUrl)
            }
            
            // Если есть StreamUrl, проверяем его тип
            if (!station.StreamUrl.isNullOrEmpty()) {
                android.util.Log.d("PlayStationRepository", "Using StreamUrl: ${station.StreamUrl}")
                
                // Если это M3U плейлист, пытаемся получить реальный URL
                if (station.StreamUrl.endsWith(".m3u") || station.StreamUrl.endsWith(".m3u8")) {
                    try {
                        val request = Request.Builder()
                            .url(station.StreamUrl)
                            .build()

                        httpClient.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val playlistContent = response.body?.string() ?: ""
                                android.util.Log.d("PlayStationRepository", "M3U content: $playlistContent")

                                // Ищем первую строку, которая не является комментарием и содержит URL
                                val lines = playlistContent.split("\n")
                                for (line in lines) {
                                    val trimmedLine = line.trim()
                                    if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#") &&
                                        (trimmedLine.startsWith("http://") || trimmedLine.startsWith("https://"))) {
                                        android.util.Log.d("PlayStationRepository", "Found stream URL in M3U: $trimmedLine")
                                        return@withContext Result.success(trimmedLine)
                                    }
                                }
                            }
                        }

                        // Если не удалось распарсить M3U, используем оригинальный URL
                        android.util.Log.w("PlayStationRepository", "Could not parse M3U, using original URL")
                        return@withContext Result.success(station.StreamUrl)
                    } catch (e: Exception) {
                        android.util.Log.e("PlayStationRepository", "Error parsing M3U", e)
                        // В случае ошибки используем оригинальный URL
                        return@withContext Result.success(station.StreamUrl)
                    }
                } else {
                    // Обычный URL потока
                    return@withContext Result.success(station.StreamUrl)
                }
            }
            
            // Если нет URL для воспроизведения, возвращаем ошибку
            android.util.Log.e("PlayStationRepository", "No playable URL available for station: ${station.Name}")
            Result.failure(Exception("No playable URL available for station: ${station.Name}"))
        } catch (e: Exception) {
            android.util.Log.e("PlayStationRepository", "Error getting playable URL", e)
            Result.failure(e)
        }
    }
    
    fun getPlayableUrlAsync(
        station: DataRadioStation,
        context: Context,
        scope: CoroutineScope,
        onSuccess: (String) -> kotlin.Unit,
        onFailure: (ExecutionResult) -> kotlin.Unit
    ): Job {
        return scope.launch {
            try {
                val result = getPlayableUrl(station, context)
                
                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = { url -> onSuccess(url) },
                        onFailure = { onFailure(ExecutionResult.FAILURE) }
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFailure(ExecutionResult.FAILURE)
                }
            }
        }
    }
}
