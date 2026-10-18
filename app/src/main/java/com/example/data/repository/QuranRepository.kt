package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.DuaData
import com.example.data.QuranData
import com.example.data.local.AppDatabase
import com.example.data.local.AyahEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.DownloadedPackageEntity
import com.example.data.local.LastReadEntity
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.model.Ayah
import com.example.model.DuaCategory
import com.example.model.DuaItem
import com.example.model.OfflinePackage
import com.example.model.ReadingPosition
import com.example.model.Surah
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    val bookmarkDao = db.bookmarkDao()
    val readingDao = db.readingPositionDao()
    val downloadDao = db.downloadDao()
    val surahDao = db.surahDao()
    val ayahDao = db.ayahDao()
    val duaDao = db.duaDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("quran_noor_settings", Context.MODE_PRIVATE)

    init {
        // Prepopulate Room Database in background if empty
        CoroutineScope(Dispatchers.IO).launch {
            ensureDatabasePopulated()
        }
    }

    suspend fun ensureDatabasePopulated() = withContext(Dispatchers.IO) {
        try {
            if (surahDao.getSurahsCount() == 0) {
                val surahEntities = QuranData.surahs.map { it.toEntity() }
                surahDao.insertSurahs(surahEntities)

                val allAyahs = mutableListOf<AyahEntity>()
                for (surah in QuranData.surahs) {
                    val ayahs = QuranData.getAyahsForSurah(surah.number)
                    for (ayah in ayahs) {
                        allAyahs.add(ayah.toEntity())
                    }
                }
                ayahDao.insertAyahs(allAyahs)
            }

            if (duaDao.getCategoriesCount() == 0) {
                val catEntities = DuaData.categories.map { it.toEntity() }
                duaDao.insertCategories(catEntities)
            }

            if (duaDao.getDuasCount() == 0) {
                val duaEntities = DuaData.duas.map { it.toEntity() }
                duaDao.insertDuas(duaEntities)
            }
        } catch (_: Exception) {
            // Graceful fallback to in-memory datasets if SQLite encountered issues
        }
    }

    // Reactive Flows from Room Database
    val surahsFlow: Flow<List<Surah>> = surahDao.getAllSurahsFlow().map { list ->
        if (list.isEmpty()) QuranData.surahs else list.map { it.toDomain() }
    }

    val duaCategoriesFlow: Flow<List<DuaCategory>> = duaDao.getAllCategoriesFlow().map { list ->
        if (list.isEmpty()) DuaData.categories else list.map { it.toDomain() }
    }

    val allDuasFlow: Flow<List<DuaItem>> = duaDao.getAllDuasFlow().map { list ->
        if (list.isEmpty()) DuaData.duas else list.map { it.toDomain() }
    }

    fun getAyahsFlow(surahNumber: Int): Flow<List<Ayah>> =
        ayahDao.getAyahsForSurahFlow(surahNumber).map { list ->
            if (list.isEmpty()) QuranData.getAyahsForSurah(surahNumber) else list.map { it.toDomain() }
        }

    fun getDuasByCategoryFlow(categoryId: String): Flow<List<DuaItem>> =
        duaDao.getDuasForCategoryFlow(categoryId).map { list ->
            if (list.isEmpty()) DuaData.getDuasByCategory(categoryId) else list.map { it.toDomain() }
        }

    // Direct / Suspend Database Operations
    suspend fun getSurahFromDb(number: Int): Surah? = withContext(Dispatchers.IO) {
        surahDao.getSurahByNumber(number)?.toDomain() ?: QuranData.getSurahByNumber(number)
    }

    suspend fun getAyahsFromDb(surahNumber: Int): List<Ayah> = withContext(Dispatchers.IO) {
        val list = ayahDao.getAyahsForSurah(surahNumber)
        if (list.isEmpty()) QuranData.getAyahsForSurah(surahNumber) else list.map { it.toDomain() }
    }

    suspend fun searchSurahsInDb(query: String): List<Surah> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            val all = surahDao.getAllSurahs()
            if (all.isEmpty()) QuranData.surahs else all.map { it.toDomain() }
        } else {
            val results = surahDao.searchSurahs(query.trim())
            if (results.isEmpty()) QuranData.searchQuran(query) else results.map { it.toDomain() }
        }
    }

    suspend fun searchAyahsInDb(query: String): List<Ayah> = withContext(Dispatchers.IO) {
        if (query.isBlank()) emptyList()
        else ayahDao.searchAyahs(query.trim()).map { it.toDomain() }
    }

    suspend fun searchDuasInDb(query: String): List<DuaItem> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            val all = duaDao.getAllDuas()
            if (all.isEmpty()) DuaData.duas else all.map { it.toDomain() }
        } else {
            val results = duaDao.searchDuas(query.trim())
            if (results.isEmpty()) DuaData.searchDuas(query) else results.map { it.toDomain() }
        }
    }

    // Synchronous fallback / immediate methods for UI bindings
    fun getAllSurahs(): List<Surah> = QuranData.surahs

    fun getSurah(number: Int): Surah? = QuranData.getSurahByNumber(number)

    fun getAyahs(surahNumber: Int): List<Ayah> = QuranData.getAyahsForSurah(surahNumber)

    fun searchSurahs(query: String): List<Surah> = QuranData.searchQuran(query)

    fun getAllDuaCategories(): List<DuaCategory> = DuaData.categories

    fun getDuasByCategory(categoryId: String): List<DuaItem> = DuaData.getDuasByCategory(categoryId)

    fun searchDuas(query: String): List<DuaItem> = DuaData.searchDuas(query)

    fun searchDuaCategories(query: String): List<DuaCategory> = DuaData.searchCategories(query)

    // Font size settings
    fun getFontSize(): Float = prefs.getFloat("arabic_font_size", 24f)
    fun setFontSize(size: Float) = prefs.edit().putFloat("arabic_font_size", size).apply()

    // Theme mode settings
    fun getThemeMode(): com.example.ui.theme.ThemeMode {
        val modeName = prefs.getString("theme_mode", com.example.ui.theme.ThemeMode.SYSTEM.name)
            ?: com.example.ui.theme.ThemeMode.SYSTEM.name
        return try {
            com.example.ui.theme.ThemeMode.valueOf(modeName)
        } catch (_: Exception) {
            com.example.ui.theme.ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: com.example.ui.theme.ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    // Bookmarking & Last Reading Position
    val bookmarksFlow: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    suspend fun toggleSurahBookmark(surah: Surah, ayahNumber: Int = 1) {
        val id = "surah_${surah.number}_$ayahNumber"
        val exists = bookmarkDao.isBookmarked(id)
        if (exists) {
            bookmarkDao.deleteBookmarkById(id)
        } else {
            val ayah = getAyahs(surah.number).find { it.numberInSurah == ayahNumber }
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    id = id,
                    type = "SURAH",
                    title = "سورة ${surah.name}",
                    subtitle = "الآية رقم $ayahNumber • ${surah.revelationType}",
                    surahNumber = surah.number,
                    ayahNumber = ayahNumber,
                    contentSnippet = ayah?.text ?: "سورة ${surah.name} الآية $ayahNumber"
                )
            )
        }
    }

    suspend fun toggleDuaBookmark(dua: DuaItem) {
        val id = "dua_${dua.id}"
        val exists = bookmarkDao.isBookmarked(id)
        if (exists) {
            bookmarkDao.deleteBookmarkById(id)
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    id = id,
                    type = "DUA",
                    title = dua.title,
                    subtitle = dua.categoryName,
                    duaId = dua.id,
                    contentSnippet = dua.arabicText
                )
            )
        }
    }

    suspend fun isBookmarked(id: String): Boolean = bookmarkDao.isBookmarked(id)

    fun isBookmarkedFlow(id: String): Flow<Boolean> = bookmarkDao.isBookmarkedFlow(id)

    suspend fun deleteBookmark(id: String) {
        bookmarkDao.deleteBookmarkById(id)
    }

    // Reading Position
    val lastReadingPositionFlow: Flow<ReadingPosition?> = readingDao.getLastRead().map { entity ->
        entity?.let {
            ReadingPosition(
                surahNumber = it.surahNumber,
                surahName = it.surahName,
                ayahNumber = it.ayahNumber,
                timestamp = it.updatedAt
            )
        }
    }

    suspend fun saveReadingPosition(surahNumber: Int, surahName: String, ayahNumber: Int) {
        readingDao.saveLastRead(
            LastReadEntity(
                id = 1,
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = ayahNumber,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // Offline download management
    val defaultPackages = listOf(
        OfflinePackage(
            id = "pkg_quran_text",
            title = "المصحف الشريف كاملاً (نصوص وتفسير ميسر)",
            description = "جميع سور القرآن الـ ١١٤ وآياتها بالرسم العثماني مع التفسير والبحث بدون نت",
            sizeText = "١٤ ميجابايت",
            isDownloaded = true
        ),
        OfflinePackage(
            id = "pkg_duas_full",
            title = "موسوعة الأذكار والأدعية الشاملة وحصن المسلم",
            description = "أذكار الصباح والمساء وأدعية القرآن والسنة والشفاء مع العداد الإلكتروني",
            sizeText = "٨ ميجابايت",
            isDownloaded = true
        ),
        OfflinePackage(
            id = "pkg_tafseer_muyassar",
            title = "حزمة التفسير الميسر وأسباب النزول",
            description = "شرح معاني الكلمات والغريب وأسباب نزول الآيات الكريمة بدون إنترنت",
            sizeText = "٢٢ ميجابايت",
            isDownloaded = false
        ),
        OfflinePackage(
            id = "pkg_recitations_selected",
            title = "تلاوات قرآنية مختارة لأشهر القراء",
            description = "تلاوات خاشعة لسور الكهف والرحمن ويس والملك بدون اتصال بالشبكة",
            sizeText = "٤٥ ميجابايت",
            isDownloaded = false
        )
    )

    val downloadedPackagesFlow: Flow<List<DownloadedPackageEntity>> = downloadDao.getAllDownloaded()

    suspend fun downloadPackage(pkg: OfflinePackage) {
        downloadDao.markAsDownloaded(
            DownloadedPackageEntity(
                packageId = pkg.id,
                title = pkg.title,
                downloadedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removePackage(pkgId: String) {
        downloadDao.deleteDownloaded(pkgId)
    }

    // Prayer & Adhkar Settings
    fun getSelectedCity(): String =
        prefs.getString("selected_prayer_city", "مكة المكرمة") ?: "مكة المكرمة"

    fun setSelectedCity(cityName: String) {
        prefs.edit().putString("selected_prayer_city", cityName).apply()
    }

    fun isPrayerAthanEnabled(prayerId: String): Boolean =
        prefs.getBoolean("athan_$prayerId", true)

    fun setPrayerAthanEnabled(prayerId: String, enabled: Boolean) {
        prefs.edit().putBoolean("athan_$prayerId", enabled).apply()
    }

    fun isAdhkarReminderEnabled(reminderId: String): Boolean =
        prefs.getBoolean("adhkar_reminder_$reminderId", true)

    fun setAdhkarReminderEnabled(reminderId: String, enabled: Boolean) {
        prefs.edit().putBoolean("adhkar_reminder_$reminderId", enabled).apply()
    }

    fun getAdhkarReminderTime(reminderId: String, defaultTime: String): String =
        prefs.getString("adhkar_time_$reminderId", defaultTime) ?: defaultTime

    fun setAdhkarReminderTime(reminderId: String, time: String) {
        prefs.edit().putString("adhkar_time_$reminderId", time).apply()
    }

    // Digital Tasbih Settings
    fun getTasbihCount(): Int = prefs.getInt("tasbih_current_count", 0)
    fun setTasbihCount(count: Int) = prefs.edit().putInt("tasbih_current_count", count).apply()

    fun getTasbihTotalCount(): Int = prefs.getInt("tasbih_total_count", 0)
    fun setTasbihTotalCount(total: Int) = prefs.edit().putInt("tasbih_total_count", total).apply()

    fun getTasbihTarget(): Int = prefs.getInt("tasbih_target", 33)
    fun setTasbihTarget(target: Int) = prefs.edit().putInt("tasbih_target", target).apply()

    fun getTasbihDhikr(): String = prefs.getString("tasbih_dhikr", "سُبْحَانَ اللَّهِ") ?: "سُبْحَانَ اللَّهِ"
    fun setTasbihDhikr(dhikr: String) = prefs.edit().putString("tasbih_dhikr", dhikr).apply()

    fun isTasbihVibrationEnabled(): Boolean = prefs.getBoolean("tasbih_vibration", true)
    fun setTasbihVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean("tasbih_vibration", enabled).apply()
}
