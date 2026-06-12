package me.ayra.music.ui.navigation

sealed interface MainRoute {
    data object Home : MainRoute
    data object Search : MainRoute
    data class SearchTracks(val query: String) : MainRoute
    data class SearchArtists(val query: String) : MainRoute
    data class SearchAlbums(val query: String) : MainRoute
    data object Settings : MainRoute

    data class Album(val id: Long) : MainRoute
    data class Artist(val name: String) : MainRoute
    data class Folder(val path: String) : MainRoute

    data class Playlist(val id: String) : MainRoute
    data class SelectPlaylistTracks(val name: String) : MainRoute
    data class AddTracksToPlaylist(val id: String) : MainRoute
    data object AddTracksToCurrentQueue : MainRoute
    data class AddToPlaylist(val trackId: Long) : MainRoute
    data class AddToTracks(val trackIds: List<Long>) : MainRoute
}
