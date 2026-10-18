package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BookmarkEntity
import com.example.data.repository.QuranRepository
import com.example.model.Ayah
import com.example.model.DuaCategory
import com.example.model.DuaItem
import com.example.model.OfflinePackage
import com.example.model.ReadingPosition
import com.example.model.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    QURAN,
    DUAS,
    PRAYER,
    BOOKMARKS,
    DOWNLOADS
}

enum class SearchFilter {
    ALL,
    QURAN,
    DUAS
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuranRepository(application)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.QURAN)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Search Query & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    // Verse Search Results from Room Database
    private val _ayahSearchResults = MutableStateFlow<List<Ayah>>(emptyList())
    val ayahSearchResults: StateFlow<List<Ayah>> = _ayahSearchResults.asStateFlow()

    // Reactive StateFlows from Room Database
    val surahsState: StateFlow<List<Surah>> = repository.surahsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.QuranData.surahs)

    val duaCategoriesState: StateFlow<List<DuaCategory>> = repository.duaCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.DuaData.categories)

    val allDuasState: StateFlow<List<DuaItem>> = repository.allDuasFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.DuaData.duas)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.trim().length >= 2) {
                _ayahSearchResults.value = repository.searchAyahsInDb(query)
            } else {
                _ayahSearchResults.value = emptyList()
            }
        }
    }

    fun setSearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    // Font Size Control (18sp - 38sp, default 24sp)
    private val _fontSize = MutableStateFlow(repository.getFontSize())
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _showFontSlider = MutableStateFlow(false)
    val showFontSlider: StateFlow<Boolean> = _showFontSlider.asStateFlow()

    fun toggleFontSlider(show: Boolean? = null) {
        _showFontSlider.value = show ?: !_showFontSlider.value
    }

    fun setFontSize(newSize: Float) {
        _fontSize.value = newSize
        repository.setFontSize(newSize)
    }

    // Theme Mode (System, Light, Dark)
    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<com.example.ui.theme.ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: com.example.ui.theme.ThemeMode) {
        _themeMode.value = mode
        repository.setThemeMode(mode)
    }

    fun toggleDarkMode() {
        val newMode = when (_themeMode.value) {
            com.example.ui.theme.ThemeMode.DARK -> com.example.ui.theme.ThemeMode.LIGHT
            com.example.ui.theme.ThemeMode.LIGHT -> com.example.ui.theme.ThemeMode.DARK
            com.example.ui.theme.ThemeMode.SYSTEM -> com.example.ui.theme.ThemeMode.DARK
        }
        setThemeMode(newMode)
    }

    // Detail screens navigation
    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah: StateFlow<Surah?> = _selectedSurah.asStateFlow()

    private val _selectedAyahs = MutableStateFlow<List<Ayah>>(emptyList())
    val selectedAyahs: StateFlow<List<Ayah>> = _selectedAyahs.asStateFlow()

    private val _selectedDuaCategory = MutableStateFlow<DuaCategory?>(null)
    val selectedDuaCategory: StateFlow<DuaCategory?> = _selectedDuaCategory.asStateFlow()

    fun openSurah(surah: Surah) {
        _selectedSurah.value = surah
        _selectedAyahs.value = repository.getAyahs(surah.number)
        viewModelScope.launch {
            val dbAyahs = repository.getAyahsFromDb(surah.number)
            if (dbAyahs.isNotEmpty()) {
                _selectedAyahs.value = dbAyahs
            }
        }
    }

    fun closeSurah() {
        _selectedSurah.value = null
        _selectedAyahs.value = emptyList()
    }

    fun openDuaCategory(category: DuaCategory) {
        _selectedDuaCategory.value = category
    }

    fun closeDuaCategory() {
        _selectedDuaCategory.value = null
    }

    // Bookmarks and Favorites
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastReadingPosition: StateFlow<ReadingPosition?> = repository.lastReadingPositionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleSurahBookmark(surah: Surah, ayahNumber: Int = 1) {
        viewModelScope.launch {
            repository.toggleSurahBookmark(surah, ayahNumber)
            vibrate(50)
        }
    }

    fun toggleDuaBookmark(dua: DuaItem) {
        viewModelScope.launch {
            repository.toggleDuaBookmark(dua)
            vibrate(50)
        }
    }

    fun removeBookmark(id: String) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    fun saveCurrentReadingPosition(surahNumber: Int, surahName: String, ayahNumber: Int) {
        viewModelScope.launch {
            repository.saveReadingPosition(surahNumber, surahName, ayahNumber)
        }
    }

    fun resumeLastRead(position: ReadingPosition) {
        val surah = repository.getSurah(position.surahNumber)
        if (surah != null) {
            _currentTab.value = AppTab.QURAN
            openSurah(surah)
        }
    }

    // Interactive Tasbeeh counter on Duas
    private val _tasbeehCounters = MutableStateFlow<Map<String, Int>>(emptyMap())
    val tasbeehCounters: StateFlow<Map<String, Int>> = _tasbeehCounters.asStateFlow()

    fun incrementTasbeeh(duaId: String, targetCount: Int) {
        val current = _tasbeehCounters.value[duaId] ?: 0
        val next = if (current >= targetCount) 0 else current + 1
        _tasbeehCounters.value = _tasbeehCounters.value + (duaId to next)
        vibrate(30)
    }

    fun resetTasbeeh(duaId: String) {
        _tasbeehCounters.value = _tasbeehCounters.value + (duaId to 0)
    }

    // Offline download management
    private val _downloadingPackages = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadingPackages: StateFlow<Map<String, Float>> = _downloadingPackages.asStateFlow()

    val offlinePackages: StateFlow<List<OfflinePackage>> = combine(
        repository.downloadedPackagesFlow,
        _downloadingPackages
    ) { downloadedList, downloadingMap ->
        val downloadedIds = downloadedList.map { it.packageId }.toSet()
        repository.defaultPackages.map { pkg ->
            val isDownloaded = downloadedIds.contains(pkg.id) || pkg.isDownloaded
            val currentProgress = downloadingMap[pkg.id] ?: if (isDownloaded) 1f else 0f
            pkg.copy(
                isDownloaded = isDownloaded,
                progress = currentProgress
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.defaultPackages
    )

    fun startDownload(pkg: OfflinePackage) {
        viewModelScope.launch {
            // Simulate smooth progress animation then persist
            for (step in 1..10) {
                kotlinx.coroutines.delay(120)
                _downloadingPackages.value = _downloadingPackages.value + (pkg.id to (step * 0.1f))
            }
            repository.downloadPackage(pkg)
            _downloadingPackages.value = _downloadingPackages.value - pkg.id
            vibrate(100)
        }
    }

    fun deleteDownloadedPackage(pkgId: String) {
        viewModelScope.launch {
            repository.removePackage(pkgId)
        }
    }

    // Data lists filtered by search query from Room Database
    fun getFilteredSurahs(): List<Surah> {
        val query = _searchQuery.value.trim().lowercase()
        val base = surahsState.value.ifEmpty { repository.getAllSurahs() }
        if (query.isEmpty()) return base
        return base.filter {
            it.name.contains(query) ||
            it.englishName.lowercase().contains(query) ||
            it.number.toString() == query ||
            it.description.contains(query) ||
            it.revelationType.contains(query)
        }
    }

    fun getFilteredDuaCategories(): List<DuaCategory> {
        val query = _searchQuery.value.trim().lowercase()
        val base = duaCategoriesState.value.ifEmpty { repository.getAllDuaCategories() }
        if (query.isEmpty()) return base
        return base.filter {
            it.name.contains(query) ||
            it.englishName.lowercase().contains(query) ||
            it.description.contains(query)
        }
    }

    fun getFilteredDuas(): List<DuaItem> {
        val query = _searchQuery.value.trim().lowercase()
        val base = allDuasState.value.ifEmpty { com.example.data.DuaData.duas }
        if (query.isEmpty()) return base
        return base.filter {
            it.title.contains(query) ||
            it.arabicText.contains(query) ||
            it.categoryName.contains(query) ||
            it.virtue.contains(query) ||
            it.reference.contains(query)
        }
    }

    // Prayer Times & Adhkar Reminders
    private val _selectedCity = MutableStateFlow(repository.getSelectedCity())
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _prayerTimes = MutableStateFlow<List<com.example.model.PrayerTime>>(emptyList())
    val prayerTimes: StateFlow<List<com.example.model.PrayerTime>> = _prayerTimes.asStateFlow()

    private val _nextPrayerInfo = MutableStateFlow<Pair<com.example.model.PrayerTime, String>?>(null)
    val nextPrayerInfo: StateFlow<Pair<com.example.model.PrayerTime, String>?> = _nextPrayerInfo.asStateFlow()

    private val _adhkarReminders = MutableStateFlow<List<com.example.model.AdhkarReminder>>(emptyList())
    val adhkarReminders: StateFlow<List<com.example.model.AdhkarReminder>> = _adhkarReminders.asStateFlow()

    init {
        loadPrayerTimes()
        loadAdhkarReminders()
    }

    fun loadPrayerTimes() {
        val cityPreset = com.example.data.PrayerData.getCityByName(_selectedCity.value)
        val times = com.example.data.PrayerData.getPrayerTimesForCity(cityPreset)
        // apply athan enabled status
        val updatedTimes = times.map { prayer ->
            prayer.copy(isAthanEnabled = repository.isPrayerAthanEnabled(prayer.id))
        }
        _prayerTimes.value = updatedTimes
        _nextPrayerInfo.value = com.example.data.PrayerData.getNextPrayerInfo(updatedTimes)
    }

    fun selectCity(cityName: String) {
        _selectedCity.value = cityName
        repository.setSelectedCity(cityName)
        loadPrayerTimes()
    }

    fun togglePrayerAthan(prayerId: String) {
        val current = repository.isPrayerAthanEnabled(prayerId)
        val newStatus = !current
        repository.setPrayerAthanEnabled(prayerId, newStatus)
        _prayerTimes.value = _prayerTimes.value.map {
            if (it.id == prayerId) it.copy(isAthanEnabled = newStatus) else it
        }
        vibrate(30)
    }

    fun loadAdhkarReminders() {
        _adhkarReminders.value = com.example.data.PrayerData.defaultAdhkarReminders.map { rem ->
            rem.copy(
                isEnabled = repository.isAdhkarReminderEnabled(rem.id),
                timeFormatted = repository.getAdhkarReminderTime(rem.id, rem.timeFormatted)
            )
        }
    }

    fun toggleAdhkarReminder(reminderId: String) {
        val current = repository.isAdhkarReminderEnabled(reminderId)
        val newStatus = !current
        repository.setAdhkarReminderEnabled(reminderId, newStatus)
        _adhkarReminders.value = _adhkarReminders.value.map {
            if (it.id == reminderId) it.copy(isEnabled = newStatus) else it
        }
        vibrate(40)
    }

    fun updateAdhkarReminderTime(reminderId: String, newTime: String) {
        repository.setAdhkarReminderTime(reminderId, newTime)
        _adhkarReminders.value = _adhkarReminders.value.map {
            if (it.id == reminderId) it.copy(timeFormatted = newTime) else it
        }
    }

    // Dedicated Digital Tasbih State
    private val _isTasbihScreenOpen = MutableStateFlow(false)
    val isTasbihScreenOpen: StateFlow<Boolean> = _isTasbihScreenOpen.asStateFlow()

    private val _tasbihCount = MutableStateFlow(repository.getTasbihCount())
    val tasbihCount: StateFlow<Int> = _tasbihCount.asStateFlow()

    private val _tasbihTotalCount = MutableStateFlow(repository.getTasbihTotalCount())
    val tasbihTotalCount: StateFlow<Int> = _tasbihTotalCount.asStateFlow()

    private val _tasbihTarget = MutableStateFlow(repository.getTasbihTarget())
    val tasbihTarget: StateFlow<Int> = _tasbihTarget.asStateFlow()

    private val _tasbihDhikr = MutableStateFlow(repository.getTasbihDhikr())
    val tasbihDhikr: StateFlow<String> = _tasbihDhikr.asStateFlow()

    private val _tasbihVibration = MutableStateFlow(repository.isTasbihVibrationEnabled())
    val tasbihVibration: StateFlow<Boolean> = _tasbihVibration.asStateFlow()

    fun openTasbih() {
        _isTasbihScreenOpen.value = true
    }

    fun closeTasbih() {
        _isTasbihScreenOpen.value = false
    }

    fun incrementTasbih() {
        val newCount = _tasbihCount.value + 1
        val newTotal = _tasbihTotalCount.value + 1
        _tasbihCount.value = newCount
        _tasbihTotalCount.value = newTotal
        repository.setTasbihCount(newCount)
        repository.setTasbihTotalCount(newTotal)

        if (_tasbihVibration.value) {
            val target = _tasbihTarget.value
            if (target > 0 && newCount % target == 0) {
                // Celebrate target completion with a longer vibration
                vibrate(100)
            } else {
                vibrate(30)
            }
        }
    }

    fun decrementTasbih() {
        if (_tasbihCount.value > 0) {
            val newCount = _tasbihCount.value - 1
            val newTotal = (_tasbihTotalCount.value - 1).coerceAtLeast(0)
            _tasbihCount.value = newCount
            _tasbihTotalCount.value = newTotal
            repository.setTasbihCount(newCount)
            repository.setTasbihTotalCount(newTotal)
            if (_tasbihVibration.value) vibrate(20)
        }
    }

    fun resetTasbih() {
        _tasbihCount.value = 0
        repository.setTasbihCount(0)
        if (_tasbihVibration.value) vibrate(50)
    }

    fun resetTotalTasbih() {
        _tasbihCount.value = 0
        _tasbihTotalCount.value = 0
        repository.setTasbihCount(0)
        repository.setTasbihTotalCount(0)
        if (_tasbihVibration.value) vibrate(70)
    }

    fun setTasbihTarget(target: Int) {
        _tasbihTarget.value = target
        repository.setTasbihTarget(target)
    }

    fun setTasbihDhikr(dhikr: String) {
        _tasbihDhikr.value = dhikr
        repository.setTasbihDhikr(dhikr)
    }

    fun toggleTasbihVibration() {
        val newStatus = !_tasbihVibration.value
        _tasbihVibration.value = newStatus
        repository.setTasbihVibrationEnabled(newStatus)
    }

    private fun vibrate(durationMillis: Long) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMillis)
                }
            }
        } catch (_: Exception) {
            // Graceful ignore
        }
    }

    // Audio Player State & Controls
    private val audioPlayerManager = com.example.data.audio.AudioPlayerManager(getApplication())
    val playbackState: StateFlow<com.example.model.PlayerPlaybackState> = audioPlayerManager.playbackState

    val availableReciters: List<com.example.model.Reciter> = com.example.data.AudioData.reciters
    private val _selectedReciter = MutableStateFlow(com.example.data.AudioData.reciters[0])
    val selectedReciter: StateFlow<com.example.model.Reciter> = _selectedReciter.asStateFlow()

    private val _isFullPlayerOpen = MutableStateFlow(false)
    val isFullPlayerOpen: StateFlow<Boolean> = _isFullPlayerOpen.asStateFlow()

    private val _trackDownloadProgress = MutableStateFlow<Float?>(null)
    val trackDownloadProgress: StateFlow<Float?> = _trackDownloadProgress.asStateFlow()

    fun playSurah(surahNumber: Int, surahName: String? = null, reciter: com.example.model.Reciter? = null) {
        val sName = surahName ?: repository.getSurah(surahNumber)?.name ?: "سورة $surahNumber"
        val r = reciter ?: _selectedReciter.value
        _selectedReciter.value = r
        audioPlayerManager.playTrack(surahNumber, sName, r)
    }

    fun toggleAudioPlayPause() {
        audioPlayerManager.togglePlayPause()
    }

    fun seekAudioTo(positionMs: Long) {
        audioPlayerManager.seekTo(positionMs)
    }

    fun seekAudioRelative(offsetMs: Long) {
        audioPlayerManager.seekRelative(offsetMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        audioPlayerManager.setPlaybackSpeed(speed)
    }

    fun toggleAudioLooping() {
        audioPlayerManager.toggleLooping()
    }

    fun changeReciter(reciter: com.example.model.Reciter) {
        _selectedReciter.value = reciter
        val current = playbackState.value.currentTrack
        if (current != null) {
            playSurah(current.surahNumber, current.surahName, reciter)
        }
    }

    fun nextSurah() {
        val current = playbackState.value.currentTrack ?: return
        val nextNumber = if (current.surahNumber >= 114) 1 else current.surahNumber + 1
        val nextSurah = repository.getSurah(nextNumber) ?: return
        playSurah(nextNumber, nextSurah.name, _selectedReciter.value)
    }

    fun previousSurah() {
        val current = playbackState.value.currentTrack ?: return
        val prevNumber = if (current.surahNumber <= 1) 114 else current.surahNumber - 1
        val prevSurah = repository.getSurah(prevNumber) ?: return
        playSurah(prevNumber, prevSurah.name, _selectedReciter.value)
    }

    fun downloadCurrentAudioTrack() {
        val track = playbackState.value.currentTrack ?: return
        viewModelScope.launch {
            _trackDownloadProgress.value = 0f
            val success = audioPlayerManager.downloadTrack(track) { progress ->
                _trackDownloadProgress.value = progress
            }
            _trackDownloadProgress.value = null
            if (success) {
                vibrate(100)
            }
        }
    }

    fun openFullPlayer() {
        _isFullPlayerOpen.value = true
    }

    fun closeFullPlayer() {
        _isFullPlayerOpen.value = false
    }

    fun stopAudio() {
        audioPlayerManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.stop()
    }
}
