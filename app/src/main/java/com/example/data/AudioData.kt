package com.example.data

import com.example.model.Reciter

object AudioData {

    val reciters: List<Reciter> = listOf(
        Reciter(
            id = "alafasy",
            nameArabic = "مشاري بن راشد العفاسي",
            nameEnglish = "Mishary Alafasy",
            style = "مرتل",
            serverUrl = "https://server8.mp3quran.net/afs/",
            avatarEmoji = "🎙️"
        ),
        Reciter(
            id = "abdulbasit",
            nameArabic = "عبد الباسط عبد الصمد",
            nameEnglish = "Abdul Basit Abdul Samad",
            style = "مرتل",
            serverUrl = "https://server7.mp3quran.net/basit/",
            avatarEmoji = "✨"
        ),
        Reciter(
            id = "al_hussary",
            nameArabic = "محمود خليل الحصري",
            nameEnglish = "Mahmoud Khalil Al-Hussary",
            style = "معلم / مرتل",
            serverUrl = "https://server13.mp3quran.net/husr/",
            avatarEmoji = "📖"
        ),
        Reciter(
            id = "saad_alghamdi",
            nameArabic = "سعد بن سعيد الغامدي",
            nameEnglish = "Saad Al-Ghamdi",
            style = "مرتل",
            serverUrl = "https://server7.mp3quran.net/s_gmd/",
            avatarEmoji = "🌿"
        ),
        Reciter(
            id = "maher_muaiqly",
            nameArabic = "ماهر بن حمد المعيقلي",
            nameEnglish = "Maher Al-Muaiqly",
            style = "إمام الحرم المكي",
            serverUrl = "https://server12.mp3quran.net/maher/",
            avatarEmoji = "🕋"
        ),
        Reciter(
            id = "ahmed_alajmi",
            nameArabic = "أحمد بن علي العجمي",
            nameEnglish = "Ahmed Al-Ajmi",
            style = "مرتل",
            serverUrl = "https://server10.mp3quran.net/ajm/",
            avatarEmoji = "🌟"
        )
    )

    val shortSurahNumbers: List<Int> = listOf(1) + (93..114).toList()

    fun isShortSurah(surahNumber: Int): Boolean {
        return shortSurahNumbers.contains(surahNumber)
    }

    fun getNextShortSurah(currentSurahNumber: Int): Int {
        val currentIndex = shortSurahNumbers.indexOf(currentSurahNumber)
        return if (currentIndex == -1 || currentIndex >= shortSurahNumbers.size - 1) {
            shortSurahNumbers.first()
        } else {
            shortSurahNumbers[currentIndex + 1]
        }
    }

    fun getPreviousShortSurah(currentSurahNumber: Int): Int {
        val currentIndex = shortSurahNumbers.indexOf(currentSurahNumber)
        return if (currentIndex <= 0) {
            shortSurahNumbers.last()
        } else {
            shortSurahNumbers[currentIndex - 1]
        }
    }

    fun getAudioUrl(reciter: Reciter, surahNumber: Int): String {
        val formattedNumber = String.format("%03d", surahNumber)
        return "${reciter.serverUrl}$formattedNumber.mp3"
    }

    fun getReciterById(id: String): Reciter {
        return reciters.find { it.id == id } ?: reciters[0]
    }
}
