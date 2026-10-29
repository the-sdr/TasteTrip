package nz.tastetrip.app

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Saved(
    val ratings: Map<String, MyRating> = emptyMap(),
    val seenWelcome: Boolean = false,
    val city: String = "Queenstown",
    val days: Int = 3,
    val pace: String = Pace.Balanced.name,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
)

/** Everything the prototype remembers, in one JSON file on the phone. Nothing leaves the device. */
class Store(context: Context) {
    private val file = File(context.filesDir, "tastetrip.json")
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): Saved = runCatching { json.decodeFromString<Saved>(file.readText()) }.getOrDefault(Saved())

    fun save(s: Saved) {
        file.writeText(json.encodeToString(s))
    }
}
