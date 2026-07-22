package example.relay.source

import dev.relay.music.source.api.RelaySource
import dev.relay.music.source.api.RelaySourceApi
import dev.relay.music.source.api.RelaySourceFactory
import dev.relay.music.source.api.RelaySourcePage
import dev.relay.music.source.api.RelaySourceTrack

/**
 * A small Mihon-style source: Relay instantiates this class from the installed APK and invokes
 * it directly. Real sources use a documented provider API or parse the provider's accessible pages here.
 */
class DemoRelaySourceFactory : RelaySourceFactory {
    override fun getApiVersion() = RelaySourceApi.VERSION

    override fun createSources(): List<RelaySource> = listOf(DemoRelaySource())
}

private class DemoRelaySource : RelaySource {
    override fun getId() = "relay-demo"
    override fun getName() = "Relay demo music"

    override fun search(query: String): RelaySourcePage {
        val (field, term) = when {
            query.startsWith("title:", ignoreCase = true) -> "title" to query.substringAfter(':')
            query.startsWith("artist:", ignoreCase = true) -> "artist" to query.substringAfter(':')
            query.startsWith("album:", ignoreCase = true) -> "album" to query.substringAfter(':')
            else -> "all" to query
        }
        val matches = demoTracks.filter { track ->
            term.isBlank() || when (field) {
                "title" -> track.title.contains(term, ignoreCase = true)
                "artist" -> track.artist.contains(term, ignoreCase = true)
                "album" -> track.album.orEmpty().contains(term, ignoreCase = true)
                else -> listOf(track.title, track.artist, track.album.orEmpty()).any { it.contains(term, ignoreCase = true) }
            }
        }
        return RelaySourcePage(matches)
    }
}

private val demoTracks = listOf(
    RelaySourceTrack("signal-test", "https://download.samplelib.com/mp3/sample-3s.mp3", "Signal Test", "Relay Demo", "Source API Samples", 3_000, null),
    RelaySourceTrack("night-transfer", "https://download.samplelib.com/mp3/sample-6s.mp3", "Night Transfer", "Relay Demo", "Source API Samples", 6_000, null),
    RelaySourceTrack("wideband", "https://download.samplelib.com/mp3/sample-9s.mp3", "Wideband", "Relay Demo", "Source API Samples", 9_000, null),
)
