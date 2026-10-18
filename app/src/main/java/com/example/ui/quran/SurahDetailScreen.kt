package com.example.ui.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookmarkEntity
import com.example.model.Ayah
import com.example.model.PlayerPlaybackState
import com.example.model.Surah
import com.example.ui.audio.MiniAudioPlayer

@Composable
fun SurahDetailScreen(
    surah: Surah,
    ayahs: List<Ayah>,
    fontSize: Float,
    isDarkMode: Boolean,
    bookmarks: List<BookmarkEntity>,
    onBack: () -> Unit,
    onSaveReadingPosition: (Int) -> Unit,
    onToggleAyahBookmark: (Int) -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleFontSize: () -> Unit,
    onPlayAudio: () -> Unit = {},
    isPlayingThisSurah: Boolean = false,
    playbackState: PlayerPlaybackState? = null,
    onOpenFullPlayer: () -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onStopAudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("surah_detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "سورة ${surah.name}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "${surah.revelationType} • ${surah.numberOfAyahs} آيات • الجزء ${surah.juzNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onPlayAudio,
                            modifier = Modifier.testTag("surah_detail_play_audio_button")
                        ) {
                            Icon(
                                imageVector = if (isPlayingThisSurah) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlayingThisSurah) "إيقاف التلاوة" else "استماع للتلاوة",
                                tint = if (isPlayingThisSurah) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onToggleDarkMode,
                            modifier = Modifier.testTag("surah_detail_dark_mode_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkMode) "الوضع النهاري" else "الوضع الليلي للقراءة",
                                tint = if (isDarkMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onToggleFontSize,
                            modifier = Modifier.testTag("surah_detail_font_size_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "تعديل حجم الخط",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (playbackState?.currentTrack != null) {
                MiniAudioPlayer(
                    playbackState = playbackState,
                    onTogglePlayPause = onTogglePlayPause,
                    onExpand = onOpenFullPlayer,
                    onClose = onStopAudio
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("surah_ayahs_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Surah Header Banner with Islamic Frame
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                        .border(
                            1.5.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "﴿ سورة ${surah.name} ﴾",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        if (surah.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = surah.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Bismillah banner (omit for Surah 9 At-Tawbah)
                        if (surah.number != 9) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                fontSize = (fontSize * 1.05f).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                lineHeight = (fontSize * 1.7f).sp
                            )
                        }
                    }
                }
            }

            // Ayahs list
            items(ayahs, key = { it.numberInSurah }) { ayah ->
                val isBookmarked = bookmarks.any {
                    it.surahNumber == surah.number && it.ayahNumber == ayah.numberInSurah
                }

                AyahCard(
                    ayah = ayah,
                    surahName = surah.name,
                    fontSize = fontSize,
                    isBookmarked = isBookmarked,
                    onSaveReadingPosition = {
                        onSaveReadingPosition(ayah.numberInSurah)
                        Toast.makeText(
                            context,
                            "تم حفظ علامة القراءة عند سورة ${surah.name} الآية ${ayah.numberInSurah}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onToggleBookmark = { onToggleAyahBookmark(ayah.numberInSurah) },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Ayah", "${ayah.text} [سورة ${surah.name}: ${ayah.numberInSurah}]")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ الآية الكريمة", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun AyahCard(
    ayah: Ayah,
    surahName: String,
    fontSize: Float,
    isBookmarked: Boolean,
    onSaveReadingPosition: () -> Unit,
    onToggleBookmark: () -> Unit,
    onCopy: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ayah_card_${ayah.numberInSurah}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top action bar for the Ayah
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Verse index tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "الآية ${ayah.numberInSurah}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Copy Ayah
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ الآية",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Save as current reading mark
                    IconButton(
                        onClick = onSaveReadingPosition,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "وضع علامة قراءة هنا",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Bookmark toggle
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "إشارة مرجعية",
                            modifier = Modifier.size(20.dp),
                            tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ayah Text with adjustable font size
            Text(
                text = "${ayah.text} ۝${ayah.numberInSurah}",
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                lineHeight = (fontSize * 1.7f).sp,
                modifier = Modifier.fillMaxWidth()
            )

            // Interpretation / Translation
            if (ayah.translation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "التفسير: ${ayah.translation}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
