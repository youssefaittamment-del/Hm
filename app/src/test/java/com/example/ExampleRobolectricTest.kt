package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DuaData
import com.example.data.QuranData
import com.example.data.local.AppDatabase
import com.example.data.local.AyahEntity
import com.example.data.local.DuaCategoryEntity
import com.example.data.local.DuaItemEntity
import com.example.data.local.SurahEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("نور القرآن والأدعية", appName)
  }

  @Test
  fun `verify quran data integrity`() {
    assertEquals(114, QuranData.surahs.size)
    val fatiha = QuranData.getSurahByNumber(1)
    assertNotNull(fatiha)
    assertEquals("الفاتحة", fatiha?.name)
    val ayahs = QuranData.getAyahsForSurah(1)
    assertEquals(7, ayahs.size)
  }

  @Test
  fun `verify room database entities and daos operations`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    val surahDao = db.surahDao()
    val ayahDao = db.ayahDao()
    val duaDao = db.duaDao()

    // Test inserting surah
    val testSurah = SurahEntity(
        number = 1,
        name = "الفاتحة",
        englishName = "Al-Fatiha",
        revelationType = "مكية",
        numberOfAyahs = 7,
        juzNumber = 1,
        pageNumber = 1,
        description = "أم الكتاب"
    )
    surahDao.insertSurahs(listOf(testSurah))
    val retrieved = surahDao.getSurahByNumber(1)
    assertNotNull(retrieved)
    assertEquals("الفاتحة", retrieved?.name)

    // Test inserting ayahs and search query
    val testAyah = AyahEntity(
        id = "ayah_1_1",
        surahNumber = 1,
        numberInSurah = 1,
        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        translation = "In the name of Allah",
        juz = 1,
        page = 1
    )
    ayahDao.insertAyahs(listOf(testAyah))
    val ayahs = ayahDao.getAyahsForSurah(1)
    assertEquals(1, ayahs.size)
    assertEquals("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", ayahs[0].text)

    // Test search in ayahs
    val searchResults = ayahDao.searchAyahs("الرَّحْمَٰنِ")
    assertTrue(searchResults.isNotEmpty())

    // Test Dua entities and Dao
    val testDuaCat = DuaCategoryEntity(
        id = "sabah",
        name = "أذكار الصباح",
        englishName = "Morning",
        count = 10,
        icon = "wb_sunny"
    )
    duaDao.insertCategories(listOf(testDuaCat))
    val cat = duaDao.getCategoryById("sabah")
    assertNotNull(cat)

    val testDua = DuaItemEntity(
        id = "test_1",
        categoryId = "sabah",
        categoryName = "أذكار الصباح",
        title = "أصبحنا وأصبح الملك لله",
        arabicText = "أصبحنا وأصبح الملك لله والحمد لله",
        reference = "صحيح مسلم"
    )
    duaDao.insertDuas(listOf(testDua))
    val duas = duaDao.getDuasForCategory("sabah")
    assertEquals(1, duas.size)
    assertEquals("أصبحنا وأصبح الملك لله", duas[0].title)
  }

  @Test
  fun `verify theme persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.repository.QuranRepository(context)
    repo.setThemeMode(com.example.ui.theme.ThemeMode.DARK)
    assertEquals(com.example.ui.theme.ThemeMode.DARK, repo.getThemeMode())
    repo.setThemeMode(com.example.ui.theme.ThemeMode.LIGHT)
    assertEquals(com.example.ui.theme.ThemeMode.LIGHT, repo.getThemeMode())
  }

  @Test
  fun `verify prayer times and adhkar reminders`() {
    val makkah = com.example.data.PrayerData.getCityByName("مكة المكرمة")
    assertEquals("مكة المكرمة", makkah.nameArabic)
    val prayers = com.example.data.PrayerData.getPrayerTimesForCity(makkah)
    assertEquals(6, prayers.size) // Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha

    val reminders = com.example.data.PrayerData.defaultAdhkarReminders
    assertEquals(3, reminders.size) // Morning, Evening, Sleep
    assertTrue(reminders.any { it.id == "sabah" })
    assertTrue(reminders.any { it.id == "massa" })
    assertTrue(reminders.any { it.id == "sleep" })
  }

  @Test
  fun `verify tasbih counter operations`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = com.example.data.repository.QuranRepository(context)
    repo.setTasbihCount(15)
    assertEquals(15, repo.getTasbihCount())
    repo.setTasbihTarget(100)
    assertEquals(100, repo.getTasbihTarget())
    repo.setTasbihDhikr("الْحَمْدُ لِلَّهِ")
    assertEquals("الْحَمْدُ لِلَّهِ", repo.getTasbihDhikr())
  }
}
