package example.relay.source

import dev.relay.music.source.api.BaseRelaySource
import dev.relay.music.source.api.RelaySource
import dev.relay.music.source.api.RelaySourceApi
import dev.relay.music.source.api.RelaySourceFactory
import dev.relay.music.source.api.RelaySourceListing
import dev.relay.music.source.api.RelaySourcePage
import dev.relay.music.source.api.RelaySourceSetting
import dev.relay.music.source.api.RelaySourceTrack

/**
 * A small Mihon-style source: Relay instantiates this class from the installed APK and invokes
 * it directly. Real sources use a documented provider API or parse the provider's accessible pages here.
 */
class DemoRelaySourceFactory : RelaySourceFactory {
    override fun getApiVersion() = RelaySourceApi.VERSION

    override fun createSources(): List<RelaySource> = listOf(DemoRelaySource())
}

private class DemoRelaySource : BaseRelaySource() {
    override fun getId() = "relay-demo"
    override fun getName() = "Relay demo music"

    /** Browse shelves Relay shows before the user types a query. */
    override fun getListings() = listOf(RelaySourceListing("samples", "Samples"))

    override fun browse(listingId: String, page: Int): RelaySourcePage {
        require(listingId == "samples") { "Unknown listing." }
        return RelaySourcePage(if (page == 1) visibleTracks() else emptyList(), false)
    }

    override fun search(query: String, page: Int): RelaySourcePage {
        if (page > 1) return RelaySourcePage(emptyList(), false)
        val (field, term) = when {
            query.startsWith("title:", ignoreCase = true) -> "title" to query.substringAfter(':')
            query.startsWith("artist:", ignoreCase = true) -> "artist" to query.substringAfter(':')
            query.startsWith("album:", ignoreCase = true) -> "album" to query.substringAfter(':')
            else -> "all" to query
        }
        val matches = visibleTracks().filter { track ->
            term.isBlank() || when (field) {
                "title" -> track.title.contains(term, ignoreCase = true)
                "artist" -> track.artist.contains(term, ignoreCase = true)
                "album" -> track.album.orEmpty().contains(term, ignoreCase = true)
                else -> listOf(track.title, track.artist, track.album.orEmpty()).any { it.contains(term, ignoreCase = true) }
            }
        }
        return RelaySourcePage(matches)
    }

    /**
     * Search results above return no stream URL, so Relay calls this just before playback.
     * A source whose stream URLs are free to include at search time may skip this and set
     * `streamUrl` on each track instead — lazy resolution exists for scraped or short-lived URLs.
     */
    override fun resolveStreamUrl(trackId: String): String? = demoStreams[trackId]

    /** Headers Relay attaches to this source's media requests (allow-listed by the host). */
    override fun getMediaRequestHeaders() = mapOf("User-Agent" to "RelayDemoSource/0.2")

    @Volatile private var onlyShort = false

    private fun visibleTracks() = if (onlyShort) demoTracks.filter { (it.durationMs ?: 0) <= 6_000 } else demoTracks

    /** Preferences Relay renders in its own settings UI and hands back via applySettings. */
    override fun getSettings() = listOf(
        RelaySourceSetting("only-short", "Only short samples", RelaySourceSetting.Type.TOGGLE, "false"),
    )

    override fun applySettings(values: Map<String, String>) {
        onlyShort = values["only-short"] == "true"
    }
}

private val demoStreams = mapOf(
    "signal-test" to "https://download.samplelib.com/mp3/sample-3s.mp3",
    "night-transfer" to "https://download.samplelib.com/mp3/sample-6s.mp3",
    "wideband" to "https://download.samplelib.com/mp3/sample-9s.mp3",
)

private val demoTracks = listOf(
    RelaySourceTrack("signal-test", null, "Signal Test", "Relay Demo", "Source API Samples", 3_000, null),
    RelaySourceTrack("night-transfer", null, "Night Transfer", "Relay Demo", "Source API Samples", 6_000, null),
    RelaySourceTrack("wideband", null, "Wideband", "Relay Demo", "Source API Samples", 9_000, null),
)
