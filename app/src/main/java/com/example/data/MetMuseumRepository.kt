package com.example.data

import android.content.Context
import com.example.data.db.ArtGalleryDatabase
import com.example.data.db.CachedArtworkEntity
import com.example.data.db.FavoriteArtworkEntity
import com.example.data.model.Artwork
import com.example.data.model.CategoryType
import com.example.data.model.MuseumCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MetMuseumRepository(private val context: Context) {

    companion object {
        private const val MET_API_BASE = "https://collectionapi.metmuseum.org/public/collection/v1"
        private const val USER_AGENT = "ArtGalleryTV/1.0 (Android; Open Access Client)"
    }

    private val db = ArtGalleryDatabase.getInstance(context)
    private val favoriteDao = db.favoriteArtworkDao()
    private val prefs = context.getSharedPreferences("art_gallery_patron_prefs", Context.MODE_PRIVATE)

    private val _isPatron = MutableStateFlow(prefs.getBoolean("is_patron", false))
    val isPatron: StateFlow<Boolean> = _isPatron.asStateFlow()

    private val _patronTier = MutableStateFlow(prefs.getString("patron_tier", "") ?: "")
    val patronTier: StateFlow<String> = _patronTier.asStateFlow()

    private val _slideCount = MutableStateFlow(prefs.getInt("slide_count", 0))
    val slideCount: StateFlow<Int> = _slideCount.asStateFlow()

    private val _hasDismissedMilestone = MutableStateFlow(prefs.getBoolean("dismissed_milestone", false))
    val hasDismissedMilestone: StateFlow<Boolean> = _hasDismissedMilestone.asStateFlow()

    fun setPatronStatus(isPatron: Boolean, tier: String) {
        _isPatron.value = isPatron
        _patronTier.value = tier
        prefs.edit()
            .putBoolean("is_patron", isPatron)
            .putString("patron_tier", tier)
            .putBoolean("dismissed_milestone", true)
            .apply()
    }

    fun incrementSlideCount() {
        val count = _slideCount.value + 1
        _slideCount.value = count
        prefs.edit().putInt("slide_count", count).apply()
    }

    fun dismissMilestone() {
        _hasDismissedMilestone.value = true
        prefs.edit().putBoolean("dismissed_milestone", true).apply()
    }

    val favoriteIds: Flow<Set<Int>> = favoriteDao.getFavoriteIdsFlow().map { it.toSet() }
    val favoriteArtworksFlow: Flow<List<Artwork>> = favoriteDao.getAllFavorites().map { list ->
        list.map { it.toArtwork() }
    }

    suspend fun toggleFavorite(artwork: Artwork) = withContext(Dispatchers.IO) {
        val isFav = favoriteDao.isFavorite(artwork.id)
        if (isFav) {
            favoriteDao.deleteFavoriteById(artwork.id)
        } else {
            favoriteDao.insertFavorite(FavoriteArtworkEntity.fromArtwork(artwork))
        }
    }

    suspend fun isFavorite(artworkId: Int): Boolean = withContext(Dispatchers.IO) {
        favoriteDao.isFavorite(artworkId)
    }

    // Base navigation & movement search filters (thumbnails & departments populated live from the Met API)
    val baseCategories: List<MuseumCategory> = listOf(
        MuseumCategory(
            id = "all",
            title = "All Masterpieces",
            categoryType = CategoryType.ALL,
            query = "painting",
            thumbnailUrl = "",
            description = "Live highlights across all departments from The Met Open Access API"
        ),
        MuseumCategory(
            id = "favorites",
            title = "Private Gallery",
            categoryType = CategoryType.FAVORITES,
            thumbnailUrl = "",
            description = "Your personal saved collection of favorite artworks stored locally"
        ),
        MuseumCategory(
            id = "mv_impressionism",
            title = "Impressionism",
            categoryType = CategoryType.MOVEMENT,
            query = "Impressionism",
            thumbnailUrl = "",
            description = "Live search for Impressionist works in The Met Open Access collection"
        ),
        MuseumCategory(
            id = "mv_post_impressionism",
            title = "Post-Impressionism",
            categoryType = CategoryType.MOVEMENT,
            query = "Post-Impressionism",
            thumbnailUrl = "",
            description = "Live search for Post-Impressionist works in The Met Open Access collection"
        ),
        MuseumCategory(
            id = "mv_renaissance",
            title = "Renaissance",
            categoryType = CategoryType.MOVEMENT,
            query = "Renaissance",
            thumbnailUrl = "",
            description = "Live search for Renaissance works in The Met Open Access collection"
        ),
        MuseumCategory(
            id = "mv_dutch_golden_age",
            title = "Dutch Golden Age",
            categoryType = CategoryType.MOVEMENT,
            query = "Dutch Golden Age",
            thumbnailUrl = "",
            description = "Live search for Dutch Golden Age works in The Met Open Access collection"
        ),
        MuseumCategory(
            id = "mv_baroque",
            title = "Baroque",
            categoryType = CategoryType.MOVEMENT,
            query = "Baroque",
            thumbnailUrl = "",
            description = "Live search for Baroque works in The Met Open Access collection"
        ),
        MuseumCategory(
            id = "mv_ukiyo_e",
            title = "Japanese Ukiyo-e",
            categoryType = CategoryType.MOVEMENT,
            query = "woodblock print",
            thumbnailUrl = "",
            description = "Live search for woodblock prints in The Met Open Access collection"
        )
    )

    suspend fun fetchDepartmentsFromMetApi(): List<MuseumCategory> = withContext(Dispatchers.IO) {
        val dynamicDepartments = mutableListOf<MuseumCategory>()
        try {
            val conn = URL("$MET_API_BASE/departments").openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(json)
                val departmentsArray = root.optJSONArray("departments")
                if (departmentsArray != null) {
                    for (i in 0 until departmentsArray.length()) {
                        val deptObj = departmentsArray.optJSONObject(i) ?: continue
                        val deptId = deptObj.optInt("departmentId", -1)
                        val displayName = deptObj.optString("displayName", "")
                        if (deptId > 0 && displayName.isNotBlank()) {
                            dynamicDepartments.add(
                                MuseumCategory(
                                    id = "dept_$deptId",
                                    title = displayName,
                                    categoryType = CategoryType.DEPARTMENT,
                                    departmentId = deptId,
                                    query = displayName,
                                    thumbnailUrl = "",
                                    description = "Official Metropolitan Museum of Art department (#$deptId)"
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Return base categories if offline
        }
        baseCategories + dynamicDepartments
    }

    suspend fun getCachedArtworksForCategory(category: MuseumCategory): List<Artwork> = withContext(Dispatchers.IO) {
        val favSet = favoriteIds.first()
        if (category.categoryType == CategoryType.FAVORITES) {
            return@withContext favoriteArtworksFlow.first()
        }
        val cachedByCat = favoriteDao.getCachedArtworksByCategory(category.id)
        val entities = if (cachedByCat.isNotEmpty()) {
            cachedByCat
        } else if (category.categoryType == CategoryType.ALL) {
            favoriteDao.getAllCachedArtworks()
        } else {
            emptyList()
        }
        entities.map { it.toArtwork(isFavorite = favSet.contains(it.id)) }
    }

    suspend fun fetchArtworksFromMetApi(category: MuseumCategory): List<Artwork> = withContext(Dispatchers.IO) {
        val favSet = favoriteIds.first()
        if (category.categoryType == CategoryType.FAVORITES) {
            return@withContext favoriteArtworksFlow.first()
        }

        val fetchedList = mutableListOf<Artwork>()
        try {
            val rawQuery = when {
                category.query.isNotBlank() -> category.query
                category.categoryType == CategoryType.DEPARTMENT -> category.title
                else -> "painting"
            }
            val encodedQuery = URLEncoder.encode(rawQuery, "UTF-8")
            val primaryEndpoint = if (category.departmentId != null) {
                "$MET_API_BASE/search?departmentId=${category.departmentId}&hasImages=true&isHighlight=true&q=$encodedQuery"
            } else {
                "$MET_API_BASE/search?hasImages=true&isHighlight=true&q=$encodedQuery"
            }

            var objectIds = fetchSearchObjectIds(primaryEndpoint)
            if (objectIds.isEmpty() && category.departmentId != null) {
                // Broaden search within the department if highlight query returned few results
                val fallbackEndpoint = "$MET_API_BASE/search?departmentId=${category.departmentId}&hasImages=true&q=*"
                objectIds = fetchSearchObjectIds(fallbackEndpoint)
            }

            if (objectIds.isNotEmpty()) {
                val selectedIds = objectIds.take(14)
                val details = coroutineScope {
                    selectedIds.map { id ->
                        async { fetchMetMuseumObjectDetails(id, favSet) }
                    }.awaitAll().filterNotNull()
                }
                fetchedList.addAll(details)
            }
        } catch (_: Exception) {
            // Fall back to previously cached real API records if network is unavailable
        }

        if (fetchedList.isNotEmpty()) {
            val entities = fetchedList.map { CachedArtworkEntity.fromArtwork(it, category.id) }
            favoriteDao.insertCachedArtworks(entities)
            return@withContext fetchedList
        }

        return@withContext getCachedArtworksForCategory(category)
    }

    private fun fetchSearchObjectIds(endpointUrl: String): List<Int> {
        return try {
            val conn = URL(endpointUrl).openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(json)
                val arr = obj.optJSONArray("objectIDs")
                if (arr != null && arr.length() > 0) {
                    (0 until minOf(24, arr.length())).map { arr.getInt(it) }
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun fetchMetMuseumObjectDetails(
        objectId: Int,
        favSetOverride: Set<Int>? = null
    ): Artwork? = withContext(Dispatchers.IO) {
        val favSet = favSetOverride ?: favoriteIds.first()
        try {
            val url = URL("$MET_API_BASE/objects/$objectId")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(json)
                val primaryImageSmall = obj.optString("primaryImageSmall", "")
                val primaryImage = obj.optString("primaryImage", "")
                val displayUrl = primaryImageSmall.ifEmpty { primaryImage }
                if (displayUrl.isNotEmpty()) {
                    val title = obj.optString("title", "").ifBlank { "Untitled" }
                    val artist = obj.optString("artistDisplayName", "")
                        .ifBlank { obj.optString("culture", "") }
                        .ifBlank { obj.optString("department", "") }
                    val classification = obj.optString("classification", "")
                    val objectName = obj.optString("objectName", "")
                    val dynasty = obj.optString("dynasty", "")
                    val reign = obj.optString("reign", "")
                    val repositoryName = obj.optString("repository", "")
                    val galleryNumber = obj.optString("GalleryNumber", "")

                    val dynamicSummary = buildList {
                        if (objectName.isNotBlank()) add(objectName)
                        if (classification.isNotBlank() && !classification.equals(objectName, ignoreCase = true)) {
                            add("Classification: $classification")
                        }
                        if (dynasty.isNotBlank()) add("Dynasty: $dynasty")
                        if (reign.isNotBlank()) add("Reign: $reign")
                        if (galleryNumber.isNotBlank()) add("On view at The Met in Gallery $galleryNumber")
                        if (repositoryName.isNotBlank()) add(repositoryName)
                    }.joinToString(" • ")

                    return@withContext Artwork(
                        id = obj.optInt("objectID", objectId),
                        title = title,
                        artistDisplayName = artist,
                        artistDisplayBio = obj.optString("artistDisplayBio", ""),
                        objectDate = obj.optString("objectDate", ""),
                        medium = obj.optString("medium", ""),
                        dimensions = obj.optString("dimensions", ""),
                        department = obj.optString("department", ""),
                        creditLine = obj.optString("creditLine", ""),
                        accessionNumber = obj.optString("accessionNumber", ""),
                        imageUrl = displayUrl,
                        thumbnailUrl = displayUrl,
                        culture = obj.optString("culture", ""),
                        period = obj.optString("period", ""),
                        isHighlight = obj.optBoolean("isHighlight", false),
                        isFavorite = favSet.contains(objectId),
                        description = dynamicSummary
                    )
                }
            }
        } catch (_: Exception) {
            // Ignore individual object fetch errors
        }
        return@withContext null
    }
}
