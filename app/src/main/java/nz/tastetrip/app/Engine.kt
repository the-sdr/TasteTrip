package nz.tastetrip.app

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.sqrt

/** One of the user's own ratings: 5 loved, 3 fine, 1 hated. `why` is the optional "what did you like?" line. */
@Serializable
data class MyRating(val stars: Int, val why: String = "")

enum class Pace(val label: String, val hours: Double, val maxStops: Int) {
    Relaxed("Relaxed", 6.0, 2), Balanced("Balanced", 8.0, 3), Packed("Packed", 11.0, 4),
}

data class TripRequest(val city: String, val days: Int, val pace: Pace, val vegan: Boolean, val glutenFree: Boolean)

data class Twin(val reviewer: Reviewer, val similarity: Double, val shared: Int)

data class Pick(
    val experience: Experience,
    val score: Double,
    val reasons: List<String>,
    /** What the closest taste twins said about it. */
    val twinNotes: List<Pair<Twin, String>>,
)

data class Day(val number: Int, val area: String, val stops: List<Pick>)

/**
 * The matching engine, all on the phone:
 * 1. Taste profile: trait weights learned from the user's ratings and the words in their "why".
 * 2. Taste twins: made-up reviewers whose ratings agree most with the user's.
 * 3. Score = blend of profile fit and what the twins thought, leaning on twins as the user rates more.
 * 4. Itinerary: best picks grouped by area per day, within the pace's hours, in time-of-day order.
 */
class Engine(
    private val experiences: List<Experience> = SampleData.experiences,
    private val reviewers: List<Reviewer> = SampleData.reviewers,
) {
    private val byId = experiences.associateBy { it.id }

    fun profile(ratings: Map<String, MyRating>): Map<Trait, Double> {
        val sums = Trait.entries.associateWith { 0.0 }.toMutableMap()
        val counts = Trait.entries.associateWith { 0.0 }.toMutableMap()
        for ((id, r) in ratings) {
            val e = byId[id] ?: continue
            val feeling = (r.stars - 3) / 2.0 // -1..1
            for ((trait, w) in e.tags) {
                sums[trait] = sums.getValue(trait) + feeling * w
                counts[trait] = counts.getValue(trait) + w
            }
            // Words in the "why" push the traits they name in the direction of the rating.
            val words = r.why.lowercase()
            if (words.isNotBlank()) for (trait in Trait.entries) {
                if (trait.keywords.any { words.contains(it) }) {
                    sums[trait] = sums.getValue(trait) + (if (feeling == 0.0) 0.5 else feeling)
                    counts[trait] = counts.getValue(trait) + 1.0
                }
            }
        }
        return Trait.entries.associateWith { tr ->
            val c = counts.getValue(tr)
            if (c == 0.0) 0.0 else (sums.getValue(tr) / (c + 0.5)).coerceIn(-1.0, 1.0)
        }
    }

    private fun reviewerProfile(r: Reviewer) = profile(r.ratings.mapValues { MyRating(it.value) })

    /** Reviewers ordered by how much their taste agrees with the user's. */
    fun twins(ratings: Map<String, MyRating>, limit: Int = 8): List<Twin> {
        if (ratings.isEmpty()) return emptyList()
        val mine = profile(ratings)
        return reviewers.map { rev ->
            val shared = ratings.keys.filter { it in rev.ratings }
            // Agreement on the same experiences: 1 when identical, 0 when as far apart as possible.
            val agreement = if (shared.isEmpty()) 0.0 else
                shared.sumOf { 1.0 - abs(ratings.getValue(it).stars - rev.ratings.getValue(it)) / 4.0 } / shared.size
            val traitFit = cosine(mine, reviewerProfile(rev))
            // Shared ratings count for more the more of them there are.
            val w = shared.size / (shared.size + 3.0)
            val sim = w * (agreement * 2 - 1) + (1 - w) * traitFit
            Twin(rev, sim, shared.size)
        }.sortedByDescending { it.similarity }.take(limit)
    }

    fun score(e: Experience, mine: Map<Trait, Double>, twins: List<Twin>, ratedCount: Int): Pick {
        val tagSum = e.tags.values.sum().coerceAtLeast(0.01)
        val fit = e.tags.entries.sumOf { (tr, w) -> w * mine.getValue(tr) } / tagSum // -1..1
        val contentStars = 3 + 2 * fit
        val voters = twins.filter { it.similarity > 0 && e.id in it.reviewer.ratings }
        val twinStars = if (voters.isEmpty()) null else
            voters.sumOf { it.similarity * it.reviewer.ratings.getValue(e.id) } / voters.sumOf { it.similarity }
        val twinWeight = if (twinStars == null) 0.0 else (ratedCount / (ratedCount + 6.0)).coerceAtMost(0.6)
        val stars = (1 - twinWeight) * contentStars + twinWeight * (twinStars ?: 0.0)

        val reasons = buildList {
            val liked = e.tags.entries.filter { mine.getValue(it.key) > 0.2 && it.value >= 0.5 }
                .sortedByDescending { mine.getValue(it.key) * it.value }.take(2).map { it.key.label.lowercase() }
            if (liked.isNotEmpty()) add("You rate ${liked.joinToString(" and ")} highly")
            val disliked = e.tags.entries.filter { mine.getValue(it.key) < -0.2 && it.value >= 0.5 }.map { it.key.label.lowercase() }
            if (disliked.isNotEmpty()) add("Heads up: heavy on ${disliked.joinToString(" and ")}, which you tend not to enjoy")
            if (voters.isNotEmpty()) {
                val loved = voters.count { it.reviewer.ratings.getValue(e.id) >= 4 }
                add("$loved of ${voters.size} taste twins who went loved it")
            }
        }
        val notes = voters.sortedByDescending { it.similarity }.take(2).map { it to (it.reviewer.notes[e.id] ?: "") }
        return Pick(e, stars, reasons, notes)
    }

    fun recommendations(city: String, ratings: Map<String, MyRating>, vegan: Boolean = false, glutenFree: Boolean = false): List<Pick> {
        val mine = profile(ratings)
        val twins = twins(ratings)
        return experiences
            .filter { it.city == city && it.id !in ratings }
            .filter { !it.isFood || ((!vegan || it.vegan) && (!glutenFree || it.glutenFree)) }
            .map { score(it, mine, twins, ratings.size) }
            .sortedByDescending { it.score }
    }

    fun itinerary(req: TripRequest, ratings: Map<String, MyRating>): List<Day> {
        val pool = recommendations(req.city, ratings, req.vegan, req.glutenFree).filter { it.score >= 2.6 }.toMutableList()
        val days = mutableListOf<Day>()
        for (n in 1..req.days) {
            if (pool.isEmpty()) break
            val anchor = pool.removeAt(0)
            val stops = mutableListOf(anchor)
            var hours = anchor.experience.hours
            // Fill the day: same area first, then the next best anywhere, while it fits the pace.
            val candidates = pool.sortedWith(compareByDescending<Pick> { it.experience.area == anchor.experience.area }.thenByDescending { it.score })
            for (c in candidates) {
                if (stops.size >= req.pace.maxStops) break
                if (hours + c.experience.hours > req.pace.hours) continue
                if (c.experience.slot != Slot.Any && stops.any { it.experience.slot == c.experience.slot }) continue
                stops += c
                hours += c.experience.hours
                pool.remove(c)
            }
            val ordered = orderByTime(stops)
            days += Day(n, ordered.map { it.experience.area }.distinct().joinToString(" & "), ordered)
        }
        return days
    }

    /** Morning, then afternoon, then evening; flexible stops fill the morning if it's empty, else go mid-day. */
    private fun orderByTime(stops: List<Pick>): List<Pick> {
        fun of(slot: Slot) = stops.filter { it.experience.slot == slot }
        val any = of(Slot.Any)
        val morning = of(Slot.Morning).ifEmpty { any.take(1) }
        return morning + of(Slot.Afternoon) + any.filter { it !in morning } + of(Slot.Evening)
    }

    /** The time of day a stop lands on in a day's order, for display. */
    fun slotFor(day: Day, pick: Pick): Slot {
        if (pick.experience.slot != Slot.Any) return pick.experience.slot
        val i = day.stops.indexOf(pick)
        return if (i == 0) Slot.Morning else Slot.Afternoon
    }

    private fun cosine(a: Map<Trait, Double>, b: Map<Trait, Double>): Double {
        val dot = Trait.entries.sumOf { a.getValue(it) * b.getValue(it) }
        val na = sqrt(Trait.entries.sumOf { a.getValue(it) * a.getValue(it) })
        val nb = sqrt(Trait.entries.sumOf { b.getValue(it) * b.getValue(it) })
        return if (na == 0.0 || nb == 0.0) 0.0 else dot / (na * nb)
    }
}
