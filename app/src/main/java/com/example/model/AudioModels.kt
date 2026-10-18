package com.example.model

data class Reciter(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val style: String, // مرتل / مجود
    val serverUrl: String,
    val avatarEmoji: String = "🎙️"
)

data class AudioTrack(
    val surahNumber: Int,
    val surahName: String,
    val reciter: Reciter,
    val streamUrl: String,
    val localFilePath: String? = null,
    val isDownloaded: Boolean = false
)

data class PlayerPlaybackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentTrack: AudioTrack? = null,
    val playbackSpeed: Float = 1.0f,
    val isLooping: Boolean = false,
    val errorMessage: String? = null
)
