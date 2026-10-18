package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.Ayah
import com.example.model.DuaCategory
import com.example.model.DuaItem
import com.example.model.Surah

@Entity(
    tableName = "surahs",
    indices = [
        Index(value = ["number"], unique = true),
        Index(value = ["name"]),
        Index(value = ["englishName"])
    ]
)
data class SurahEntity(
    @PrimaryKey
    val number: Int,
    val name: String,
    val englishName: String,
    val revelationType: String, // مكية أو مدنية
    val numberOfAyahs: Int,
    val juzNumber: Int,
    val pageNumber: Int,
    val description: String = ""
)

@Entity(
    tableName = "ayahs",
    indices = [
        Index(value = ["surahNumber"]),
        Index(value = ["surahNumber", "numberInSurah"]),
        Index(value = ["text"])
    ]
)
data class AyahEntity(
    @PrimaryKey
    val id: String, // e.g. "ayah_1_1"
    val surahNumber: Int,
    val numberInSurah: Int,
    val text: String,
    val translation: String = "",
    val juz: Int = 1,
    val page: Int = 1
)

@Entity(
    tableName = "dua_categories",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["name"])
    ]
)
data class DuaCategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val englishName: String,
    val count: Int,
    val icon: String,
    val description: String = ""
)

@Entity(
    tableName = "dua_items",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["categoryId"]),
        Index(value = ["title"]),
        Index(value = ["arabicText"])
    ]
)
data class DuaItemEntity(
    @PrimaryKey
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val title: String,
    val arabicText: String,
    val reference: String,
    val virtue: String = "",
    val targetCount: Int = 1
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val id: String, // e.g. "surah_18_10" or "dua_sabah_1"
    val type: String, // "SURAH" or "DUA"
    val title: String,
    val subtitle: String,
    val surahNumber: Int? = null,
    val ayahNumber: Int? = null,
    val duaId: String? = null,
    val contentSnippet: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_position")
data class LastReadEntity(
    @PrimaryKey
    val id: Int = 1, // single row
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_packages")
data class DownloadedPackageEntity(
    @PrimaryKey
    val packageId: String,
    val title: String,
    val downloadedAt: Long = System.currentTimeMillis()
)

// Extension mappers between Entities and Domain Models
fun SurahEntity.toDomain(): Surah = Surah(
    number = number,
    name = name,
    englishName = englishName,
    revelationType = revelationType,
    numberOfAyahs = numberOfAyahs,
    juzNumber = juzNumber,
    pageNumber = pageNumber,
    description = description
)

fun Surah.toEntity(): SurahEntity = SurahEntity(
    number = number,
    name = name,
    englishName = englishName,
    revelationType = revelationType,
    numberOfAyahs = numberOfAyahs,
    juzNumber = juzNumber,
    pageNumber = pageNumber,
    description = description
)

fun AyahEntity.toDomain(): Ayah = Ayah(
    surahNumber = surahNumber,
    numberInSurah = numberInSurah,
    text = text,
    translation = translation,
    juz = juz,
    page = page
)

fun Ayah.toEntity(): AyahEntity = AyahEntity(
    id = "ayah_${surahNumber}_${numberInSurah}",
    surahNumber = surahNumber,
    numberInSurah = numberInSurah,
    text = text,
    translation = translation,
    juz = juz,
    page = page
)

fun DuaCategoryEntity.toDomain(): DuaCategory = DuaCategory(
    id = id,
    name = name,
    englishName = englishName,
    count = count,
    icon = icon,
    description = description
)

fun DuaCategory.toEntity(): DuaCategoryEntity = DuaCategoryEntity(
    id = id,
    name = name,
    englishName = englishName,
    count = count,
    icon = icon,
    description = description
)

fun DuaItemEntity.toDomain(): DuaItem = DuaItem(
    id = id,
    categoryId = categoryId,
    categoryName = categoryName,
    title = title,
    arabicText = arabicText,
    reference = reference,
    virtue = virtue,
    targetCount = targetCount
)

fun DuaItem.toEntity(): DuaItemEntity = DuaItemEntity(
    id = id,
    categoryId = categoryId,
    categoryName = categoryName,
    title = title,
    arabicText = arabicText,
    reference = reference,
    virtue = virtue,
    targetCount = targetCount
)
