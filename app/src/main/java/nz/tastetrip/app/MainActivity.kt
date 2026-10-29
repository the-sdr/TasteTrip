package nz.tastetrip.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Teal = Color(0xFF0F766E)
private val Coral = Color(0xFFE76F51)
private val Sand = Color(0xFFFAF7F2)

private val scheme = lightColorScheme(
    primary = Teal, onPrimary = Color.White, secondary = Coral, background = Sand, surface = Color.White,
    primaryContainer = Color(0xFFCCFBF1), onPrimaryContainer = Color(0xFF134E4A),
    // Selected chips and the nav indicator use the secondary container.
    secondaryContainer = Color(0xFFCCFBF1), onSecondaryContainer = Color(0xFF134E4A),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = Store(this)
        setContent { MaterialTheme(colorScheme = scheme) { App(store) } }
    }
}

private enum class Tab(val label: String, val icon: String) { Rate("Rate", "★"), Taste("My taste", "◉"), Trip("Trip", "➜") }

private const val ENOUGH = 5

@Composable
private fun App(store: Store) {
    var saved by remember { mutableStateOf(store.load()) }
    fun update(f: (Saved) -> Saved) { saved = f(saved); store.save(saved) }
    val engine = remember { Engine() }
    var tab by rememberSaveable { mutableStateOf(Tab.Rate) }

    if (!saved.seenWelcome) {
        Welcome { update { it.copy(seenWelcome = true) } }
        return
    }

    Scaffold(
        containerColor = Sand,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t, onClick = { tab = t },
                        icon = { Text(t.icon, fontSize = 20.sp) }, label = { Text(t.label) },
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                Tab.Rate -> RateScreen(saved, ::update) { tab = Tab.Taste }
                Tab.Taste -> TasteScreen(saved, engine) { tab = Tab.Trip }
                Tab.Trip -> TripScreen(saved, engine, ::update) { tab = Tab.Rate }
            }
        }
    }
}

@Composable
private fun Welcome(onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Teal).systemBarsPadding().padding(28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("TasteTrip", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("Trips matched to your taste.", color = Color.White, fontSize = 20.sp)
        Spacer(Modifier.height(28.dp))
        listOf(
            "1. Rate a few things you've already done.",
            "2. TasteTrip learns what you like, and finds travellers who like the same things.",
            "3. It plans your next trip from what they loved.",
        ).forEach { Text(it, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp)) }
        Spacer(Modifier.height(28.dp))
        Text(
            "Prototype: four New Zealand cities with sample data. Place names are real; the travellers and their reviews are made up. Everything stays on your phone.",
            color = Color(0xFFCCFBF1), fontSize = 13.sp,
        )
        Spacer(Modifier.height(28.dp))
        Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Teal)) {
            Text("Get started", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Header(title: String, sub: String? = null) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
        if (sub != null) Text(sub, fontSize = 14.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun CityChips(selected: String, onPick: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleData.cities.forEach { c -> FilterChip(selected = c == selected, onClick = { onPick(c) }, label = { Text(c) }) }
    }
}

// ---------- Rate ----------

@Composable
private fun RateScreen(saved: Saved, update: ((Saved) -> Saved) -> Unit, onDone: () -> Unit) {
    var city by rememberSaveable { mutableStateOf("Auckland") }
    val count = saved.ratings.size
    Column(Modifier.fillMaxSize()) {
        Header("What have you done?", "Pick a place you've been and rate what you did there. Skip anything you haven't done.")
        CityChips(city) { city = it }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(SampleData.experiences.filter { it.city == city }, key = { it.id }) { e ->
                RateCard(e, saved.ratings[e.id]) { r ->
                    update { s -> s.copy(ratings = if (r == null) s.ratings - e.id else s.ratings + (e.id to r)) }
                }
            }
        }
        Surface(color = Color.White, shadowElevation = 6.dp) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (count >= ENOUGH) "$count rated. Enough to get started." else "$count of $ENOUGH rated",
                    modifier = Modifier.weight(1f), color = Color(0xFF374151),
                )
                Button(onClick = onDone, enabled = count >= ENOUGH) { Text("See my taste") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RateCard(e: Experience, current: MyRating?, onRate: (MyRating?) -> Unit) {
    var why by remember(e.id) { mutableStateOf(current?.why ?: "") }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(e.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(e.blurb, fontSize = 13.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5 to "Loved it", 3 to "It was fine", 1 to "Didn't like it").forEach { (stars, label) ->
                    val on = current?.stars == stars
                    FilterChip(
                        selected = on,
                        onClick = { onRate(if (on) null else MyRating(stars, why)) },
                        label = { Text(label, fontSize = 13.sp) },
                    )
                }
            }
            if (current != null) {
                OutlinedTextField(
                    value = why,
                    onValueChange = { why = it; onRate(current.copy(why = it)) },
                    label = { Text(if (current.stars >= 4) "What did you love? (optional)" else "Why? (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
    }
}

// ---------- Taste ----------

@Composable
private fun TasteScreen(saved: Saved, engine: Engine, onPlan: () -> Unit) {
    val profile = remember(saved.ratings) { engine.profile(saved.ratings) }
    val twins = remember(saved.ratings) { engine.twins(saved.ratings, 5) }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Header("Your travel taste", "Learned from ${saved.ratings.size} ratings. It sharpens every time you rate something.") }
        if (saved.ratings.isEmpty()) {
            item { Text("Rate a few things first.", modifier = Modifier.padding(16.dp)) }
            return@LazyColumn
        }
        item {
            Card(Modifier.padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    profile.entries.sortedByDescending { it.value }.forEach { (trait, v) -> TraitBar(trait.label, v) }
                }
            }
        }
        item {
            Text("Your taste twins", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            Text(
                "Travellers whose ratings line up with yours. Their reviews count most when TasteTrip picks for you.",
                fontSize = 13.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        items(twins, key = { it.reviewer.id }) { tw ->
            Card(Modifier.padding(horizontal = 16.dp, vertical = 5.dp).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)).background(scheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Text(tw.reviewer.name.take(1), fontWeight = FontWeight.Bold, color = Teal)
                    }
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text("${tw.reviewer.name}, ${tw.reviewer.home}", fontWeight = FontWeight.SemiBold)
                        val loves = engine.profile(tw.reviewer.ratings.mapValues { MyRating(it.value) })
                            .entries.sortedByDescending { it.value }.take(2).joinToString(" · ") { it.key.label }
                        Text("Loves $loves · ${tw.shared} places in common", fontSize = 13.sp, color = Color(0xFF6B7280))
                    }
                    Text("${matchPercent(tw.similarity)}%", color = Teal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
        item {
            Button(onClick = onPlan, modifier = Modifier.padding(16.dp).fillMaxWidth()) { Text("Plan a trip") }
        }
    }
}

private fun matchPercent(sim: Double) = ((sim + 1) / 2 * 100).toInt().coerceIn(0, 99)

@Composable
private fun TraitBar(label: String, v: Double) {
    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(130.dp), fontSize = 14.sp)
        // A centred bar: right in teal for likes, left in coral for dislikes.
        Row(Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF1F5F9))) {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                if (v < 0) Box(Modifier.fillMaxWidth((-v).toFloat()).fillMaxHeight().background(Coral))
            }
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
                if (v > 0) Box(Modifier.fillMaxWidth(v.toFloat()).fillMaxHeight().background(Teal))
            }
        }
    }
}

// ---------- Trip ----------

@Composable
private fun TripScreen(saved: Saved, engine: Engine, update: ((Saved) -> Saved) -> Unit, onRateMore: () -> Unit) {
    val req = TripRequest(saved.city, saved.days, Pace.valueOf(saved.pace), saved.vegan, saved.glutenFree)
    val days = remember(req, saved.ratings) { engine.itinerary(req, saved.ratings) }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Header("Plan a trip", "Picked for your taste from what your taste twins loved.") }
        item { CityChips(saved.city) { c -> update { it.copy(city = c) } } }
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Days", modifier = Modifier.width(70.dp))
                    OutlinedButton(onClick = { update { it.copy(days = (it.days - 1).coerceAtLeast(1)) } }) { Text("−") }
                    Text("${saved.days}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    OutlinedButton(onClick = { update { it.copy(days = (it.days + 1).coerceAtMost(5)) } }) { Text("+") }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Pace", modifier = Modifier.width(70.dp))
                    Pace.entries.forEach { p ->
                        FilterChip(selected = p == req.pace, onClick = { update { it.copy(pace = p.name) } }, label = { Text(p.label) }, modifier = Modifier.padding(end = 6.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Food", modifier = Modifier.width(70.dp))
                    FilterChip(selected = saved.vegan, onClick = { update { it.copy(vegan = !it.vegan) } }, label = { Text("Vegan") }, modifier = Modifier.padding(end = 6.dp))
                    FilterChip(selected = saved.glutenFree, onClick = { update { it.copy(glutenFree = !it.glutenFree) } }, label = { Text("Gluten-free") })
                }
            }
        }
        if (saved.ratings.size < ENOUGH) {
            item {
                Card(Modifier.padding(16.dp), colors = CardDefaults.cardColors(containerColor = scheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Rate at least $ENOUGH things first, so the picks are about you and not a guess.")
                        TextButton(onClick = onRateMore) { Text("Rate things") }
                    }
                }
            }
            return@LazyColumn
        }
        if (days.isEmpty()) item { Text("Nothing left in ${saved.city} that suits you. Try another city.", modifier = Modifier.padding(16.dp)) }
        days.forEach { day ->
            item(key = "day${day.number}") {
                Text(
                    "Day ${day.number} · ${day.area}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Teal,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 6.dp),
                )
            }
            items(day.stops, key = { it.experience.id }) { pick ->
                StopCard(pick, engine.slotFor(day, pick)) { r -> update { s -> s.copy(ratings = s.ratings + (pick.experience.id to r)) } }
            }
        }
        if (days.isNotEmpty()) item {
            Text(
                "Not keen on a stop? Tap \"Not for me\" and TasteTrip learns from it and replans. Been already? Rate it and it's swapped for something new.",
                fontSize = 12.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StopCard(pick: Pick, slot: Slot, onRate: (MyRating) -> Unit) {
    val e = pick.experience
    var open by remember(e.id) { mutableStateOf(false) }
    Card(
        onClick = { open = !open },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(slot.label.uppercase(), fontSize = 11.sp, color = Coral, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${matchPercent((pick.score - 3) / 2)}% match", fontSize = 12.sp, color = Teal, fontWeight = FontWeight.SemiBold)
            }
            Text(e.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
            Text("${fmtHours(e.hours)} · ${e.cost}", fontSize = 12.sp, color = Color(0xFF6B7280))
            pick.reasons.forEach { Text("• $it", fontSize = 13.sp, color = Color(0xFF374151), modifier = Modifier.padding(top = 4.dp)) }
            if (open) {
                Text(e.blurb, fontSize = 13.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(top = 8.dp))
                pick.twinNotes.forEach { (tw, note) ->
                    Text(
                        "\"$note\" — ${tw.reviewer.name}, ${matchPercent(tw.similarity)}% taste match, ${"★".repeat(tw.reviewer.ratings.getValue(e.id))}",
                        fontSize = 13.sp, fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 6.dp),
                    )
                }
                FlowRow(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onRate(MyRating(1, "not for me")) }) { Text("Not for me") }
                    OutlinedButton(onClick = { onRate(MyRating(5)) }) { Text("Been, loved it") }
                    OutlinedButton(onClick = { onRate(MyRating(3)) }) { Text("Been, fine") }
                }
            } else {
                Text("Tap for reviews and options", fontSize = 12.sp, color = Teal, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

private fun fmtHours(h: Double) = if (h >= 8) "Full day" else if (h % 1.0 == 0.0) "${h.toInt()} hr" else "$h hr"
