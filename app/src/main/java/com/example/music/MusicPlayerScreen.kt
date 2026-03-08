package com.example.music


import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun MusicPlayerScreen(
    navController: NavController,
    viewModel: PlayerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val currentSong = uiState.songs.getOrNull(uiState.currentIndex)
    val duration = uiState.duration.coerceAtLeast(1L)

    val progress =
        (uiState.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF052A35),
            Color(0xFF1D2C8E),
            Color(0xFF5A0C7B),
            Color(0xFF0B2E3D)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .navigationBarsPadding()
            .padding(vertical = 22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(
                onBackClick = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(42.dp))

            AsyncImage(
                model = currentSong?.artworkUri,
                contentDescription = currentSong?.title,
                placeholder = painterResource(R.drawable.unnamed),
                error = painterResource(R.drawable.unnamed),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(300.dp)
                    .clip(RoundedCornerShape(26.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentSong?.title ?: "No song selected",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .basicMarquee(),
                textAlign = TextAlign.Center,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = currentSong?.artist ?: "Unknown artist",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .basicMarquee(),
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.65f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                MusicProgressSlider(
                    progress = progress,
                    currentTime = formatDuration(uiState.currentPosition),
                    totalTime = formatDuration(uiState.duration),
                    onProgressChange = { newProgress ->
                        val newPosition = (uiState.duration * newProgress).toLong()
                        viewModel.seekTo(newPosition)
                    }
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SmallControlButton(
                    icon = R.drawable.baseline_skip_previous_24,
                    onClick = { viewModel.previous() },
                    tint = Color.White
                )

                PlayButton(
                    isPlaying = uiState.isPlaying,
                    onClick = { viewModel.togglePlayPause() }
                )

                SmallControlButton(
                    icon = R.drawable.baseline_skip_next_24,
                    onClick = { viewModel.next() },
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            text = "Music Player",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
    }
}

@Composable
private fun SmallControlButton(
    icon: Int,
    onClick: () -> Unit,
    tint: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(35.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(35.dp)
        )
    }
}

@Composable
private fun PlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Transparent,
        modifier = Modifier.size(75.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isPlaying) {
                Icon(
                    painter = painterResource(R.drawable.outline_pause_circle_24),
                    contentDescription = "Pause",
                    tint = Color.White,
                    modifier = Modifier
                        .size(75.dp)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.outline_play_circle_24),
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier
                        .size(100.dp)
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

