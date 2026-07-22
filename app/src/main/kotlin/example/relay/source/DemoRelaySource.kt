package example.relay.source

import dev.relay.music.source.api.RelaySource
import dev.relay.music.source.api.RelaySourceApi
import dev.relay.music.source.api.RelaySourceFactory
import dev.relay.music.source.api.RelaySourcePage
import dev.relay.music.source.api.RelaySourceTrack

/**
 * A small Mihon-style source: Relay instantiates this class from the installed APK and invokes
 * it directly. Real sources make their own authenticated API requests or parse their sites here.
 */
class DemoRelaySourceFactory : RelaySourceFactory {
    override fun getApiVersion() = RelaySourceApi.VERSION

    override fun createSources(): List<RelaySource> = listOf(DemoRelaySource())
}

private class DemoRelaySource : RelaySource {
    override fun getId() = "relay-demo"
    override fun getName() = "Relay demo music"

    override fun search(query: String): RelaySourcePage {
        val matches = demoTracks.filter { track ->
            query.isBlank() || listOf(track.title, track.artist, track.album.orEmpty())
                .any { it.contains(query, ignoreCase = true) }
        }
        return RelaySourcePage(matches)
    }
}

private val demoTracks = listOf(
    RelaySourceTrack("signal-test", "https://download.samplelib.com/mp3/sample-3s.mp3", "Signal Test", "Relay Demo", "Source API Samples", 3_000, null),
    RelaySourceTrack("night-transfer", "https://download.samplelib.com/mp3/sample-6s.mp3", "Night Transfer", "Relay Demo", "Source API Samples", 6_000, null),
    RelaySourceTrack("wideband", "https://download.samplelib.com/mp3/sample-9s.mp3", "Wideband", "Relay Demo", "Source API Samples", 9_000, null),
)
