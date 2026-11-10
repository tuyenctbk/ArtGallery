package com.example.data

import android.content.Context
import com.example.data.db.ArtGalleryDatabase
import com.example.data.db.FavoriteArtworkEntity
import com.example.data.model.Artwork
import com.example.data.model.CategoryType
import com.example.data.model.MuseumCategory
import kotlinx.coroutines.Dispatchers
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

class MetMuseumRepository(private val context: Context) {

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

    // Comprehensive list of Art Movements & Met Museum Departments
    val museumCategories: List<MuseumCategory> = listOf(
        MuseumCategory(
            id = "all",
            title = "All Masterpieces",
            categoryType = CategoryType.ALL,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP130999.jpg",
            description = "Complete cross-department curated collection of world masterpieces"
        ),
        MuseumCategory(
            id = "favorites",
            title = "Private Gallery",
            categoryType = CategoryType.FAVORITES,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP169935.jpg",
            description = "Your saved personal collection of favorite artworks in Room database"
        ),

        // --- ART MOVEMENTS ---
        MuseumCategory(
            id = "mv_impressionism",
            title = "Impressionism",
            categoryType = CategoryType.MOVEMENT,
            query = "Impressionism",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-23192-001.jpg",
            description = "Light, movement, and vivid color by Monet, Degas, Manet & Renoir"
        ),
        MuseumCategory(
            id = "mv_post_impressionism",
            title = "Post-Impressionism",
            categoryType = CategoryType.MOVEMENT,
            query = "Post-Impressionism",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP229743.jpg",
            description = "Emotional expression & bold form by Van Gogh, Cézanne & Gauguin"
        ),
        MuseumCategory(
            id = "mv_renaissance",
            title = "Renaissance",
            categoryType = CategoryType.MOVEMENT,
            query = "Renaissance",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14285-001.jpg",
            description = "Classical revival & perspective masterworks from 14th–16th century Europe"
        ),
        MuseumCategory(
            id = "mv_dutch_golden_age",
            title = "Dutch Golden Age",
            categoryType = CategoryType.MOVEMENT,
            query = "Dutch Golden Age",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14284-001.jpg",
            description = "Luminous light & intimate realism by Rembrandt & Vermeer"
        ),
        MuseumCategory(
            id = "mv_baroque",
            title = "Baroque",
            categoryType = CategoryType.MOVEMENT,
            query = "Baroque",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14070-001.jpg",
            description = "Dramatic chiaroscuro & grandeur from 17th-century European masters"
        ),
        MuseumCategory(
            id = "mv_ukiyo_e",
            title = "Japanese Ukiyo-e",
            categoryType = CategoryType.MOVEMENT,
            query = "woodblock print",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/as/web-large/DP130155.jpg",
            description = "Edo period floating world woodblock prints by Hokusai & Hiroshige"
        ),
        MuseumCategory(
            id = "mv_neoclassicism",
            title = "Neoclassicism",
            categoryType = CategoryType.MOVEMENT,
            query = "Neoclassicism",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14070-001.jpg",
            description = "Heroic order, harmony & classical antiquity by David & Ingres"
        ),

        // --- MET MUSEUM DEPARTMENTS ---
        MuseumCategory(
            id = "dept_9",
            title = "European Paintings",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 9,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP121473.jpg",
            description = "Masterworks from the 13th to early 20th century in Western Europe"
        ),
        MuseumCategory(
            id = "dept_6",
            title = "Asian Art",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 6,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/as/web-large/DP254203.jpg",
            description = "Paintings, screens, ceramics & sculptures spanning 5,000 years of Asian history"
        ),
        MuseumCategory(
            id = "dept_1",
            title = "The American Wing",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 1,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ap/web-large/DP215410.jpg",
            description = "American painting, sculpture & decorative arts from the 18th–20th centuries"
        ),
        MuseumCategory(
            id = "dept_11",
            title = "Greek and Roman Art",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 11,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/gr/web-large/DP115201.jpg",
            description = "Classical marble statues, vases & bronzes from ancient Mediterranean civilizations"
        ),
        MuseumCategory(
            id = "dept_8",
            title = "Egyptian Art",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 8,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/gr/web-large/DP115201.jpg",
            description = "Ancient Egyptian monuments, papyri & tomb treasures"
        ),
        MuseumCategory(
            id = "dept_15",
            title = "Medieval Art & The Cloisters",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 15,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14285-001.jpg",
            description = "Illuminated manuscripts, tapestries & Gothic architecture"
        )
    )

    // Curated high-resolution CC0 masterworks
    val curatedMasterpieces: List<Artwork> = listOf(
        Artwork(
            id = 436535,
            title = "Wheat Field with Cypresses",
            artistDisplayName = "Vincent van Gogh",
            artistDisplayBio = "Dutch, Zundert 1853–1890 Auvers-sur-Oise",
            objectDate = "1889",
            medium = "Oil on canvas",
            dimensions = "28 3/4 x 36 3/4 in. (73 x 93.4 cm)",
            department = "European Paintings",
            creditLine = "Purchase, The Annenberg Foundation Gift, 1993",
            accessionNumber = "1993.132",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP130999.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP130999.jpg",
            culture = "Dutch",
            period = "Post-Impressionism",
            isHighlight = true,
            description = "Wheat Field with Cypresses was painted in June 1889 at the Saint-Paul-de-Mausole asylum in Saint-Rémy, France."
        ),
        Artwork(
            id = 437980,
            title = "The Water Lily Pond",
            artistDisplayName = "Claude Monet",
            artistDisplayBio = "French, Paris 1840–1926 Giverny",
            objectDate = "1899",
            medium = "Oil on canvas",
            dimensions = "36 1/2 x 29 in. (92.7 x 73.7 cm)",
            department = "European Paintings",
            creditLine = "H. O. Havemeyer Collection, Bequest of Mrs. H. O. Havemeyer, 1929",
            accessionNumber = "29.100.113",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP-23192-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-23192-001.jpg",
            culture = "French",
            period = "Impressionism",
            isHighlight = true,
            description = "In 1893, Monet purchased land with a pond near his property in Giverny, creating his iconic water garden motives."
        ),
        Artwork(
            id = 437984,
            title = "Bridge over a Pond of Water Lilies",
            artistDisplayName = "Claude Monet",
            artistDisplayBio = "French, Paris 1840–1926 Giverny",
            objectDate = "1899",
            medium = "Oil on canvas",
            dimensions = "36 1/2 x 29 in. (92.7 x 73.7 cm)",
            department = "European Paintings",
            creditLine = "H. O. Havemeyer Collection, Bequest of Mrs. H. O. Havemeyer, 1929",
            accessionNumber = "29.100.113",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP-14286-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14286-001.jpg",
            culture = "French",
            period = "Impressionism",
            isHighlight = true,
            description = "Monet painted eighteen views of the wooden footbridge over his lily pond in 1899."
        ),
        Artwork(
            id = 436524,
            title = "Self-Portrait with a Straw Hat",
            artistDisplayName = "Vincent van Gogh",
            artistDisplayBio = "Dutch, Zundert 1853–1890 Auvers-sur-Oise",
            objectDate = "1887",
            medium = "Oil on canvas",
            dimensions = "16 x 12 1/2 in. (40.6 x 31.8 cm)",
            department = "European Paintings",
            creditLine = "Bequest of Miss Adelaide Milton de Groot (1876–1967), 1967",
            accessionNumber = "67.187.70a",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP229743.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP229743.jpg",
            culture = "Dutch",
            period = "Post-Impressionism",
            isHighlight = true,
            description = "Van Gogh produced over twenty self-portraits during his stay in Paris (1886–88)."
        ),
        Artwork(
            id = 437329,
            title = "Madame X (Madame Pierre Gautreau)",
            artistDisplayName = "John Singer Sargent",
            artistDisplayBio = "American, Florence 1856–1925 London",
            objectDate = "1883–84",
            medium = "Oil on canvas",
            dimensions = "82 1/8 x 43 1/4 in. (208.6 x 109.9 cm)",
            department = "The American Wing",
            creditLine = "Arthur Hoppock Hearn Fund, 1916",
            accessionNumber = "16.53",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP121473.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP121473.jpg",
            culture = "American",
            period = "19th Century",
            isHighlight = true,
            description = "Sargent's dramatic portrait of Virginie Amélie Avegno Gautreau caused a sensation at the 1884 Paris Salon."
        ),
        Artwork(
            id = 437397,
            title = "The Death of Socrates",
            artistDisplayName = "Jacques Louis David",
            artistDisplayBio = "French, Paris 1748–1825 Brussels",
            objectDate = "1787",
            medium = "Oil on canvas",
            dimensions = "51 x 77 1/4 in. (129.5 x 196.2 cm)",
            department = "European Paintings",
            creditLine = "Catharine Lorillard Wolfe Collection, Wolfe Fund, 1931",
            accessionNumber = "31.45",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP-14070-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14070-001.jpg",
            culture = "French",
            period = "Neoclassicism",
            isHighlight = true,
            description = "Socrates prepares to drink hemlock while speaking on the immortality of the soul."
        ),
        Artwork(
            id = 436105,
            title = "Aristotle with a Bust of Homer",
            artistDisplayName = "Rembrandt (Rembrandt van Rijn)",
            artistDisplayBio = "Dutch, Leiden 1606–1669 Amsterdam",
            objectDate = "1653",
            medium = "Oil on canvas",
            dimensions = "56 1/2 x 53 7/8 in. (143.5 x 136.8 cm)",
            department = "European Paintings",
            creditLine = "Purchase, special contributions given or bequeathed by friends of the Museum, 1961",
            accessionNumber = "61.198",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP-14284-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14284-001.jpg",
            culture = "Dutch",
            period = "Dutch Golden Age",
            isHighlight = true,
            description = "Aristotle rests a hand meditatively on a marble bust of Homer."
        ),
        Artwork(
            id = 437881,
            title = "Young Woman with a Water Pitcher",
            artistDisplayName = "Johannes Vermeer",
            artistDisplayBio = "Dutch, Delft 1632–1675 Delft",
            objectDate = "ca. 1662",
            medium = "Oil on canvas",
            dimensions = "18 x 16 in. (45.7 x 40.6 cm)",
            department = "European Paintings",
            creditLine = "Marquand Collection, Gift of Henry G. Marquand, 1889",
            accessionNumber = "89.15.21",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP169935.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP169935.jpg",
            culture = "Dutch",
            period = "Dutch Golden Age",
            isHighlight = true,
            description = "An exquisite study of light showing a young woman opening a window."
        ),
        Artwork(
            id = 45434,
            title = "Under the Wave off Kanagawa (The Great Wave)",
            artistDisplayName = "Katsushika Hokusai",
            artistDisplayBio = "Japanese, Tokyo (Edo) 1759–1849 Tokyo (Edo)",
            objectDate = "ca. 1830–32",
            medium = "Woodblock print; ink and color on paper",
            dimensions = "10 1/8 x 14 15/16 in. (25.7 x 37.9 cm)",
            department = "Asian Art",
            creditLine = "The H. O. Havemeyer Collection, Bequest of Mrs. H. O. Havemeyer, 1929",
            accessionNumber = "29.100.1164",
            imageUrl = "https://images.metmuseum.org/CRDImages/as/original/DP130155.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/as/web-large/DP130155.jpg",
            culture = "Japanese",
            period = "Edo period",
            isHighlight = true,
            description = "The iconic ukiyo-e woodblock print depicting a towering wave off Kanagawa."
        ),
        Artwork(
            id = 436838,
            title = "The Dance Class",
            artistDisplayName = "Edgar Degas",
            artistDisplayBio = "French, Paris 1834–1917 Paris",
            objectDate = "1874",
            medium = "Oil on canvas",
            dimensions = "32 7/8 x 30 1/4 in. (83.5 x 76.8 cm)",
            department = "European Paintings",
            creditLine = "H. O. Havemeyer Collection, Bequest of Mrs. H. O. Havemeyer, 1929",
            accessionNumber = "29.100.184",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP-20101-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-20101-001.jpg",
            culture = "French",
            period = "Impressionism",
            isHighlight = true,
            description = "Degas captures ballerina students practicing under ballet master Jules Perrot."
        ),
        Artwork(
            id = 437658,
            title = "A Sunday on La Grande Jatte — 1884",
            artistDisplayName = "Georges Seurat",
            artistDisplayBio = "French, Paris 1859–1891 Paris",
            objectDate = "1884–86",
            medium = "Oil on canvas",
            dimensions = "81 3/4 x 121 1/4 in. (207.6 x 308 cm)",
            department = "European Paintings",
            creditLine = "The Metropolitan Museum of Art / Art Institute Study Collection",
            accessionNumber = "1926.224",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP123844.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP123844.jpg",
            culture = "French",
            period = "Pointillism / Post-Impressionism",
            isHighlight = true,
            description = "Seurat's masterwork of Pointillism along the Seine."
        ),
        Artwork(
            id = 435882,
            title = "The Card Players",
            artistDisplayName = "Paul Cézanne",
            artistDisplayBio = "French, Aix-en-Provence 1839–1906 Aix-en-Provence",
            objectDate = "1890–92",
            medium = "Oil on canvas",
            dimensions = "25 3/4 x 32 in. (65.4 x 81.3 cm)",
            department = "European Paintings",
            creditLine = "Bequest of Stephen C. Clark, 1960",
            accessionNumber = "61.101.1",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP-17094-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-17094-001.jpg",
            culture = "French",
            period = "Post-Impressionism",
            isHighlight = true,
            description = "Two Provençal peasants absorbed in their card game."
        ),
        Artwork(
            id = 435844,
            title = "The Harvesters",
            artistDisplayName = "Pieter Bruegel the Elder",
            artistDisplayBio = "Netherlandish, Breda (?) ca. 1525–1569 Brussels",
            objectDate = "1565",
            medium = "Oil on wood",
            dimensions = "46 1/2 x 63 3/4 in. (118.1 x 161.9 cm)",
            department = "European Paintings",
            creditLine = "Rogers Fund, 1919",
            accessionNumber = "19.164",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/original/DP-14285-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14285-001.jpg",
            culture = "Netherlandish",
            period = "Northern Renaissance",
            isHighlight = true,
            description = "A watershed moment in Western art representing late summer harvesting."
        ),
        Artwork(
            id = 436896,
            title = "Washington Crossing the Delaware",
            artistDisplayName = "Emanuel Leutze",
            artistDisplayBio = "American, Schwäbisch Gmünd 1816–1868 Washington, D.C.",
            objectDate = "1851",
            medium = "Oil on canvas",
            dimensions = "149 x 255 in. (378.5 x 647.7 cm)",
            department = "The American Wing",
            creditLine = "Gift of John Stewart Kennedy, 1897",
            accessionNumber = "97.34",
            imageUrl = "https://images.metmuseum.org/CRDImages/ap/original/DP215410.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ap/web-large/DP215410.jpg",
            culture = "American",
            period = "19th Century",
            isHighlight = true,
            description = "Monumental painting commemorating Washington crossing the icy Delaware."
        ),
        Artwork(
            id = 45524,
            title = "Irises at Yatsuhashi (Eight Bridges)",
            artistDisplayName = "Ogata Kōrin",
            artistDisplayBio = "Japanese, Kyoto 1658–1716 Kyoto",
            objectDate = "after 1709",
            medium = "Pair of six-panel folding screens; ink and color on gold leaf on paper",
            dimensions = "Each screen: 70 1/2 x 146 1/4 in. (179.1 x 371.5 cm)",
            department = "Asian Art",
            creditLine = "Purchase, Louisa Eldridge McBurney Gift, 1953",
            accessionNumber = "53.7.1,.2",
            imageUrl = "https://images.metmuseum.org/CRDImages/as/original/DP254203.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/as/web-large/DP254203.jpg",
            culture = "Japanese",
            period = "Edo period",
            isHighlight = true,
            description = "Dynamic masterpiece of Rimpa style on gold leaf."
        )
    )

    suspend fun getArtworksForCategory(category: MuseumCategory): List<Artwork> = withContext(Dispatchers.IO) {
        val favSet = favoriteIds.first()
        if (category.categoryType == CategoryType.FAVORITES) {
            val roomFavs = favoriteArtworksFlow.first()
            if (roomFavs.isNotEmpty()) return@withContext roomFavs
            return@withContext curatedMasterpieces.filter { favSet.contains(it.id) }.map { it.copy(isFavorite = true) }
        }

        val baseList = when (category.categoryType) {
            CategoryType.ALL -> curatedMasterpieces
            CategoryType.FAVORITES -> emptyList()
            CategoryType.MOVEMENT -> {
                curatedMasterpieces.filter {
                    it.period.contains(category.query, ignoreCase = true) ||
                            it.description.contains(category.query, ignoreCase = true) ||
                            it.title.contains(category.query, ignoreCase = true)
                }
            }
            CategoryType.DEPARTMENT -> {
                curatedMasterpieces.filter {
                    it.department.equals(category.title, ignoreCase = true) ||
                            (category.departmentId != null && (it.department.contains("European", ignoreCase = true) && category.departmentId == 9))
                }
            }
        }
        val resultList = if (baseList.isEmpty()) curatedMasterpieces else baseList
        return@withContext resultList.map { it.copy(isFavorite = favSet.contains(it.id)) }
    }

    suspend fun fetchArtworksFromMetApi(category: MuseumCategory): List<Artwork> = withContext(Dispatchers.IO) {
        val favSet = favoriteIds.first()
        if (category.categoryType == CategoryType.FAVORITES) {
            val roomFavs = favoriteArtworksFlow.first()
            if (roomFavs.isNotEmpty()) return@withContext roomFavs
            return@withContext curatedMasterpieces.filter { favSet.contains(it.id) }.map { it.copy(isFavorite = true) }
        }

        val fetchedList = mutableListOf<Artwork>()
        try {
            val endpointUrl = when {
                category.departmentId != null ->
                    "https://collectionapi.metmuseum.org/public/collection/v1/search?departmentId=${category.departmentId}&hasImages=true&isHighlight=true&q=painting"
                category.query.isNotEmpty() ->
                    "https://collectionapi.metmuseum.org/public/collection/v1/search?hasImages=true&isHighlight=true&q=${category.query}"
                else ->
                    "https://collectionapi.metmuseum.org/public/collection/v1/search?hasImages=true&isHighlight=true&q=paintings"
            }

            val conn = URL(endpointUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(json)
                val objectIDs = obj.optJSONArray("objectIDs")
                if (objectIDs != null) {
                    val limit = minOf(12, objectIDs.length())
                    for (i in 0 until limit) {
                        val id = objectIDs.getInt(i)
                        val art = fetchMetMuseumObjectDetails(id)
                        if (art != null) {
                            fetchedList.add(art.copy(isFavorite = favSet.contains(art.id)))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val localList = getArtworksForCategory(category)
        val combined = if (fetchedList.isNotEmpty()) {
            (fetchedList + localList).distinctBy { it.id }
        } else {
            localList
        }
        return@withContext combined.map { it.copy(isFavorite = favSet.contains(it.id)) }
    }

    suspend fun fetchMetMuseumObjectDetails(objectId: Int): Artwork? = withContext(Dispatchers.IO) {
        val favSet = favoriteIds.first()
        try {
            val url = URL("https://collectionapi.metmuseum.org/public/collection/v1/objects/$objectId")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(json)
                val primaryImage = obj.optString("primaryImage", "")
                val primaryImageSmall = obj.optString("primaryImageSmall", primaryImage)
                if (primaryImage.isNotEmpty()) {
                    return@withContext Artwork(
                        id = obj.optInt("objectID", objectId),
                        title = obj.optString("title", "Untitled"),
                        artistDisplayName = obj.optString("artistDisplayName", "Unknown Artist"),
                        artistDisplayBio = obj.optString("artistDisplayBio", ""),
                        objectDate = obj.optString("objectDate", ""),
                        medium = obj.optString("medium", ""),
                        dimensions = obj.optString("dimensions", ""),
                        department = obj.optString("department", "Metropolitan Museum of Art"),
                        creditLine = obj.optString("creditLine", ""),
                        accessionNumber = obj.optString("accessionNumber", ""),
                        imageUrl = primaryImage,
                        thumbnailUrl = primaryImageSmall,
                        culture = obj.optString("culture", ""),
                        period = obj.optString("period", ""),
                        isHighlight = obj.optBoolean("isHighlight", false),
                        isFavorite = favSet.contains(objectId)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
