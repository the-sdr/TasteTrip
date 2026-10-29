package nz.tastetrip.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTest {
    private val engine = Engine()

    // Someone who loves thrills and hates slow, cultural things.
    private val thrillSeeker = mapOf(
        "akl-skyjump" to MyRating(5, "the adrenaline"),
        "akl-kayak" to MyRating(5),
        "rot-mtb" to MyRating(5),
        "rot-zorb" to MyRating(5),
        "akl-museum" to MyRating(1, "boring museum"),
        "rot-polynesian" to MyRating(1),
    )

    @Test
    fun profileLearnsLikesAndDislikes() {
        val p = engine.profile(thrillSeeker)
        assertTrue(p.getValue(Trait.Adventure) > 0.4)
        assertTrue(p.getValue(Trait.Culture) < 0)
        assertTrue(p.getValue(Trait.Relaxing) < 0)
    }

    @Test
    fun twinsShareTheTaste() {
        val top = engine.twins(thrillSeeker, 3)
        assertEquals(3, top.size)
        top.forEach { tw -> assertTrue(engine.profile(tw.reviewer.ratings.mapValues { MyRating(it.value) }).getValue(Trait.Adventure) > 0) }
    }

    @Test
    fun recommendationsPutThrillsFirstInQueenstown() {
        val recs = engine.recommendations("Queenstown", thrillSeeker)
        assertTrue(recs.take(3).any { it.experience.id == "zqn-bungy" || it.experience.id == "zqn-jetboat" })
    }

    @Test
    fun itineraryRespectsPaceDietAndSkipsRated() {
        val days = engine.itinerary(TripRequest("Auckland", 3, Pace.Relaxed, vegan = false, glutenFree = true), thrillSeeker)
        assertTrue(days.isNotEmpty())
        val all = days.flatMap { it.stops }
        assertTrue(days.all { d -> d.stops.size <= Pace.Relaxed.maxStops && d.stops.sumOf { it.experience.hours } <= Pace.Relaxed.hours })
        assertTrue(all.none { it.experience.id in thrillSeeker })
        assertTrue(all.none { it.experience.isFood && !it.experience.glutenFree })
        assertEquals(all.size, all.map { it.experience.id }.toSet().size)
    }
}
