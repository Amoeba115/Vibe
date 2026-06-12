package me.ayra.music.lyrics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LyricsRenderer(
    lyrics: Lyrics,
    positionMs: Long,
    accent: Color,
    onTimestampClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val activeIndex = LyricSynchronizer.activeLineIndex(lyrics, positionMs)

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem(activeIndex, scrollOffset = -120)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        itemsIndexed(lyrics.lines) { index, line ->
            val active = index == activeIndex || (activeIndex < 0 && index == 0)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(
                            line.startMs?.let { timestamp ->
                                Modifier.clickable { onTimestampClick(timestamp) }
                            } ?: Modifier,
                        ),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Text(
                    text = line.text.ifBlank { "..." },
                    color = if (active) accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f),
                    fontSize = if (active) 28.sp else 21.sp,
                    lineHeight = if (active) 34.sp else 28.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
