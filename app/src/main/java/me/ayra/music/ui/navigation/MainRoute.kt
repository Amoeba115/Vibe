package me.ayra.music.ui.navigation

sealed interface MainRoute {
    data object Home : MainRoute
    data object Search : MainRoute
    data object Settings : MainRoute

    data class Album(val id: Long) : MainRoute
    data class Artist(val name: String) : MainRoute
    data class Folder(val path: String) : MainRoute

    data class Playlist(val id: String) : MainRoute
}
