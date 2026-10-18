package com.example.model

data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val revelationType: String, // مكية أو مدنية
    val numberOfAyahs: Int,
    val juzNumber: Int,
    val pageNumber: Int,
    val description: String = ""
)

data class Ayah(
    val surahNumber: Int,
    val numberInSurah: Int,
    val text: String,
    val translation: String = "",
    val juz: Int = 1,
    val page: Int = 1
)

data class DuaCategory(
    val id: String,
    val name: String,
    val englishName: String,
    val count: Int,
    val icon: String,
    val description: String = ""
)

data class DuaItem(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val title: String,
    val arabicText: String,
    val reference: String,
    val virtue: String = "",
    val targetCount: Int = 1
)

data class ReadingPosition(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class OfflinePackage(
    val id: String,
    val title: String,
    val description: String,
    val sizeText: String,
    val isDownloaded: Boolean,
    val progress: Float = 0f
)
