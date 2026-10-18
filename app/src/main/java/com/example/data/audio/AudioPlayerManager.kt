package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import com.example.data.AudioData
import com.example.model.AudioTrack
import com.example.model.PlayerPlaybackState
import com.example.model.Reciter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AudioPlayerManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var progressTrackerJob: Job? = null

    private val _playbackState = MutableStateFlow(PlayerPlaybackState())
    val playbackState: StateFlow<PlayerPlaybackState> = _playbackState.asStateFlow()

    private val audioDir: File by lazy {
        val dir = File(context.filesDir, "quran_recitations")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    fun isTrackDownloaded(reciterId: String, surahNumber: Int): Boolean {
        val file = getLocalFile(reciterId, surahNumber)
        return file.exists() && file.length() > 0
    }

    private fun getLocalFile(reciterId: String, surahNumber: Int): File {
        val formattedNum = String.format("%03d", surahNumber)
        return File(audioDir, "${reciterId}_$formattedNum.mp3")
    }

    fun playTrack(surahNumber: Int, surahName: String, reciter: Reciter) {
        val isDownloaded = isTrackDownloaded(reciter.id, surahNumber)
        val localFile = if (isDownloaded) getLocalFile(reciter.id, surahNumber) else null
        val streamUrl = AudioData.getAudioUrl(reciter, surahNumber)

        val track = AudioTrack(
            surahNumber = surahNumber,
            surahName = surahName,
            reciter = reciter,
            streamUrl = streamUrl,
            localFilePath = localFile?.absolutePath,
            isDownloaded = isDownloaded
        )

        val dataSource = localFile?.absolutePath ?: streamUrl

        scope.launch {
            try {
                _playbackState.value = _playbackState.value.copy(
                    isBuffering = true,
                    currentTrack = track,
                    currentPositionMs = 0L,
                    durationMs = 0L,
                    errorMessage = null
                )

                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )

                    setDataSource(dataSource)
                    isLooping = _playbackState.value.isLooping

                    setOnPreparedListener { mp ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                mp.playbackParams = mp.playbackParams.setSpeed(_playbackState.value.playbackSpeed)
                            } catch (_: Exception) {}
                        }
                        mp.start()
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = true,
                            isBuffering = false,
                            durationMs = mp.duration.toLong()
                        )
                        startProgressTracker()
                    }

                    setOnCompletionListener {
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = false,
                            currentPositionMs = it.duration.toLong()
                        )
                        stopProgressTracker()
                    }

                    setOnErrorListener { _, what, extra ->
                        _playbackState.value = _playbackState.value.copy(
                            isPlaying = false,
                            isBuffering = false,
                            errorMessage = "تعذر تشغيل التلاوة. يرجى التحقق من الاتصال بالإنترنت"
                        )
                        stopProgressTracker()
                        true
                    }

                    prepareAsync()
                }
            } catch (e: Exception) {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    isBuffering = false,
                    errorMessage = "حدث خطأ أثناء تحميل الصوت: ${e.localizedMessage}"
                )
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
            stopProgressTracker()
        } else {
            player.start()
            _playbackState.value = _playbackState.value.copy(isPlaying = true)
            startProgressTracker()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        val clamped = positionMs.coerceIn(0L, _playbackState.value.durationMs)
        player.seekTo(clamped.toInt())
        _playbackState.value = _playbackState.value.copy(currentPositionMs = clamped)
    }

    fun seekRelative(offsetMs: Long) {
        val current = _playbackState.value.currentPositionMs
        seekTo(current + offsetMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
        mediaPlayer?.let { mp ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    mp.playbackParams = mp.playbackParams.setSpeed(speed)
                } catch (_: Exception) {}
            }
        }
    }

    fun toggleLooping() {
        val next = !_playbackState.value.isLooping
        _playbackState.value = _playbackState.value.copy(isLooping = next)
        mediaPlayer?.isLooping = next
    }

    fun stop() {
        stopProgressTracker()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _playbackState.value = PlayerPlaybackState()
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackerJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _playbackState.value = _playbackState.value.copy(
                            currentPositionMs = mp.currentPosition.toLong(),
                            durationMs = mp.duration.toLong()
                        )
                    }
                }
                delay(300)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }

    // Download functionality
    suspend fun downloadTrack(
        track: AudioTrack,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val destinationFile = getLocalFile(track.reciter.id, track.surahNumber)
            val url = URL(track.streamUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode !in 200..299) {
                return@withContext false
            }

            val fileLength = connection.contentLength
            val input = connection.inputStream
            val output = FileOutputStream(destinationFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count
                output.write(data, 0, count)
                if (fileLength > 0) {
                    onProgress(total.toFloat() / fileLength.toFloat())
                }
            }

            output.flush()
            output.close()
            input.close()

            withContext(Dispatchers.Main) {
                val current = _playbackState.value.currentTrack
                if (current != null && current.surahNumber == track.surahNumber && current.reciter.id == track.reciter.id) {
                    _playbackState.value = _playbackState.value.copy(
                        currentTrack = current.copy(
                            isDownloaded = true,
                            localFilePath = destinationFile.absolutePath
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteDownloadedTrack(reciterId: String, surahNumber: Int) {
        val file = getLocalFile(reciterId, surahNumber)
        if (file.exists()) {
            file.delete()
        }
        val current = _playbackState.value.currentTrack
        if (current != null && current.surahNumber == surahNumber && current.reciter.id == reciterId) {
            _playbackState.value = _playbackState.value.copy(
                currentTrack = current.copy(isDownloaded = false, localFilePath = null)
            )
        }
    }
}
