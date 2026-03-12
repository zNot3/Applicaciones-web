package com.curso.android.module2.stream.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.curso.android.module2.stream.data.model.Playlist
import com.curso.android.module2.stream.ui.components.SongCoverMock
import com.curso.android.module2.stream.ui.viewmodel.LibraryUiState
import com.curso.android.module2.stream.ui.viewmodel.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * ================================================================================
 * LIBRARY SCREEN - Biblioteca del Usuario
 * ================================================================================
 *
 * Esta pantalla muestra las playlists guardadas del usuario.
 * Es parte del sistema de BottomNavigation (Home, Search, Library).
 *
 * DIFERENCIA CON HOME:
 * - Home: Muestra categorías con listas horizontales (LazyRow en LazyColumn)
 * - Library: Muestra playlists en lista vertical simple
 *
 * PATRÓN UI:
 * - Lista vertical de playlists
 * - Cada playlist muestra: cover, nombre, descripción, número de canciones
 * - Click en playlist podría navegar al detalle (no implementado en este ejemplo)
 */

/**
 * Pantalla de biblioteca que muestra las playlists del usuario.
 *
 * @param viewModel ViewModel que provee el estado (inyectado por Koin)
 * @param onPlaylistClick Callback cuando el usuario selecciona una playlist
 * @param modifier Modificador de Compose
 */
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = koinViewModel(),
    onPlaylistClick: (Playlist) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier) {
        when (val state = uiState) {
            is LibraryUiState.Loading -> {
                LoadingContent()
            }

            is LibraryUiState.Success -> {
                LibraryContent(
                    playlists = state.playlists,
                    onPlaylistClick = onPlaylistClick
                )
            }

            is LibraryUiState.Error -> {
                ErrorContent(message = state.message)
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $message",
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Contenido principal con las playlists.
 */
@Composable
private fun LibraryContent(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Playlists
        items(
            items = playlists,
            key = { it.id }
        ) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist) }
            )
        }
    }
}

/**
 * Tarjeta de una playlist.
 *
 * Muestra el cover, nombre, descripción y número de canciones.
 */
@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cover de la playlist
        SongCoverMock(
            colorSeed = playlist.colorSeed,
            size = 64.dp
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Información de la playlist
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = playlist.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${playlist.songCount} songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
