package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.audio.FullAudioPlayerDialog
import com.example.ui.audio.MiniAudioPlayer
import com.example.ui.bookmarks.BookmarksScreen
import com.example.ui.components.AppTopBar
import com.example.ui.components.FontSizeSliderDialog
import com.example.ui.downloads.DownloadsScreen
import com.example.ui.duas.DuaCategoryDetailScreen
import com.example.ui.duas.DuasScreen
import com.example.ui.prayer.PrayerTimesScreen
import com.example.ui.quran.QuranScreen
import com.example.ui.quran.SurahDetailScreen
import com.example.ui.tasbih.TasbihScreen
import com.example.ui.theme.NoorQuranTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.SearchFilter

class MainActivity : ComponentActivity() {

    private val viewModel: QuranViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            NoorQuranTheme(themeMode = themeMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainAppContent(viewModel = viewModel, themeMode = themeMode)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: QuranViewModel, themeMode: ThemeMode) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchFilter by viewModel.searchFilter.collectAsStateWithLifecycle()
    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val showFontSlider by viewModel.showFontSlider.collectAsStateWithLifecycle()
    val ayahSearchResults by viewModel.ayahSearchResults.collectAsStateWithLifecycle()

    val selectedSurah by viewModel.selectedSurah.collectAsStateWithLifecycle()
    val selectedAyahs by viewModel.selectedAyahs.collectAsStateWithLifecycle()
    val selectedDuaCategory by viewModel.selectedDuaCategory.collectAsStateWithLifecycle()

    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val lastRead by viewModel.lastReadingPosition.collectAsStateWithLifecycle()
    val tasbeehCounts by viewModel.tasbeehCounters.collectAsStateWithLifecycle()
    val offlinePackages by viewModel.offlinePackages.collectAsStateWithLifecycle()
    val downloadingPackages by viewModel.downloadingPackages.collectAsStateWithLifecycle()

    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()
    val nextPrayerInfo by viewModel.nextPrayerInfo.collectAsStateWithLifecycle()
    val adhkarReminders by viewModel.adhkarReminders.collectAsStateWithLifecycle()

    val isTasbihOpen by viewModel.isTasbihScreenOpen.collectAsStateWithLifecycle()
    val tasbihCount by viewModel.tasbihCount.collectAsStateWithLifecycle()
    val tasbihTotalCount by viewModel.tasbihTotalCount.collectAsStateWithLifecycle()
    val tasbihTarget by viewModel.tasbihTarget.collectAsStateWithLifecycle()
    val tasbihDhikr by viewModel.tasbihDhikr.collectAsStateWithLifecycle()
    val tasbihVibration by viewModel.tasbihVibration.collectAsStateWithLifecycle()

    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val selectedReciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val isFullPlayerOpen by viewModel.isFullPlayerOpen.collectAsStateWithLifecycle()
    val trackDownloadProgress by viewModel.trackDownloadProgress.collectAsStateWithLifecycle()

    val isDarkEffective = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Full Audio Player Dialog
    if (isFullPlayerOpen && playbackState.currentTrack != null) {
        FullAudioPlayerDialog(
            playbackState = playbackState,
            reciters = viewModel.availableReciters,
            selectedReciter = selectedReciter,
            downloadProgress = trackDownloadProgress,
            onTogglePlayPause = { viewModel.toggleAudioPlayPause() },
            onSeekTo = { viewModel.seekAudioTo(it) },
            onSeekRelative = { viewModel.seekAudioRelative(it) },
            onNextSurah = { viewModel.nextSurah() },
            onPreviousSurah = { viewModel.previousSurah() },
            onChangeReciter = { viewModel.changeReciter(it) },
            onDownloadTrack = { viewModel.downloadCurrentAudioTrack() },
            onToggleLooping = { viewModel.toggleAudioLooping() },
            onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
            onDismiss = { viewModel.closeFullPlayer() }
        )
    }

    // Font size & Theme dialog
    if (showFontSlider) {
        FontSizeSliderDialog(
            fontSize = fontSize,
            onFontSizeChange = { viewModel.setFontSize(it) },
            themeMode = themeMode,
            onThemeModeChange = { viewModel.setThemeMode(it) },
            onDismiss = { viewModel.toggleFontSlider(false) }
        )
    }

    // Detail screens check
    if (isTasbihOpen) {
        TasbihScreen(
            currentCount = tasbihCount,
            totalCount = tasbihTotalCount,
            target = tasbihTarget,
            currentDhikr = tasbihDhikr,
            vibrationEnabled = tasbihVibration,
            onIncrement = { viewModel.incrementTasbih() },
            onDecrement = { viewModel.decrementTasbih() },
            onResetCurrent = { viewModel.resetTasbih() },
            onResetTotal = { viewModel.resetTotalTasbih() },
            onSetTarget = { viewModel.setTasbihTarget(it) },
            onSetDhikr = { viewModel.setTasbihDhikr(it) },
            onToggleVibration = { viewModel.toggleTasbihVibration() },
            onBack = { viewModel.closeTasbih() }
        )
    } else if (selectedSurah != null) {
        SurahDetailScreen(
            surah = selectedSurah!!,
            ayahs = selectedAyahs,
            fontSize = fontSize,
            isDarkMode = isDarkEffective,
            bookmarks = bookmarks,
            onBack = { viewModel.closeSurah() },
            onSaveReadingPosition = { ayahNum ->
                viewModel.saveCurrentReadingPosition(selectedSurah!!.number, selectedSurah!!.name, ayahNum)
            },
            onToggleAyahBookmark = { ayahNum ->
                viewModel.toggleSurahBookmark(selectedSurah!!, ayahNum)
            },
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            onToggleFontSize = { viewModel.toggleFontSlider() },
            onPlayAudio = {
                if (playbackState.currentTrack?.surahNumber == selectedSurah!!.number) {
                    viewModel.toggleAudioPlayPause()
                } else {
                    viewModel.playSurah(selectedSurah!!.number, selectedSurah!!.name)
                }
            },
            isPlayingThisSurah = playbackState.currentTrack?.surahNumber == selectedSurah!!.number && playbackState.isPlaying,
            playbackState = playbackState,
            onOpenFullPlayer = { viewModel.openFullPlayer() },
            onTogglePlayPause = { viewModel.toggleAudioPlayPause() },
            onStopAudio = { viewModel.stopAudio() }
        )
    } else if (selectedDuaCategory != null) {
        val duasInCategory = viewModel.getFilteredDuas().filter { it.categoryId == selectedDuaCategory!!.id }
        DuaCategoryDetailScreen(
            category = selectedDuaCategory!!,
            duas = duasInCategory,
            fontSize = fontSize,
            isDarkMode = isDarkEffective,
            bookmarks = bookmarks,
            tasbeehCounts = tasbeehCounts,
            onBack = { viewModel.closeDuaCategory() },
            onToggleBookmark = { dua -> viewModel.toggleDuaBookmark(dua) },
            onIncrementTasbeeh = { duaId, target -> viewModel.incrementTasbeeh(duaId, target) },
            onResetTasbeeh = { duaId -> viewModel.resetTasbeeh(duaId) },
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            onToggleFontSize = { viewModel.toggleFontSlider() }
        )
    } else {
        Scaffold(
            topBar = {
                AppTopBar(
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.updateSearchQuery(it) },
                    searchFilter = searchFilter,
                    onFilterChange = { viewModel.setSearchFilter(it) },
                    themeMode = themeMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onToggleFontSize = { viewModel.toggleFontSlider() },
                    onOpenTasbih = { viewModel.openTasbih() },
                    onOpenDownloads = { viewModel.setTab(AppTab.DOWNLOADS) },
                    onOpenAudioPlayer = {
                        if (playbackState.currentTrack == null) {
                            viewModel.playSurah(1, "الفاتحة")
                        }
                        viewModel.openFullPlayer()
                    }
                )
            },
            bottomBar = {
                Column {
                    if (playbackState.currentTrack != null) {
                        MiniAudioPlayer(
                            playbackState = playbackState,
                            onTogglePlayPause = { viewModel.toggleAudioPlayPause() },
                            onExpand = { viewModel.openFullPlayer() },
                            onClose = { viewModel.stopAudio() }
                        )
                    }
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.QURAN,
                        onClick = { viewModel.setTab(AppTab.QURAN) },
                        icon = {
                            Icon(imageVector = Icons.Default.MenuBook, contentDescription = "المصحف")
                        },
                        label = { Text("المصحف") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_quran")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.DUAS,
                        onClick = { viewModel.setTab(AppTab.DUAS) },
                        icon = {
                            Icon(imageVector = Icons.Default.Spa, contentDescription = "الأدعية")
                        },
                        label = { Text("الأدعية") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_duas")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.PRAYER,
                        onClick = { viewModel.setTab(AppTab.PRAYER) },
                        icon = {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = "الصلاة والأذكار")
                        },
                        label = { Text("الصلاة والأذكار") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_prayer")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.BOOKMARKS,
                        onClick = { viewModel.setTab(AppTab.BOOKMARKS) },
                        icon = {
                            Icon(imageVector = Icons.Default.Bookmark, contentDescription = "المحفوظات")
                        },
                        label = { Text("المحفوظات") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_bookmarks")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.DOWNLOADS,
                        onClick = { viewModel.setTab(AppTab.DOWNLOADS) },
                        icon = {
                            Icon(imageVector = Icons.Default.CloudDone, contentDescription = "بدون نت")
                        },
                        label = { Text("بدون نت") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_downloads")
                    )
                }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Crossfade(targetState = currentTab, label = "tab_fade") { tab ->
                    when (tab) {
                        AppTab.QURAN -> {
                            val surahs = viewModel.getFilteredSurahs()
                            QuranScreen(
                                surahs = surahs,
                                lastRead = lastRead,
                                bookmarks = bookmarks,
                                onResumeLastRead = { pos -> viewModel.resumeLastRead(pos) },
                                onSurahClick = { surah -> viewModel.openSurah(surah) },
                                onToggleBookmark = { surah -> viewModel.toggleSurahBookmark(surah) },
                                onPlaySurahAudio = { surah ->
                                    viewModel.playSurah(surah.number, surah.name)
                                    viewModel.openFullPlayer()
                                },
                                currentPlayingSurahNumber = playbackState.currentTrack?.surahNumber,
                                isPlaying = playbackState.isPlaying,
                                matchedAyahs = ayahSearchResults,
                                onAyahClick = { ayah ->
                                    val surah = viewModel.surahsState.value.find { it.number == ayah.surahNumber }
                                        ?: com.example.data.QuranData.getSurahByNumber(ayah.surahNumber)
                                    if (surah != null) {
                                        viewModel.openSurah(surah)
                                    }
                                }
                            )
                        }

                        AppTab.DUAS -> {
                            val categories = viewModel.getFilteredDuaCategories()
                            val searchResults = if (searchQuery.isNotEmpty()) viewModel.getFilteredDuas() else emptyList()
                            DuasScreen(
                                categories = categories,
                                searchResults = searchResults,
                                isSearching = searchQuery.isNotEmpty(),
                                onCategoryClick = { category -> viewModel.openDuaCategory(category) },
                                onDuaClick = { dua ->
                                    val cat = categories.find { it.id == dua.categoryId }
                                        ?: com.example.data.DuaData.getCategoryById(dua.categoryId)
                                    if (cat != null) {
                                        viewModel.openDuaCategory(cat)
                                    }
                                },
                                onOpenTasbih = { viewModel.openTasbih() }
                            )
                        }

                        AppTab.PRAYER -> {
                            PrayerTimesScreen(
                                selectedCity = selectedCity,
                                prayerTimes = prayerTimes,
                                nextPrayerInfo = nextPrayerInfo,
                                adhkarReminders = adhkarReminders,
                                onSelectCity = { city -> viewModel.selectCity(city) },
                                onTogglePrayerAthan = { prayerId -> viewModel.togglePrayerAthan(prayerId) },
                                onToggleAdhkarReminder = { remId -> viewModel.toggleAdhkarReminder(remId) },
                                onUpdateReminderTime = { remId, time -> viewModel.updateAdhkarReminderTime(remId, time) },
                                onOpenAdhkarCategory = { categoryId ->
                                    val cat = com.example.data.DuaData.getCategoryById(categoryId)
                                    if (cat != null) {
                                        viewModel.openDuaCategory(cat)
                                    }
                                }
                            )
                        }

                        AppTab.BOOKMARKS -> {
                            BookmarksScreen(
                                bookmarks = bookmarks,
                                onOpenSurah = { surahNumber ->
                                    val surah = viewModel.getFilteredSurahs().find { it.number == surahNumber }
                                        ?: com.example.data.QuranData.getSurahByNumber(surahNumber)
                                    if (surah != null) {
                                        viewModel.openSurah(surah)
                                    }
                                },
                                onOpenDua = { duaId ->
                                    val dua = com.example.data.DuaData.duas.find { it.id == duaId }
                                    if (dua != null) {
                                        val cat = com.example.data.DuaData.getCategoryById(dua.categoryId)
                                        if (cat != null) {
                                            viewModel.openDuaCategory(cat)
                                        }
                                    }
                                },
                                onDeleteBookmark = { id -> viewModel.removeBookmark(id) }
                            )
                        }

                        AppTab.DOWNLOADS -> {
                            DownloadsScreen(
                                packages = offlinePackages,
                                downloadingMap = downloadingPackages,
                                onStartDownload = { pkg -> viewModel.startDownload(pkg) },
                                onDeletePackage = { id -> viewModel.deleteDownloadedPackage(id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
