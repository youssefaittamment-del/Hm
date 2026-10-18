package com.example.data

import com.example.model.AdhkarReminder
import com.example.model.CityPreset
import com.example.model.PrayerTime
import java.util.Calendar

object PrayerData {

    val cities: List<CityPreset> = listOf(
        CityPreset("مكة المكرمة", "Makkah", "المملكة العربية السعودية", "05:05", "06:22", "12:15", "15:37", "18:02", "19:32"),
        CityPreset("المدينة المنورة", "Madinah", "المملكة العربية السعودية", "05:07", "06:26", "12:16", "15:38", "18:03", "19:33"),
        CityPreset("الرياض", "Riyadh", "المملكة العربية السعودية", "04:45", "06:03", "11:51", "15:13", "17:36", "19:06"),
        CityPreset("القاهرة", "Cairo", "مصر", "04:38", "06:05", "11:43", "14:58", "17:21", "18:40"),
        CityPreset("القدس الشريف", "Jerusalem", "فلسطين", "05:12", "06:34", "12:21", "15:36", "18:04", "19:24"),
        CityPreset("دبي", "Dubai", "الإمارات العربية المتحدة", "05:02", "06:20", "12:11", "15:33", "17:58", "19:15"),
        CityPreset("عمان", "Amman", "الأردن", "05:11", "06:33", "12:20", "15:35", "18:03", "19:23"),
        CityPreset("بغداد", "Baghdad", "العراق", "04:58", "06:21", "12:02", "15:16", "17:41", "19:02"),
        CityPreset("الجزائر", "Algiers", "الجزائر", "05:22", "06:51", "12:35", "15:47", "18:16", "19:38"),
        CityPreset("الدار البيضاء", "Casablanca", "المغرب", "05:43", "07:11", "12:56", "16:08", "18:37", "19:59"),
        CityPreset("إسطنبول", "Istanbul", "تركيا", "05:31", "07:05", "12:47", "15:52", "18:23", "19:48")
    )

    fun getCityByName(name: String): CityPreset {
        return cities.find { it.nameArabic == name } ?: cities[0]
    }

    fun getPrayerTimesForCity(city: CityPreset, now: Calendar = Calendar.getInstance()): List<PrayerTime> {
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val rawTimes = listOf(
            Triple("fajr", "الفجر", city.fajr),
            Triple("sunrise", "الشروق", city.sunrise),
            Triple("dhuhr", "الظهر", city.dhuhr),
            Triple("asr", "العصر", city.asr),
            Triple("maghrib", "المغرب", city.maghrib),
            Triple("isha", "العشاء", city.isha)
        )

        // Find which prayer is next
        var nextFound = false
        val prayers = mutableListOf<PrayerTime>()

        for ((id, name, time24) in rawTimes) {
            val parts = time24.split(":")
            val prayerMinutes = parts[0].toInt() * 60 + parts[1].toInt()
            val isNext = !nextFound && (prayerMinutes > currentMinutes)
            if (isNext) nextFound = true

            prayers.add(
                PrayerTime(
                    id = id,
                    nameArabic = name,
                    nameEnglish = when (id) {
                        "fajr" -> "Fajr"
                        "sunrise" -> "Sunrise"
                        "dhuhr" -> "Dhuhr"
                        "asr" -> "Asr"
                        "maghrib" -> "Maghrib"
                        else -> "Isha"
                    },
                    timeFormatted = formatTo12Hour(time24),
                    time24 = time24,
                    isNext = isNext
                )
            )
        }

        // If all prayers passed today, tomorrow's Fajr is next!
        if (!nextFound && prayers.isNotEmpty()) {
            val first = prayers[0]
            prayers[0] = first.copy(isNext = true)
        }

        return prayers
    }

    fun getNextPrayerInfo(prayers: List<PrayerTime>, now: Calendar = Calendar.getInstance()): Pair<PrayerTime, String> {
        val next = prayers.find { it.isNext } ?: prayers.first()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val parts = next.time24.split(":")
        var targetMinutes = parts[0].toInt() * 60 + parts[1].toInt()

        if (targetMinutes <= currentMinutes) {
            targetMinutes += 24 * 60 // next day
        }

        val diff = targetMinutes - currentMinutes
        val hours = diff / 60
        val mins = diff % 60

        val remainingText = when {
            hours > 0 && mins > 0 -> "متبقي $hours ساعة و $mins دقيقة"
            hours > 0 -> "متبقي $hours ساعة"
            else -> "متبقي $mins دقيقة فقط"
        }

        return Pair(next, remainingText)
    }

    private fun formatTo12Hour(time24: String): String {
        val parts = time24.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt()
        val isPm = h >= 12
        val h12 = if (h % 12 == 0) 12 else h % 12
        val period = if (isPm) "م" else "ص"
        val mStr = if (m < 10) "0$m" else "$m"
        val hStr = if (h12 < 10) "0$h12" else "$h12"
        return "$hStr:$mStr $period"
    }

    val defaultAdhkarReminders: List<AdhkarReminder> = listOf(
        AdhkarReminder(
            id = "sabah",
            titleArabic = "أذكار الصباح",
            categoryId = "sabah",
            timeFormatted = "06:00 ص",
            isEnabled = true,
            iconEmoji = "☀️",
            description = "تحصين النفس والبركة في الرزق والعمر مع إشراقة الصباح"
        ),
        AdhkarReminder(
            id = "massa",
            titleArabic = "أذكار المساء",
            categoryId = "massa",
            timeFormatted = "05:00 م",
            isEnabled = true,
            iconEmoji = "🌙",
            description = "سكينة وطمأنينة وحفظ من الشرور قبل غروب الشمس"
        ),
        AdhkarReminder(
            id = "sleep",
            titleArabic = "أذكار النوم",
            categoryId = "sleep",
            timeFormatted = "10:30 م",
            isEnabled = true,
            iconEmoji = "🛌",
            description = "آية الكرسي والمعوذات وباسمك ربي وضعت جنبي لنوم هانئ"
        )
    )
}
