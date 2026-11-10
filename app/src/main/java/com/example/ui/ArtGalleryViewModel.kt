package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AudioSoundscapeManager
import com.example.data.MetMuseumRepository
import com.example.data.model.Artwork
import com.example.data.model.CategoryType
import com.example.data.model.MuseumCategory
import com.example.data.model.SlideshowConfig
import com.example.data.model.SoundscapeType
import com.example.util.DonationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtGalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MetMuseumRepository(application)
    private val soundscapeManager = AudioSoundscapeManager()
    val donationManager = DonationManager(application) { tier ->
        becomePatron(tier)
    }

    private val _categories = MutableStateFlow<List<MuseumCategory>>(repository.baseCategories)
    val categoriesFlow: StateFlow<List<MuseumCategory>> = _categories.asStateFlow()
    val museumCategories: List<MuseumCategory>
        get() = _categories.value

    private val _activeCategory = MutableStateFlow(repository.baseCategories.first())
    val activeCategory: StateFlow<MuseumCategory> = _activeCategory.asStateFlow()

    private val _config = MutableStateFlow(SlideshowConfig(activeCategory = _activeCategory.value))
    val config: StateFlow<SlideshowConfig> = _config.asStateFlow()

    private val _artworks = MutableStateFlow<List<Artwork>>(emptyList())
    val artworks: StateFlow<List<Artwork>> = _artworks.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _timerProgress = MutableStateFlow(0f)
    val timerProgress: StateFlow<Float> = _timerProgress.asStateFlow()

    private val _isPlacardVisible = MutableStateFlow(true)
    val isPlacardVisible: StateFlow<Boolean> = _isPlacardVisible.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isBrowserOpen = MutableStateFlow(false)
    val isBrowserOpen: StateFlow<Boolean> = _isBrowserOpen.asStateFlow()

    private val _isDetailModalOpen = MutableStateFlow(false)
    val isDetailModalOpen: StateFlow<Boolean> = _isDetailModalOpen.asStateFlow()

    private val _isCategorySelectionOpen = MutableStateFlow(false)
    val isCategorySelectionOpen: StateFlow<Boolean> = _isCategorySelectionOpen.asStateFlow()

    private val _isPatronDialogOpen = MutableStateFlow(false)
    val isPatronDialogOpen: StateFlow<Boolean> = _isPatronDialogOpen.asStateFlow()

    val isPatron: StateFlow<Boolean> = repository.isPatron
    val patronTier: StateFlow<String> = repository.patronTier
    val slideCount: StateFlow<Int> = repository.slideCount
    val hasDismissedMilestone: StateFlow<Boolean> = repository.hasDismissedMilestone

    private val _isLoadingCategory = MutableStateFlow(true)
    val isLoadingCategory: StateFlow<Boolean> = _isLoadingCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val isAnyModalOpen: Boolean
        get() = _isCategorySelectionOpen.value || _isSettingsOpen.value || _isBrowserOpen.value || _isDetailModalOpen.value || _isPatronDialogOpen.value

    val departments: List<String>
        get() = _categories.value.map { it.title }

    private var timerJob: Job? = null
    private var placardTimeoutJob: Job? = null

    init {
        // Fetch live Met Museum departments
        viewModelScope.launch {
            val allCats = repository.fetchDepartmentsFromMetApi()
            if (allCats.isNotEmpty()) {
                _categories.value = allCats
            }
        }

        // Observe Room favorite IDs for reactive updates across the entire app
        viewModelScope.launch {
            repository.favoriteIds.collectLatest { favSet ->
                _favoriteIds.value = favSet
                _artworks.value = _artworks.value.map { art ->
                    art.copy(isFavorite = favSet.contains(art.id))
                }
            }
        }

        // Observe active category: display cached API records if available, then fetch live from Met API
        viewModelScope.launch {
            _activeCategory.collectLatest { cat ->
                loadCategoryArtworks(cat)
            }
        }

        startSlideshowTimer()
        resetPlacardTimer()
        donationManager.startConnection()
    }

    private suspend fun loadCategoryArtworks(cat: MuseumCategory) {
        _isLoadingCategory.value = true
        val cachedList = repository.getCachedArtworksForCategory(cat)
        if (cachedList.isNotEmpty()) {
            _artworks.value = cachedList
            if (_currentIndex.value >= _artworks.value.size) {
                _currentIndex.value = 0
            }
            updateCategoryThumbnails(cat, cachedList)
        } else {
            _artworks.value = emptyList()
            _currentIndex.value = 0
        }
        resetSlideshowTimer()

        if (cat.categoryType != CategoryType.FAVORITES) {
            val liveList = repository.fetchArtworksFromMetApi(cat)
            if (liveList.isNotEmpty()) {
                _artworks.value = liveList
                if (_currentIndex.value >= _artworks.value.size) {
                    _currentIndex.value = 0
                }
                updateCategoryThumbnails(cat, liveList)
            }
        }
        _isLoadingCategory.value = false
    }

    private fun updateCategoryThumbnails(activeCat: MuseumCategory, loadedArtworks: List<Artwork>) {
        if (loadedArtworks.isEmpty()) return
        val firstThumb = loadedArtworks.first().thumbnailUrl
        _categories.value = _categories.value.map { cat ->
            when {
                cat.id == activeCat.id && cat.thumbnailUrl.isEmpty() ->
                    cat.copy(thumbnailUrl = firstThumb)
                cat.thumbnailUrl.isEmpty() -> {
                    val match = loadedArtworks.firstOrNull { art ->
                        (cat.categoryType == CategoryType.DEPARTMENT && art.department.equals(cat.title, ignoreCase = true)) ||
                                (cat.categoryType == CategoryType.MOVEMENT && (art.period.contains(cat.query, ignoreCase = true) || art.medium.contains(cat.query, ignoreCase = true)))
                    }
                    if (match != null) cat.copy(thumbnailUrl = match.thumbnailUrl) else cat
                }
                else -> cat
            }
        }
    }

    fun retryFetchCurrentCategory() {
        viewModelScope.launch {
            loadCategoryArtworks(_activeCategory.value)
        }
    }

    private fun startSlideshowTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var elapsedMs = 0L
            val stepMs = 100L
            while (true) {
                delay(stepMs)
                if (_isPlaying.value && !isAnyModalOpen && _artworks.value.isNotEmpty()) {
                    val totalMs = _config.value.intervalSeconds.coerceAtLeast(3) * 1000L
                    elapsedMs += stepMs
                    _timerProgress.value = (elapsedMs.toFloat() / totalMs).coerceIn(0f, 1f)
                    if (elapsedMs >= totalMs) {
                        elapsedMs = 0L
                        _timerProgress.value = 0f
                        nextArtwork(fromTimer = true)
                    }
                } else if (!_isPlaying.value) {
                    // keep current progress frozen when paused
                } else {
                    _timerProgress.value = 0f
                    elapsedMs = 0L
                }
            }
        }
    }

    private fun resetSlideshowTimer() {
        _timerProgress.value = 0f
        startSlideshowTimer()
    }

    fun onUserActivity() {
        if (_config.value.autoHidePlacard) {
            _isPlacardVisible.value = true
            resetPlacardTimer()
        }
    }

    private fun resetPlacardTimer() {
        placardTimeoutJob?.cancel()
        if (_config.value.autoHidePlacard) {
            placardTimeoutJob = viewModelScope.launch {
                delay(_config.value.placardTimeoutSeconds * 1000L)
                if (!isAnyModalOpen) {
                    _isPlacardVisible.value = false
                }
            }
        } else {
            _isPlacardVisible.value = true
        }
    }

    fun selectCategory(category: MuseumCategory) {
        _activeCategory.value = category
        _config.value = _config.value.copy(
            departmentFilter = category.title,
            activeCategory = category,
            favoritesOnly = (category.id == "favorites")
        )
        _currentIndex.value = 0
        onUserActivity()
    }

    fun nextArtwork(fromTimer: Boolean = false) {
        if (_artworks.value.isNotEmpty()) {
            _currentIndex.value = (_currentIndex.value + 1) % _artworks.value.size
            repository.incrementSlideCount()
            if (!fromTimer) {
                resetSlideshowTimer()
                onUserActivity()
            }
        }
    }

    fun prevArtwork() {
        if (_artworks.value.isNotEmpty()) {
            _currentIndex.value = if (_currentIndex.value - 1 < 0) _artworks.value.size - 1 else _currentIndex.value - 1
            resetSlideshowTimer()
            onUserActivity()
        }
    }

    fun shuffleArtwork() {
        val size = _artworks.value.size
        if (size > 1) {
            var next = (0 until size).random()
            if (next == _currentIndex.value) {
                next = (next + 1) % size
            }
            _currentIndex.value = next
            repository.incrementSlideCount()
            resetSlideshowTimer()
            onUserActivity()
        }
    }

    fun selectArtworkIndex(index: Int) {
        if (index in _artworks.value.indices) {
            _currentIndex.value = index
            resetSlideshowTimer()
            onUserActivity()
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
        onUserActivity()
    }

    fun togglePlacard() {
        _isPlacardVisible.value = !_isPlacardVisible.value
        if (_isPlacardVisible.value) {
            resetPlacardTimer()
        } else {
            placardTimeoutJob?.cancel()
        }
    }

    fun toggleFavorite(artworkId: Int) {
        viewModelScope.launch {
            val targetArtwork = _artworks.value.find { it.id == artworkId }
                ?: repository.fetchMetMuseumObjectDetails(artworkId)
                ?: return@launch
            repository.toggleFavorite(targetArtwork)
            if (_activeCategory.value.id == "favorites") {
                val list = repository.getCachedArtworksForCategory(_activeCategory.value)
                _artworks.value = list
                if (_currentIndex.value >= _artworks.value.size) {
                    _currentIndex.value = 0
                }
            }
        }
        onUserActivity()
    }

    fun isFavorite(artworkId: Int): Boolean {
        return _favoriteIds.value.contains(artworkId)
    }

    fun updateConfig(newConfig: SlideshowConfig) {
        val oldSound = _config.value.soundscape
        _config.value = newConfig
        if (oldSound != newConfig.soundscape) {
            soundscapeManager.playSoundscape(newConfig.soundscape)
        }
        resetSlideshowTimer()
        resetPlacardTimer()
        onUserActivity()
    }

    fun setSoundscape(soundscape: SoundscapeType) {
        updateConfig(_config.value.copy(soundscape = soundscape))
    }

    fun toggleDeskStandMode() {
        val newMode = !_config.value.deskStandMode
        updateConfig(_config.value.copy(deskStandMode = newMode))
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openCategorySelection() {
        _isCategorySelectionOpen.value = true
        _isPlacardVisible.value = true
    }

    fun closeCategorySelection() {
        _isCategorySelectionOpen.value = false
        resetPlacardTimer()
    }

    fun openSettings() {
        _isSettingsOpen.value = true
        _isPlacardVisible.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
        resetPlacardTimer()
    }

    fun openBrowser() {
        _isBrowserOpen.value = true
        _isPlacardVisible.value = true
    }

    fun closeBrowser() {
        _isBrowserOpen.value = false
        resetPlacardTimer()
    }

    fun openDetailModal() {
        _isDetailModalOpen.value = true
        _isPlacardVisible.value = true
    }

    fun closeDetailModal() {
        _isDetailModalOpen.value = false
        resetPlacardTimer()
    }

    fun openPatronDialog() {
        _isPatronDialogOpen.value = true
        _isPlacardVisible.value = true
    }

    fun closePatronDialog() {
        _isPatronDialogOpen.value = false
        resetPlacardTimer()
    }

    fun becomePatron(tier: String) {
        repository.setPatronStatus(true, tier)
    }

    fun dismissMilestone() {
        repository.dismissMilestone()
    }

    fun isMilestoneCelebrationEligible(): Boolean {
        return !isPatron.value && !hasDismissedMilestone.value && (slideCount.value >= 25 || favoriteIds.value.size >= 5)
    }

    override fun onCleared() {
        super.onCleared()
        soundscapeManager.stop()
        timerJob?.cancel()
        placardTimeoutJob?.cancel()
        donationManager.destroy()
    }
}
