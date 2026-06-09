package me.ayra.music.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class MusicNavigator(
    initialStack: List<MainRoute> = listOf(MainRoute.Home),
) {
    private val stack = mutableStateListOf<MainRoute>().apply {
        addAll(initialStack.ifEmpty { listOf(MainRoute.Home) })
    }

    val currentRoute: MainRoute
        get() = stack.last()

    fun navigate(route: MainRoute) {
        stack += route
    }

    fun replace(route: MainRoute) {
        if (stack.isNotEmpty()) {
            stack[stack.lastIndex] = route
        } else {
            stack += route
        }
    }

    fun back() {
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
        }
    }

    fun canGoBack(): Boolean = stack.size > 1

    fun snapshot(): List<MainRoute> = stack.toList()

    companion object {
        val Saver: Saver<MusicNavigator, List<String>> = Saver(
            save = { navigator -> navigator.snapshot().map(MainRoute::toKey) },
            restore = { keys -> MusicNavigator(keys.mapNotNull(String::toMainRoute).ifEmpty { listOf(MainRoute.Home) }) },
        )
    }
}

@Composable
fun rememberMusicNavigator(): MusicNavigator =
    rememberSaveable(saver = MusicNavigator.Saver) { MusicNavigator() }

private fun MainRoute.toKey(): String =
    when (this) {
        MainRoute.Home -> "home"
        MainRoute.Search -> "search"
        is MainRoute.SearchTracks -> "searchTracks:${Uri.encode(query)}"
        is MainRoute.SearchArtists -> "searchArtists:${Uri.encode(query)}"
        is MainRoute.SearchAlbums -> "searchAlbums:${Uri.encode(query)}"
        MainRoute.Settings -> "settings"
        is MainRoute.Album -> "album:$id"
        is MainRoute.Artist -> "artist:$name"
        is MainRoute.Folder -> "folder:$path"
        is MainRoute.Playlist -> "playlist:$id"
        is MainRoute.SelectPlaylistTracks -> "selectPlaylistTracks:${Uri.encode(name)}"
        is MainRoute.AddTracksToPlaylist -> "addTracksToPlaylist:${Uri.encode(id)}"
        is MainRoute.AddToPlaylist -> "addToPlaylist:$trackId"
    }

private fun String.toMainRoute(): MainRoute? =
    when {
        this == "home" -> MainRoute.Home
        this == "search" -> MainRoute.Search
        startsWith("searchTracks:") -> MainRoute.SearchTracks(Uri.decode(removePrefix("searchTracks:")))
        startsWith("searchArtists:") -> MainRoute.SearchArtists(Uri.decode(removePrefix("searchArtists:")))
        startsWith("searchAlbums:") -> MainRoute.SearchAlbums(Uri.decode(removePrefix("searchAlbums:")))
        this == "settings" -> MainRoute.Settings
        startsWith("album:") -> removePrefix("album:").toLongOrNull()?.let(MainRoute::Album)
        startsWith("artist:") -> MainRoute.Artist(removePrefix("artist:"))
        startsWith("folder:") -> MainRoute.Folder(removePrefix("folder:"))
        startsWith("playlist:") -> MainRoute.Playlist(removePrefix("playlist:"))
        startsWith("selectPlaylistTracks:") -> MainRoute.SelectPlaylistTracks(Uri.decode(removePrefix("selectPlaylistTracks:")))
        startsWith("addTracksToPlaylist:") -> MainRoute.AddTracksToPlaylist(Uri.decode(removePrefix("addTracksToPlaylist:")))
        startsWith("addToPlaylist:") -> removePrefix("addToPlaylist:").toLongOrNull()?.let(MainRoute::AddToPlaylist)
        else -> null
    }
