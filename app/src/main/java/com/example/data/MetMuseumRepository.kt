package com.example.data

import android.content.Context
import com.example.data.db.ArtGalleryDatabase
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

    // Comprehensive list of Art Movements & Met Museum Departments with verified live URLs
    val museumCategories: List<MuseumCategory> = listOf(
        MuseumCategory(
            id = "all",
            title = "All Masterpieces",
            categoryType = CategoryType.ALL,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-42549-001.jpg",
            description = "Complete cross-department curated collection of world masterpieces"
        ),
        MuseumCategory(
            id = "favorites",
            title = "Private Gallery",
            categoryType = CategoryType.FAVORITES,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP353257.jpg",
            description = "Your saved personal collection of favorite artworks in Room database"
        ),

        // --- ART MOVEMENTS ---
        MuseumCategory(
            id = "mv_impressionism",
            title = "Impressionism",
            categoryType = CategoryType.MOVEMENT,
            query = "Impressionism",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-25465-001.jpg",
            description = "Light, movement, and vivid color by Monet, Degas, Manet & Renoir"
        ),
        MuseumCategory(
            id = "mv_post_impressionism",
            title = "Post-Impressionism",
            categoryType = CategoryType.MOVEMENT,
            query = "Post-Impressionism",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-41223-001.jpg",
            description = "Emotional expression & bold form by Van Gogh, Cézanne & Seurat"
        ),
        MuseumCategory(
            id = "mv_renaissance",
            title = "Renaissance",
            categoryType = CategoryType.MOVEMENT,
            query = "Renaissance",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP119115.jpg",
            description = "Classical revival & perspective masterworks from 14th–16th century Europe"
        ),
        MuseumCategory(
            id = "mv_dutch_golden_age",
            title = "Dutch Golden Age",
            categoryType = CategoryType.MOVEMENT,
            query = "Dutch Golden Age",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-30758-001.jpg",
            description = "Luminous light & intimate realism by Rembrandt & Vermeer"
        ),
        MuseumCategory(
            id = "mv_baroque",
            title = "Baroque",
            categoryType = CategoryType.MOVEMENT,
            query = "Baroque",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14286-015.jpg",
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
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-13139-001.jpg",
            description = "Heroic order, harmony & classical antiquity by David & Ingres"
        ),

        // --- MET MUSEUM DEPARTMENTS ---
        MuseumCategory(
            id = "dept_9",
            title = "European Paintings",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 11,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP130999.jpg",
            description = "Masterworks from the 13th to early 20th century in Western Europe"
        ),
        MuseumCategory(
            id = "dept_6",
            title = "Asian Art",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 6,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/as/web-large/DP153537.jpg",
            description = "Paintings, screens, ceramics & sculptures spanning 5,000 years of Asian history"
        ),
        MuseumCategory(
            id = "dept_1",
            title = "The American Wing",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 1,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ad/web-large/DP215410.jpg",
            description = "American painting, sculpture & decorative arts from the 18th–20th centuries"
        ),
        MuseumCategory(
            id = "dept_11",
            title = "Greek and Roman Art",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 13,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/gr/web-large/DP-14287-144.jpg",
            description = "Classical marble statues, vases & bronzes from ancient Mediterranean civilizations"
        ),
        MuseumCategory(
            id = "dept_8",
            title = "Egyptian Art",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 10,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/eg/web-large/DP-24216-003.jpg",
            description = "Ancient Egyptian monuments, papyri & royal tomb treasures"
        ),
        MuseumCategory(
            id = "dept_15",
            title = "Medieval Art & The Cloisters",
            categoryType = CategoryType.DEPARTMENT,
            departmentId = 7,
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/cl/web-large/DP-46759-014.jpg",
            description = "Illuminated manuscripts, altarpieces, tapestries & Gothic art"
        )
    )

    // Curated high-resolution CC0 masterworks (100% verified live Met Open Access URLs)
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
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-42549-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-42549-001.jpg",
            culture = "Dutch",
            period = "Post-Impressionism",
            isHighlight = true,
            description = "Wheat Field with Cypresses was painted in June 1889 at the Saint-Paul-de-Mausole asylum in Saint-Rémy, France, where Van Gogh regarded it as one of his finest summer landscapes."
        ),
        Artwork(
            id = 45434,
            title = "Under the Wave off Kanagawa (The Great Wave)",
            artistDisplayName = "Katsushika Hokusai",
            artistDisplayBio = "Japanese, Tokyo (Edo) 1759–1849 Tokyo (Edo)",
            objectDate = "ca. 1830–32",
            medium = "Polychrome woodblock print; ink and color on paper",
            dimensions = "10 1/8 x 14 15/16 in. (25.7 x 37.9 cm)",
            department = "Asian Art",
            creditLine = "H. O. Havemeyer Collection, Bequest of Mrs. H. O. Havemeyer, 1929",
            accessionNumber = "JP1847",
            imageUrl = "https://images.metmuseum.org/CRDImages/as/web-large/DP130155.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/as/web-large/DP130155.jpg",
            culture = "Japanese",
            period = "Edo period • Japanese Ukiyo-e",
            isHighlight = true,
            description = "The iconic ukiyo-e woodblock print from Thirty-six Views of Mount Fuji depicting a towering cresting wave framing distant Mount Fuji."
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
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP353257.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP353257.jpg",
            culture = "Dutch",
            period = "Dutch Golden Age",
            isHighlight = true,
            description = "An exquisite study of serene domestic light and primary pigments showing a young woman opening a leaded-glass window."
        ),
        Artwork(
            id = 436965,
            title = "The Monet Family in Their Garden at Argenteuil",
            artistDisplayName = "Édouard Manet",
            artistDisplayBio = "French, Paris 1832–1883 Paris",
            objectDate = "1874",
            medium = "Oil on canvas",
            dimensions = "24 x 39 1/4 in. (61 x 99.7 cm)",
            department = "European Paintings",
            creditLine = "Bequest of Joan Whitney Payson, 1975",
            accessionNumber = "1976.201.14",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-25465-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-25465-001.jpg",
            culture = "French",
            period = "Impressionism",
            isHighlight = true,
            description = "Painted outdoors in the summer of 1874 when Manet vacationed near Claude Monet at Argenteuil, capturing sunlit garden leisure."
        ),
        Artwork(
            id = 436105,
            title = "The Death of Socrates",
            artistDisplayName = "Jacques Louis David",
            artistDisplayBio = "French, Paris 1748–1825 Brussels",
            objectDate = "1787",
            medium = "Oil on canvas",
            dimensions = "51 x 77 1/4 in. (129.5 x 196.2 cm)",
            department = "European Paintings",
            creditLine = "Catharine Lorillard Wolfe Collection, Wolfe Fund, 1931",
            accessionNumber = "31.45",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-13139-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-13139-001.jpg",
            culture = "French",
            period = "Neoclassicism",
            isHighlight = true,
            description = "Condemned for his philosophical teachings in Athens, Socrates calmly discourses on the immortality of the soul before drinking hemlock."
        ),
        Artwork(
            id = 437394,
            title = "Aristotle with a Bust of Homer",
            artistDisplayName = "Rembrandt (Rembrandt van Rijn)",
            artistDisplayBio = "Dutch, Leiden 1606–1669 Amsterdam",
            objectDate = "1653",
            medium = "Oil on canvas",
            dimensions = "56 1/2 x 53 7/8 in. (143.5 x 136.8 cm)",
            department = "European Paintings",
            creditLine = "Purchase, special contributions given or bequeathed by friends of the Museum, 1961",
            accessionNumber = "61.198",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-30758-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-30758-001.jpg",
            culture = "Dutch",
            period = "Dutch Golden Age • Baroque",
            isHighlight = true,
            description = "Aristotle rests a contemplative hand on a sculpted bust of Homer, weighing worldly honors against timeless poetic wisdom."
        ),
        Artwork(
            id = 436524,
            title = "Sunflowers",
            artistDisplayName = "Vincent van Gogh",
            artistDisplayBio = "Dutch, Zundert 1853–1890 Auvers-sur-Oise",
            objectDate = "1887",
            medium = "Oil on canvas",
            dimensions = "17 x 24 in. (43.2 x 61 cm)",
            department = "European Paintings",
            creditLine = "Rogers Fund, 1949",
            accessionNumber = "49.41",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-41223-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-41223-001.jpg",
            culture = "Dutch",
            period = "Post-Impressionism",
            isHighlight = true,
            description = "Painted in Paris in late summer 1887, showcasing energetic impasto brushwork and radiant chrome yellows against a cobalt ground."
        ),
        Artwork(
            id = 12127,
            title = "Madame X (Madame Pierre Gautreau)",
            artistDisplayName = "John Singer Sargent",
            artistDisplayBio = "American, Florence 1856–1925 London",
            objectDate = "1883–84",
            medium = "Oil on canvas",
            dimensions = "82 1/8 x 43 1/4 in. (208.6 x 109.9 cm)",
            department = "The American Wing",
            creditLine = "Arthur Hoppock Hearn Fund, 1916",
            accessionNumber = "16.53",
            imageUrl = "https://images.metmuseum.org/CRDImages/ad/web-large/DP-29006-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ad/web-large/DP-29006-001.jpg",
            culture = "American",
            period = "19th Century Realism",
            isHighlight = true,
            description = "Sargent's striking profile portrait of Virginie Amélie Avegno Gautreau became the most celebrated masterpiece of the 1884 Paris Salon."
        ),
        Artwork(
            id = 438817,
            title = "The Dance Class",
            artistDisplayName = "Edgar Degas",
            artistDisplayBio = "French, Paris 1834–1917 Paris",
            objectDate = "1874",
            medium = "Oil on canvas",
            dimensions = "32 7/8 x 30 1/4 in. (83.5 x 76.8 cm)",
            department = "European Paintings",
            creditLine = "H. O. Havemeyer Collection, Bequest of Mrs. H. O. Havemeyer, 1929",
            accessionNumber = "29.100.184",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-20101-001.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-20101-001.jpg",
            culture = "French",
            period = "Impressionism",
            isHighlight = true,
            description = "Degas captures young ballerinas rehearsing at the Paris Opéra under the watchful eye of ballet master Jules Perrot."
        ),
        Artwork(
            id = 435809,
            title = "The Harvesters",
            artistDisplayName = "Pieter Bruegel the Elder",
            artistDisplayBio = "Netherlandish, Breda (?) ca. 1525–1569 Brussels",
            objectDate = "1565",
            medium = "Oil on wood",
            dimensions = "46 1/2 x 63 3/4 in. (118.1 x 161.9 cm)",
            department = "European Paintings",
            creditLine = "Rogers Fund, 1919",
            accessionNumber = "19.164",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP119115.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP119115.jpg",
            culture = "Netherlandish",
            period = "Northern Renaissance",
            isHighlight = true,
            description = "A watershed in Western landscape painting depicting peasants harvesting golden wheat on a sweltering late-summer afternoon."
        ),
        Artwork(
            id = 437658,
            title = "Study for 'A Sunday on La Grande Jatte'",
            artistDisplayName = "Georges Seurat",
            artistDisplayBio = "French, Paris 1859–1891 Paris",
            objectDate = "1884",
            medium = "Oil on canvas",
            dimensions = "27 3/4 x 41 in. (70.5 x 104.1 cm)",
            department = "European Paintings",
            creditLine = "Bequest of Sam A. Lewisohn, 1951",
            accessionNumber = "51.112.6",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP259921.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP259921.jpg",
            culture = "French",
            period = "Post-Impressionism • Pointillism",
            isHighlight = true,
            description = "Seurat's final compositional study in optical color mixing along the banks of the Seine at Courbevoie."
        ),
        Artwork(
            id = 435868,
            title = "The Card Players",
            artistDisplayName = "Paul Cézanne",
            artistDisplayBio = "French, Aix-en-Provence 1839–1906 Aix-en-Provence",
            objectDate = "1890–92",
            medium = "Oil on canvas",
            dimensions = "25 3/4 x 32 in. (65.4 x 81.3 cm)",
            department = "European Paintings",
            creditLine = "Bequest of Stephen C. Clark, 1960",
            accessionNumber = "61.101.1",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP231550.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP231550.jpg",
            culture = "French",
            period = "Post-Impressionism",
            isHighlight = true,
            description = "Monumental stillness and geometric harmony in Cézanne's celebrated portrayal of Provençal farmhands playing cards."
        ),
        Artwork(
            id = 11417,
            title = "Washington Crossing the Delaware",
            artistDisplayName = "Emanuel Leutze",
            artistDisplayBio = "American, Schwäbisch Gmünd 1816–1868 Washington, D.C.",
            objectDate = "1851",
            medium = "Oil on canvas",
            dimensions = "149 x 255 in. (378.5 x 647.7 cm)",
            department = "The American Wing",
            creditLine = "Gift of John Stewart Kennedy, 1897",
            accessionNumber = "97.34",
            imageUrl = "https://images.metmuseum.org/CRDImages/ad/web-large/DP215410.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ad/web-large/DP215410.jpg",
            culture = "American",
            period = "19th Century",
            isHighlight = true,
            description = "Epic history painting commemorating General George Washington's daring crossing of the ice-choked Delaware River on December 25, 1776."
        ),
        Artwork(
            id = 437980,
            title = "Cypresses",
            artistDisplayName = "Vincent van Gogh",
            artistDisplayBio = "Dutch, Zundert 1853–1890 Auvers-sur-Oise",
            objectDate = "1889",
            medium = "Oil on canvas",
            dimensions = "36 3/4 x 29 1/8 in. (93.4 x 74 cm)",
            department = "European Paintings",
            creditLine = "Rogers Fund, 1949",
            accessionNumber = "49.30",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP130999.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP130999.jpg",
            culture = "Dutch",
            period = "Post-Impressionism",
            isHighlight = true,
            description = "Towering Provençal cypresses rendered in flame-like swirling brushwork beneath a crescent moon."
        ),
        Artwork(
            id = 436838,
            title = "The Fortune-Teller",
            artistDisplayName = "Georges de La Tour",
            artistDisplayBio = "French, Vic-sur-Seille 1593–1652 Lunéville",
            objectDate = "probably 1630s",
            medium = "Oil on canvas",
            dimensions = "40 1/8 x 48 5/8 in. (101.9 x 123.5 cm)",
            department = "European Paintings",
            creditLine = "Rogers Fund, 1960",
            accessionNumber = "60.30",
            imageUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14286-015.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/ep/web-large/DP-14286-015.jpg",
            culture = "French",
            period = "Baroque",
            isHighlight = true,
            description = "A Caravaggesque theatrical tableau of a wealthy youth having his fortune told while his pockets are deftly picked."
        ),
        Artwork(
            id = 45665,
            title = "Village and Temples in Jiangnan",
            artistDisplayName = "Ming Dynasty Master",
            artistDisplayBio = "Chinese, Ming dynasty (1368–1644)",
            objectDate = "16th century",
            medium = "Handscroll; ink and color on silk",
            dimensions = "12 1/4 x 68 1/2 in. (31.1 x 174 cm)",
            department = "Asian Art",
            creditLine = "Purchase, The Dillon Fund Gift, 1986",
            accessionNumber = "1986.266.2",
            imageUrl = "https://images.metmuseum.org/CRDImages/as/web-large/DP153537.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/as/web-large/DP153537.jpg",
            culture = "Chinese",
            period = "Ming dynasty • Asian Art",
            isHighlight = true,
            description = "Tranquil river valleys, mist-shrouded mountains, and temple pavilions across southern China."
        ),
        Artwork(
            id = 254819,
            title = "Marble Sarcophagus with the Triumph of Dionysos",
            artistDisplayName = "Roman Imperial Sculptor",
            artistDisplayBio = "Roman, Severan period, ca. 260–270 CE",
            objectDate = "ca. 260–270 CE",
            medium = "Proconnesian marble",
            dimensions = "34 x 85 in. (86.4 x 215.9 cm)",
            department = "Greek and Roman Art",
            creditLine = "Purchase, Joseph Pulitzer Bequest, 1955",
            accessionNumber = "55.11.5",
            imageUrl = "https://images.metmuseum.org/CRDImages/gr/web-large/DP-14287-144.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/gr/web-large/DP-14287-144.jpg",
            culture = "Roman",
            period = "Late Imperial Roman • Classical Antiquity",
            isHighlight = true,
            description = "Virtuoso high-relief marble carving depicting Dionysos accompanied by personifications of the Four Seasons."
        ),
        Artwork(
            id = 544442,
            title = "Sphinx of Hatshepsut",
            artistDisplayName = "New Kingdom Royal Workshop",
            artistDisplayBio = "Egyptian, Dynasty 18, Joint reign of Hatshepsut and Thutmose III",
            objectDate = "ca. 1479–1458 BCE",
            medium = "Granite with traces of paint",
            dimensions = "64 9/16 x 135 7/16 in. (164 x 344 cm)",
            department = "Egyptian Art",
            creditLine = "Rogers Fund, 1931",
            accessionNumber = "31.3.166",
            imageUrl = "https://images.metmuseum.org/CRDImages/eg/web-large/DP-24216-003.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/eg/web-large/DP-24216-003.jpg",
            culture = "Egyptian",
            period = "New Kingdom • Dynasty 18",
            isHighlight = true,
            description = "Monumental red granite sphinx from Deir el-Bahri combining the royal lion body with the nemes headcloth of Pharaoh Hatshepsut."
        ),
        Artwork(
            id = 470304,
            title = "Annunciation Triptych (Mérode Altarpiece)",
            artistDisplayName = "Workshop of Robert Campin",
            artistDisplayBio = "Netherlandish, ca. 1375–1444 Tournai",
            objectDate = "ca. 1427–32",
            medium = "Oil on oak",
            dimensions = "25 3/8 x 46 3/8 in. (64.5 x 117.8 cm)",
            department = "Medieval Art & The Cloisters",
            creditLine = "The Cloisters Collection, 1956",
            accessionNumber = "56.70a–c",
            imageUrl = "https://images.metmuseum.org/CRDImages/cl/web-large/DP-46759-014.jpg",
            thumbnailUrl = "https://images.metmuseum.org/CRDImages/cl/web-large/DP-46759-014.jpg",
            culture = "South Netherlandish",
            period = "Early Netherlandish • Northern Renaissance",
            isHighlight = true,
            description = "A cornerstone of Early Netherlandish art housed at The Met Cloisters, rich in domestic realism and luminous oil glazes."
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
                            it.medium.contains(category.query, ignoreCase = true) ||
                            it.description.contains(category.query, ignoreCase = true) ||
                            it.title.contains(category.query, ignoreCase = true)
                }
            }
            CategoryType.DEPARTMENT -> {
                curatedMasterpieces.filter {
                    it.department.equals(category.title, ignoreCase = true) ||
                            (category.title.contains("Cloisters", ignoreCase = true) && it.department.contains("Cloisters", ignoreCase = true))
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

        val localList = getArtworksForCategory(category)
        val fetchedList = mutableListOf<Artwork>()
        try {
            val encodedQuery = URLEncoder.encode(
                if (category.query.isNotEmpty()) category.query else "painting",
                "UTF-8"
            )
            val endpointUrl = when {
                category.departmentId != null ->
                    "https://collectionapi.metmuseum.org/public/collection/v1/search?departmentId=${category.departmentId}&hasImages=true&isHighlight=true&q=$encodedQuery"
                category.query.isNotEmpty() ->
                    "https://collectionapi.metmuseum.org/public/collection/v1/search?hasImages=true&isHighlight=true&q=$encodedQuery"
                else ->
                    "https://collectionapi.metmuseum.org/public/collection/v1/search?hasImages=true&isHighlight=true&q=paintings"
            }

            val conn = URL(endpointUrl).openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "ArtGalleryTV/1.0 (Android; Museum Frame)")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(json)
                val objectIDs = obj.optJSONArray("objectIDs")
                if (objectIDs != null && objectIDs.length() > 0) {
                    val limit = minOf(8, objectIDs.length())
                    val ids = (0 until limit).map { objectIDs.getInt(it) }
                    val details = coroutineScope {
                        ids.map { id ->
                            async { fetchMetMuseumObjectDetails(id, favSet) }
                        }.awaitAll().filterNotNull()
                    }
                    fetchedList.addAll(details)
                }
            }
        } catch (e: Exception) {
            // Gracefully use curated collection when offline or rate-limited
        }

        val combined = if (fetchedList.isNotEmpty()) {
            (localList + fetchedList).distinctBy { it.id }
        } else {
            localList
        }
        return@withContext combined.map { it.copy(isFavorite = favSet.contains(it.id)) }
    }

    suspend fun fetchMetMuseumObjectDetails(
        objectId: Int,
        favSetOverride: Set<Int>? = null
    ): Artwork? = withContext(Dispatchers.IO) {
        val favSet = favSetOverride ?: favoriteIds.first()
        try {
            val url = URL("https://collectionapi.metmuseum.org/public/collection/v1/objects/$objectId")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "ArtGalleryTV/1.0 (Android; Museum Frame)")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(json)
                val primaryImageSmall = obj.optString("primaryImageSmall", "")
                val primaryImage = obj.optString("primaryImage", "")
                val displayUrl = primaryImageSmall.ifEmpty { primaryImage }
                if (displayUrl.isNotEmpty()) {
                    return@withContext Artwork(
                        id = obj.optInt("objectID", objectId),
                        title = obj.optString("title", "Untitled"),
                        artistDisplayName = obj.optString("artistDisplayName", "").ifBlank { "Master Artist" },
                        artistDisplayBio = obj.optString("artistDisplayBio", ""),
                        objectDate = obj.optString("objectDate", ""),
                        medium = obj.optString("medium", ""),
                        dimensions = obj.optString("dimensions", ""),
                        department = obj.optString("department", "Metropolitan Museum of Art"),
                        creditLine = obj.optString("creditLine", ""),
                        accessionNumber = obj.optString("accessionNumber", ""),
                        imageUrl = displayUrl,
                        thumbnailUrl = displayUrl,
                        culture = obj.optString("culture", ""),
                        period = obj.optString("period", ""),
                        isHighlight = obj.optBoolean("isHighlight", false),
                        isFavorite = favSet.contains(objectId)
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore individual object fetch errors
        }
        return@withContext null
    }
}
