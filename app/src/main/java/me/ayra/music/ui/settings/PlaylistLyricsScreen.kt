package me.ayra.music.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.ayra.music.PlaylistGroup
import me.ayra.music.R
import me.ayra.music.Track
import me.ayra.music.lyrics.LyricsCandidate
import me.ayra.music.lyrics.LyricsRepository
import me.ayra.music.lyrics.toEditableText

private const val CUSTOM_PLAYLIST_PREFIX = "custom-"

@Composable
internal fun PlaylistLyricsScreen(
    playlists: List<PlaylistGroup>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember(context) { LyricsRepository(context) }
    val customPlaylists = remember(playlists) { playlists.filter { it.id.startsWith(CUSTOM_PLAYLIST_PREFIX) } }
    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
    var candidatesByTrack by remember { mutableStateOf<Map<Long, List<LyricsCandidate>>>(emptyMap()) }
    var downloading by remember { mutableStateOf(false) }
    var completedTracks by remember { mutableStateOf(0) }
    var tracksWithoutResults by remember { mutableStateOf(0) }
    var tracksWithExistingLyrics by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var expandedCandidate by remember { mutableStateOf<Pair<Long, Int>?>(null) }
    var editingCandidate by remember { mutableStateOf<LyricsCandidate?>(null) }
    val selectedPlaylist = customPlaylists.firstOrNull { it.id == selectedPlaylistId }

    LaunchedEffect(selectedPlaylist?.id) {
        candidatesByTrack =
            if (selectedPlaylist == null) {
                emptyMap()
            } else {
                repository.candidatesFor(selectedPlaylist.tracks)
            }
        expandedCandidate = null
        statusMessage = null
    }

    fun refreshCandidates() {
        val playlist = selectedPlaylist ?: return
        coroutineScope.launch {
            candidatesByTrack = repository.candidatesFor(playlist.tracks)
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, top = 92.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.playlist_lyrics_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.select_custom_playlist),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
        if (customPlaylists.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_custom_playlists_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            item {
                SettingsGroup {
                    customPlaylists.forEachIndexed { index, playlist ->
                        PlaylistSelectionRow(
                            playlist = playlist,
                            selected = playlist.id == selectedPlaylistId,
                            onClick = { selectedPlaylistId = playlist.id },
                        )
                        if (index != customPlaylists.lastIndex) SettingsDivider()
                    }
                }
            }
        }
        selectedPlaylist?.let { playlist ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                downloading = true
                                completedTracks = 0
                                tracksWithoutResults = 0
                                tracksWithExistingLyrics = 0
                                statusMessage = null
                                try {
                                    playlist.tracks.forEach { track ->
                                        if (repository.localLyricsFor(track) != null) {
                                            tracksWithExistingLyrics++
                                            completedTracks++
                                            return@forEach
                                        }
                                        val results =
                                            repository
                                                .searchResults("${track.title} ${track.artist}")
                                                .take(LyricsRepository.MAX_CANDIDATES_PER_TRACK)
                                        if (results.isEmpty()) tracksWithoutResults++
                                        repository.replaceCandidates(track, results)
                                        completedTracks++
                                    }
                                    candidatesByTrack = repository.candidatesFor(playlist.tracks)
                                    statusMessage =
                                        context.getString(
                                            R.string.playlist_lyrics_batch_complete,
                                            playlist.tracks.size - tracksWithExistingLyrics - tracksWithoutResults,
                                            tracksWithExistingLyrics,
                                            tracksWithoutResults,
                                        )
                                } finally {
                                    downloading = false
                                }
                            }
                        },
                        enabled = !downloading && playlist.tracks.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.download_lyric_candidates))
                    }
                    if (downloading) {
                        LinearProgressIndicator(
                            progress = { completedTracks.toFloat() / playlist.tracks.size.coerceAtLeast(1) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text =
                                stringResource(
                                    R.string.playlist_lyrics_download_progress,
                                    completedTracks,
                                    playlist.tracks.size,
                                ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    statusMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            if (playlist.tracks.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_tracks_found),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                item {
                    Text(
                        text = stringResource(R.string.lyrics_candidates),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
                items(playlist.tracks, key = Track::id) { track ->
                    TrackCandidates(
                        track = track,
                        candidates = candidatesByTrack[track.id].orEmpty(),
                        expandedCandidateIndex = expandedCandidate?.takeIf { it.first == track.id }?.second,
                        onPreview = { candidate ->
                            expandedCandidate =
                                if (expandedCandidate == track.id to candidate.candidateIndex) {
                                    null
                                } else {
                                    track.id to candidate.candidateIndex
                                }
                        },
                        onUse = { candidate ->
                            coroutineScope.launch {
                                if (repository.selectCandidate(track, candidate)) {
                                    candidatesByTrack = repository.candidatesFor(playlist.tracks)
                                } else {
                                    statusMessage = context.getString(R.string.lyrics_candidate_save_failed)
                                }
                            }
                        },
                        onEdit = { editingCandidate = it },
                        onDelete = { candidate ->
                            coroutineScope.launch {
                                repository.deleteCandidate(candidate)
                                refreshCandidates()
                            }
                        },
                    )
                }
            }
        } ?: item {
            Text(
                text = stringResource(R.string.playlist_lyrics_no_selection),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    editingCandidate?.let { candidate ->
        EditCandidateDialog(
            candidate = candidate,
            onDismiss = { editingCandidate = null },
            onSave = { lyricsText ->
                coroutineScope.launch {
                    val updated = repository.updateCandidate(candidate, lyricsText)
                    if (updated == null) {
                        statusMessage = context.getString(R.string.lyrics_candidate_empty)
                    } else {
                        candidatesByTrack =
                            if (selectedPlaylist == null) {
                                emptyMap()
                            } else {
                                repository.candidatesFor(selectedPlaylist.tracks)
                            }
                        statusMessage = context.getString(R.string.lyrics_candidate_saved)
                        editingCandidate = null
                    }
                }
            },
        )
    }
}

@Composable
private fun PlaylistSelectionRow(
    playlist: PlaylistGroup,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
        ) {
            Text(
                text = playlist.title,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.tracks_count, playlist.tracks.size),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun TrackCandidates(
    track: Track,
    candidates: List<LyricsCandidate>,
    expandedCandidateIndex: Int?,
    onPreview: (LyricsCandidate) -> Unit,
    onUse: (LyricsCandidate) -> Unit,
    onEdit: (LyricsCandidate) -> Unit,
    onDelete: (LyricsCandidate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = track.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (candidates.isEmpty()) {
            Text(
                text = stringResource(R.string.lyrics_candidates_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            candidates.forEach { candidate ->
                CandidateCard(
                    candidate = candidate,
                    previewExpanded = candidate.candidateIndex == expandedCandidateIndex,
                    onPreview = { onPreview(candidate) },
                    onUse = { onUse(candidate) },
                    onEdit = { onEdit(candidate) },
                    onDelete = { onDelete(candidate) },
                )
            }
        }
    }
}

@Composable
private fun CandidateCard(
    candidate: LyricsCandidate,
    previewExpanded: Boolean,
    onPreview: () -> Unit,
    onUse: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.candidate_number, candidate.candidateIndex + 1),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                if (candidate.isSelected) {
                    Text(
                        text = stringResource(R.string.lyrics_candidate_selected),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Text(
                text = candidate.result.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (candidate.result.artist.isNotBlank()) {
                Text(
                    text = candidate.result.artist,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onPreview) {
                    Text(stringResource(R.string.preview))
                }
                TextButton(onClick = onUse) {
                    Text(stringResource(R.string.use_lyrics))
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            if (previewExpanded) {
                SelectionContainer {
                    Text(
                        text = candidate.lyrics.toEditableText(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditCandidateDialog(
    candidate: LyricsCandidate,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var lyricsText by remember(candidate.trackId, candidate.candidateIndex) {
        mutableStateOf(candidate.lyrics.toEditableText())
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_lyrics_candidate)) },
        text = {
            Box(modifier = Modifier.height(360.dp)) {
                OutlinedTextField(
                    value = lyricsText,
                    onValueChange = { lyricsText = it },
                    label = { Text(stringResource(R.string.lyrics_edit_hint)) },
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(lyricsText) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
