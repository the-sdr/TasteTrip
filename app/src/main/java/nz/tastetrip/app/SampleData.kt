package nz.tastetrip.app

import kotlin.random.Random

/** The taste traits a profile is made of. Every experience is tagged with how much of each it offers (0..1). */
enum class Trait(val label: String, val keywords: List<String>) {
    Nature("Nature", listOf("nature", "bush", "forest", "walk", "wildlife", "birds", "trees")),
    Adventure("Adventure", listOf("adrenaline", "thrill", "scary", "jump", "fast", "adventure", "exciting")),
    Culture("Culture & history", listOf("history", "culture", "museum", "māori", "maori", "art", "learn")),
    Food("Food & drink", listOf("food", "eat", "delicious", "wine", "coffee", "café", "cafe", "beer", "tasty")),
    Nightlife("Nightlife", listOf("night", "bar", "music", "party", "late", "gig")),
    Relaxing("Relaxing", listOf("relax", "chill", "calm", "slow", "peaceful", "spa", "quiet")),
    Scenery("Views", listOf("view", "views", "scenery", "scenic", "beautiful", "stunning", "photos")),
    Water("On the water", listOf("water", "swim", "beach", "boat", "lake", "kayak", "sea")),
    Active("Active", listOf("hike", "climb", "bike", "cycle", "steep", "exercise", "active", "tiring")),
    Quirky("Quirky", listOf("weird", "quirky", "unusual", "odd", "fun", "different")),
}

enum class Slot(val label: String) { Morning("Morning"), Afternoon("Afternoon"), Evening("Evening"), Any("Any time") }

data class Experience(
    val id: String,
    val city: String,
    val area: String,
    val name: String,
    val blurb: String,
    val tags: Map<Trait, Double>,
    val hours: Double,
    val slot: Slot = Slot.Any,
    val cost: String = "$",
    /** Only meaningful for food experiences: whether there are clearly labelled options. */
    val vegan: Boolean = true,
    val glutenFree: Boolean = true,
) {
    val isFood: Boolean get() = (tags[Trait.Food] ?: 0.0) >= 0.6
}

data class Reviewer(val id: Int, val name: String, val home: String, val ratings: Map<String, Int>, val notes: Map<String, String>)

/**
 * Sample data for the prototype. Place names are real; the descriptions are short and general, and every
 * reviewer and review is made up. The real app would pull places and reviews from a licensed source.
 */
object SampleData {
    private fun t(vararg pairs: Pair<Trait, Double>) = pairs.toMap()
    private val N = Trait.Nature; private val A = Trait.Adventure; private val C = Trait.Culture; private val F = Trait.Food
    private val NL = Trait.Nightlife; private val R = Trait.Relaxing; private val S = Trait.Scenery; private val W = Trait.Water
    private val AC = Trait.Active; private val Q = Trait.Quirky

    val cities = listOf("Auckland", "Rotorua", "Queenstown", "Wellington")

    val experiences: List<Experience> = listOf(
        // Auckland
        Experience("akl-skytower", "Auckland", "City centre", "Sky Tower lookout", "The view over the harbour and volcanic cones from 220 m up.", t(S to 1.0, Q to 0.3), 1.5, cost = "$$"),
        Experience("akl-skyjump", "Auckland", "City centre", "SkyJump off the Sky Tower", "A guided, cable-controlled jump from 192 m.", t(A to 1.0, Q to 0.4, S to 0.4), 1.5, cost = "$$$"),
        Experience("akl-museum", "Auckland", "Parnell", "Auckland War Memorial Museum", "Pacific and Māori collections, natural history and a cultural performance.", t(C to 1.0), 3.0, Slot.Morning, "$$"),
        Experience("akl-rangitoto", "Auckland", "Harbour", "Rangitoto Island summit walk", "Ferry out, then walk lava fields and pōhutukawa forest to the crater rim.", t(N to 0.9, AC to 0.7, S to 0.8, W to 0.4), 5.0, Slot.Morning),
        Experience("akl-waiheke", "Auckland", "Harbour", "Waiheke Island wine and beaches", "Ferry to vineyards, beaches and a slow lunch.", t(F to 0.8, R to 0.7, W to 0.6, S to 0.6), 6.0, Slot.Morning, "$$$", vegan = true, glutenFree = true),
        Experience("akl-piha", "Auckland", "West Coast", "Piha black-sand beach and Lion Rock", "Wild west-coast surf beach, rugged and windy.", t(N to 0.8, W to 0.8, S to 0.9), 4.0, Slot.Afternoon, "Free"),
        Experience("akl-waitakere", "Auckland", "West Coast", "Waitākere Ranges waterfall walk", "Rainforest tracks to Kitekite Falls.", t(N to 1.0, AC to 0.6, R to 0.4), 3.0, Slot.Morning, "Free"),
        Experience("akl-ponsonby", "Auckland", "Ponsonby", "Ponsonby Road vegan café crawl", "A few plant-based cafés and bakeries in a walkable strip.", t(F to 1.0, R to 0.3), 2.5, Slot.Morning, "$$", vegan = true, glutenFree = true),
        Experience("akl-britomart", "Auckland", "City centre", "Britomart and Viaduct bars", "Cocktail bars and late spots by the waterfront.", t(NL to 1.0, F to 0.5), 3.0, Slot.Evening, "$$", vegan = true, glutenFree = false),
        Experience("akl-kayak", "Auckland", "Harbour", "Sunset kayak to Rangitoto", "Guided paddle across the channel, back by headlamp.", t(W to 1.0, A to 0.6, AC to 0.7, S to 0.7), 4.0, Slot.Evening, "$$$"),
        Experience("akl-nightmarket", "Auckland", "Ponsonby", "Night market street food", "Stalls from around the world, loud and busy.", t(F to 0.9, NL to 0.5, Q to 0.4), 2.0, Slot.Evening, "$", vegan = true, glutenFree = true),

        // Rotorua
        Experience("rot-tepuia", "Rotorua", "Te Puia", "Te Puia geysers and carving school", "Pōhutu geyser, mud pools and Māori arts and crafts institute.", t(C to 0.9, N to 0.6, Q to 0.4), 3.0, Slot.Morning, "$$"),
        Experience("rot-hangi", "Rotorua", "Te Puia", "Evening hāngī and cultural performance", "A Māori welcome, kapa haka and a meal cooked in the ground.", t(C to 1.0, F to 0.7), 3.5, Slot.Evening, "$$$", vegan = false, glutenFree = true),
        Experience("rot-redwoods", "Rotorua", "Whakarewarewa Forest", "Redwoods Treewalk", "Suspension bridges between giant redwoods, lit with lanterns at night.", t(N to 0.9, Q to 0.5, R to 0.4, S to 0.5), 1.5, cost = "$$"),
        Experience("rot-mtb", "Rotorua", "Whakarewarewa Forest", "Mountain biking the redwoods trails", "Graded trails for every level, bikes for hire.", t(AC to 1.0, A to 0.7, N to 0.7), 3.0, Slot.Morning, "$$"),
        Experience("rot-luge", "Rotorua", "Skyline", "Skyline gondola and luge", "Gondola up, then race down on a gravity cart.", t(A to 0.7, Q to 0.6, S to 0.7), 2.0, Slot.Afternoon, "$$"),
        Experience("rot-zorb", "Rotorua", "Skyline", "Zorbing down a hill", "Roll downhill inside a giant inflatable ball with water in it.", t(A to 0.8, Q to 1.0, W to 0.3), 1.0, Slot.Afternoon, "$$"),
        Experience("rot-polynesian", "Rotorua", "Lakefront", "Polynesian Spa hot pools", "Lakeside mineral pools, best at dusk.", t(R to 1.0, W to 0.6, S to 0.4), 2.0, Slot.Evening, "$$"),
        Experience("rot-waiotapu", "Rotorua", "Wai-O-Tapu", "Wai-O-Tapu thermal wonderland", "Champagne Pool's colours and the morning geyser.", t(N to 0.9, S to 0.9, Q to 0.5), 2.5, Slot.Morning, "$$"),
        Experience("rot-eatstreet", "Rotorua", "Lakefront", "Eat Streat dinner", "A covered street of restaurants, warmed by geothermal heat.", t(F to 0.9, NL to 0.4), 2.0, Slot.Evening, "$$", vegan = true, glutenFree = true),
        Experience("rot-kayak", "Rotorua", "Lakefront", "Lake Rotoiti hot-pool kayak", "Paddle to bush hot pools you can only reach by water.", t(W to 1.0, N to 0.7, R to 0.5, AC to 0.4), 4.0, Slot.Morning, "$$$"),

        // Queenstown
        Experience("zqn-bungy", "Queenstown", "Kawarau", "Kawarau Bridge bungy", "The original commercial bungy, 43 m over the river.", t(A to 1.0, Q to 0.4), 2.0, Slot.Afternoon, "$$$"),
        Experience("zqn-jetboat", "Queenstown", "Kawarau", "Shotover jet boat", "High-speed spins through a narrow canyon.", t(A to 0.9, W to 0.8), 1.5, cost = "$$$"),
        Experience("zqn-gondola", "Queenstown", "Town", "Skyline gondola at sunset", "Views over Lake Wakatipu and the Remarkables.", t(S to 1.0, R to 0.4), 2.0, Slot.Evening, "$$"),
        Experience("zqn-hill", "Queenstown", "Town", "Queenstown Hill walk", "A steep climb to the Basket of Dreams and a big view.", t(AC to 0.9, S to 0.9, N to 0.6), 3.0, Slot.Morning, "Free"),
        Experience("zqn-milford", "Queenstown", "Fiordland", "Milford Sound day cruise", "A long coach day to the fiord, cruise past waterfalls and seals.", t(N to 1.0, S to 1.0, W to 0.7), 12.0, Slot.Morning, "$$$"),
        Experience("zqn-gibbston", "Queenstown", "Gibbston", "Gibbston Valley wineries by bike", "Cycle between cellar doors on a flat trail.", t(F to 0.8, AC to 0.5, R to 0.5, S to 0.6), 5.0, Slot.Morning, "$$$", vegan = true, glutenFree = true),
        Experience("zqn-onsen", "Queenstown", "Arthurs Point", "Onsen hot tubs over the Shotover", "Private cedar hot tubs looking down the canyon.", t(R to 1.0, S to 0.8), 1.5, Slot.Evening, "$$$"),
        Experience("zqn-bars", "Queenstown", "Town", "Queenstown pub and bar night", "Small town with a big late-night scene.", t(NL to 1.0, F to 0.3), 3.0, Slot.Evening, "$$", vegan = true, glutenFree = false),
        Experience("zqn-glenorchy", "Queenstown", "Glenorchy", "Glenorchy drive and lagoon walk", "Lakeside road to a quiet village and a boardwalk walk.", t(N to 0.9, S to 1.0, R to 0.7), 4.0, Slot.Afternoon, "Free"),
        Experience("zqn-arrowtown", "Queenstown", "Arrowtown", "Arrowtown gold-rush village", "Old main street, Chinese miners' settlement and autumn colours.", t(C to 0.9, R to 0.5, F to 0.3), 3.0, Slot.Afternoon, "Free"),

        // Wellington
        Experience("wlg-tepapa", "Wellington", "Waterfront", "Te Papa museum", "The national museum: nature, Māori and Pacific taonga, and history. Free entry.", t(C to 1.0, Q to 0.3), 3.0, Slot.Morning, "Free"),
        Experience("wlg-cablecar", "Wellington", "Kelburn", "Cable car and Botanic Garden", "Ride up, then walk down through the gardens.", t(S to 0.7, N to 0.6, R to 0.6), 2.0, cost = "$"),
        Experience("wlg-zealandia", "Wellington", "Karori", "Zealandia night tour", "A fenced sanctuary: spot kiwi, tuatara and glow-worms by torchlight.", t(N to 1.0, Q to 0.5), 2.5, Slot.Evening, "$$$"),
        Experience("wlg-cuba", "Wellington", "Cuba Street", "Cuba Street cafés and vintage shops", "Bohemian strip with good coffee and lots of plant-based food.", t(F to 0.8, Q to 0.7, R to 0.3), 3.0, Slot.Afternoon, "$", vegan = true, glutenFree = true),
        Experience("wlg-craftbeer", "Wellington", "Cuba Street", "Craft beer bar crawl", "Small breweries and taprooms close together.", t(NL to 0.9, F to 0.6), 3.0, Slot.Evening, "$$", vegan = true, glutenFree = false),
        Experience("wlg-mtvic", "Wellington", "Mount Victoria", "Mount Victoria lookout walk", "Bush tracks to a 360° view of the harbour.", t(S to 0.9, AC to 0.6, N to 0.5), 2.0, Slot.Morning, "Free"),
        Experience("wlg-weta", "Wellington", "Miramar", "Wētā Workshop tour", "Behind the scenes of the film effects studio.", t(Q to 0.9, C to 0.6), 2.0, Slot.Afternoon, "$$"),
        Experience("wlg-sealcoast", "Wellington", "South Coast", "Red Rocks seal colony walk", "Rugged coastal walk to a winter seal haul-out.", t(N to 0.9, W to 0.5, AC to 0.6, S to 0.7), 4.0, Slot.Morning, "Free"),
        Experience("wlg-parliament", "Wellington", "Waterfront", "Parliament and the Beehive tour", "Free guided tour of how New Zealand's government works.", t(C to 0.8), 1.5, Slot.Afternoon, "Free"),
        Experience("wlg-oriental", "Wellington", "Waterfront", "Oriental Bay swim and waterfront stroll", "City beach and promenade, packed on a still day.", t(W to 0.8, R to 0.8, S to 0.5), 2.0, Slot.Afternoon, "Free"),
    )

    val byId: Map<String, Experience> = experiences.associateBy { it.id }

    /** Made-up travellers with different tastes, so the taste-twins matching has something to match against. */
    val reviewers: List<Reviewer> by lazy { generateReviewers() }

    private val names = listOf(
        "Aroha", "Ben", "Chloe", "Dev", "Ella", "Finn", "Grace", "Hemi", "Isla", "Jack", "Kiri", "Liam", "Maya", "Nikau",
        "Olivia", "Priya", "Quinn", "Rangi", "Sofia", "Tama", "Uma", "Vikram", "Wiremu", "Xin", "Yasmin", "Zac", "Anahera",
        "Bruno", "Carmen", "Dmitri", "Emi", "Felix", "Hana", "Ivan", "Jade", "Kai", "Lucia", "Mateo", "Noa", "Oscar",
        "Pania", "Ravi", "Sione", "Tessa", "Wei", "Amelie", "Lars", "Ngaio", "Ruby", "Tomás",
    )
    private val homes = listOf("Melbourne", "Christchurch", "London", "Toronto", "Dunedin", "Berlin", "Tokyo", "Sydney", "Denver", "Hamilton")

    /** Archetypes the made-up reviewers are drawn from (trait -> how much they care, -1..1). */
    private val archetypes: List<Map<Trait, Double>> = listOf(
        t(A to 1.0, AC to 0.8, W to 0.5, R to -0.5, C to -0.3),             // thrill seeker
        t(N to 1.0, S to 0.8, AC to 0.5, NL to -0.7, Q to -0.2),            // nature lover
        t(F to 1.0, NL to 0.4, R to 0.4, AC to -0.4),                       // foodie
        t(C to 1.0, Q to 0.4, A to -0.6, NL to -0.3),                       // culture buff
        t(R to 1.0, S to 0.7, W to 0.4, A to -0.8, AC to -0.5),             // slow traveller
        t(NL to 1.0, F to 0.6, Q to 0.5, N to -0.4),                        // night owl
        t(Q to 1.0, A to 0.4, C to 0.4, R to -0.2),                         // novelty hunter
    )

    private fun generateReviewers(): List<Reviewer> {
        val rnd = Random(42)
        return names.mapIndexed { i, name ->
            val base = archetypes[i % archetypes.size]
            val mix = archetypes[rnd.nextInt(archetypes.size)]
            val taste = Trait.entries.associateWith { (base[it] ?: 0.0) * 0.8 + (mix[it] ?: 0.0) * 0.3 + (rnd.nextDouble() - 0.5) * 0.3 }
            val ratings = mutableMapOf<String, Int>()
            val notes = mutableMapOf<String, String>()
            experiences.filter { rnd.nextDouble() < 0.55 }.forEach { e ->
                val fit = e.tags.entries.sumOf { (tr, w) -> w * (taste[tr] ?: 0.0) } + (rnd.nextDouble() - 0.5) * 0.6
                val stars = when {
                    fit > 0.9 -> 5; fit > 0.35 -> 4; fit > -0.1 -> 3; fit > -0.5 -> 2; else -> 1
                }
                ratings[e.id] = stars
                notes[e.id] = noteFor(e, stars, taste, rnd)
            }
            Reviewer(i, name, homes[rnd.nextInt(homes.size)], ratings, notes)
        }
    }

    private fun noteFor(e: Experience, stars: Int, taste: Map<Trait, Double>, rnd: Random): String {
        val top = e.tags.maxBy { it.value * (taste[it.key] ?: 0.0) }.key
        val liked = listOf(
            "Exactly my kind of thing. Great for %s.", "Loved it, would go back tomorrow.", "Highlight of the trip for me. Big on %s.",
            "So good. If you like %s, don't skip it.",
        )
        val meh = listOf("It was fine. Glad I went, wouldn't rush back.", "Okay, a bit overrated for me.", "Decent but not really my thing.")
        val no = listOf("Not for me at all.", "Wished I'd spent the time elsewhere.", "Not my thing. I'm not big on %s.")
        val pool = when { stars >= 4 -> liked; stars == 3 -> meh; else -> no }
        return pool[rnd.nextInt(pool.size)].replace("%s", top.label.lowercase())
    }
}
