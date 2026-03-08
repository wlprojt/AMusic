package com.example.music

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun PlayerScreen(navController: NavController, viewModel: PlayerViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val currentSong = if (uiState.currentIndex in uiState.songs.indices) {
        uiState.songs[uiState.currentIndex]
    } else null

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF052A35),
            Color(0xFF1D2C8E),
            Color(0xFF5A0C7B),
            Color(0xFF0B2E3D)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
            .padding(16.dp)
    ) {
        Text(
            text = "Music Player",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(top = 26.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.songs.isEmpty()) {
            Text("No songs found")
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.songs) { song ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { viewModel.playSong(song) },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Row(

                        ) {
                            AsyncImage(
                                model = song.artworkUri,
                                contentDescription = null,
                                placeholder = painterResource(R.drawable.unnamed),
                                error = painterResource(R.drawable.unnamed),
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        if (currentSong != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {navController.navigate("play")}
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                AsyncImage(
                    model = currentSong.artworkUri,
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.unnamed),
                    error = painterResource(R.drawable.unnamed),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(MaterialTheme.shapes.medium)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = currentSong.title,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Text(
                        text = currentSong.artist,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { viewModel.togglePlayPause() }) {
                    if (uiState.isPlaying) {
                        Icon(
                            painter = painterResource(R.drawable.outline_pause_circle_24),
                            contentDescription = "Pause",
                            tint = Color.White,
                            modifier = Modifier
                                .size(35.dp)
                                .offset(x = 5.dp, y = 0.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.outline_play_circle_24),
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier
                                .size(35.dp)
                                .offset(x = 5.dp, y = 0.dp)
                        )
                    }
                }

                IconButton(onClick = { viewModel.next() }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_skip_next_24),
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.offset(x = 8.dp, y = 0.dp)
                    )
                }
            }
        }
    }
}