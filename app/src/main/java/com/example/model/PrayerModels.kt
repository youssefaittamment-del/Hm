package com.example.model

data class PrayerTime(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val timeFormatted: String, // e.g. "05:12 ص"
    val time24: String, // "05:12"
    val isNext: Boolean = false,
    val isAthanEnabled: Boolean = true
)

data class CityPreset(
    val nameArabic: String,
    val nameEnglish: String,
    val countryArabic: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

data class AdhkarReminder(
    val id: String, // "sabah", "massa", "sleep"
    val titleArabic: String,
    val categoryId: String,
    val timeFormatted: String, // e.g. "06:00 ص"
    val isEnabled: Boolean = true,
    val iconEmoji: String,
    val description: String
)
