package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
    @Query("SELECT * FROM surahs ORDER BY number ASC")
    fun getAllSurahsFlow(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs ORDER BY number ASC")
    suspend fun getAllSurahs(): List<SurahEntity>

    @Query("SELECT * FROM surahs WHERE number = :number LIMIT 1")
    suspend fun getSurahByNumber(number: Int): SurahEntity?

    @Query("SELECT * FROM surahs WHERE name LIKE '%' || :query || '%' OR englishName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY number ASC")
    fun searchSurahsFlow(query: String): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs WHERE name LIKE '%' || :query || '%' OR englishName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY number ASC")
    suspend fun searchSurahs(query: String): List<SurahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun getSurahsCount(): Int
}

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah ASC")
    fun getAyahsForSurahFlow(surahNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah ASC")
    suspend fun getAyahsForSurah(surahNumber: Int): List<AyahEntity>

    @Query("SELECT * FROM ayahs WHERE text LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%' ORDER BY surahNumber ASC, numberInSurah ASC")
    fun searchAyahsFlow(query: String): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE text LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%' ORDER BY surahNumber ASC, numberInSurah ASC")
    suspend fun searchAyahs(query: String): List<AyahEntity>

    @Query("SELECT * FROM ayahs WHERE id = :id LIMIT 1")
    suspend fun getAyahById(id: String): AyahEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun getAyahsCount(): Int
}

@Dao
interface DuaDao {
    @Query("SELECT * FROM dua_categories ORDER BY id ASC")
    fun getAllCategoriesFlow(): Flow<List<DuaCategoryEntity>>

    @Query("SELECT * FROM dua_categories ORDER BY id ASC")
    suspend fun getAllCategories(): List<DuaCategoryEntity>

    @Query("SELECT * FROM dua_categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): DuaCategoryEntity?

    @Query("SELECT * FROM dua_items WHERE categoryId = :categoryId ORDER BY id ASC")
    fun getDuasForCategoryFlow(categoryId: String): Flow<List<DuaItemEntity>>

    @Query("SELECT * FROM dua_items WHERE categoryId = :categoryId ORDER BY id ASC")
    suspend fun getDuasForCategory(categoryId: String): List<DuaItemEntity>

    @Query("SELECT * FROM dua_items ORDER BY id ASC")
    fun getAllDuasFlow(): Flow<List<DuaItemEntity>>

    @Query("SELECT * FROM dua_items ORDER BY id ASC")
    suspend fun getAllDuas(): List<DuaItemEntity>

    @Query("SELECT * FROM dua_items WHERE title LIKE '%' || :query || '%' OR arabicText LIKE '%' || :query || '%' OR virtue LIKE '%' || :query || '%' OR categoryName LIKE '%' || :query || '%'")
    fun searchDuasFlow(query: String): Flow<List<DuaItemEntity>>

    @Query("SELECT * FROM dua_items WHERE title LIKE '%' || :query || '%' OR arabicText LIKE '%' || :query || '%' OR virtue LIKE '%' || :query || '%' OR categoryName LIKE '%' || :query || '%'")
    suspend fun searchDuas(query: String): List<DuaItemEntity>

    @Query("SELECT * FROM dua_categories WHERE name LIKE '%' || :query || '%' OR englishName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    suspend fun searchCategories(query: String): List<DuaCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<DuaCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuas(duas: List<DuaItemEntity>)

    @Query("SELECT COUNT(*) FROM dua_items")
    suspend fun getDuasCount(): Int

    @Query("SELECT COUNT(*) FROM dua_categories")
    suspend fun getCategoriesCount(): Int
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE type = :type ORDER BY createdAt DESC")
    fun getBookmarksByType(type: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    fun isBookmarkedFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    suspend fun isBookmarked(id: String): Boolean
}

@Dao
interface ReadingPositionDao {
    @Query("SELECT * FROM reading_position WHERE id = 1 LIMIT 1")
    fun getLastRead(): Flow<LastReadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLastRead(entity: LastReadEntity)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloaded_packages")
    fun getAllDownloaded(): Flow<List<DownloadedPackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAsDownloaded(entity: DownloadedPackageEntity)

    @Query("DELETE FROM downloaded_packages WHERE packageId = :packageId")
    suspend fun deleteDownloaded(packageId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_packages WHERE packageId = :packageId)")
    fun isPackageDownloaded(packageId: String): Flow<Boolean>
}
