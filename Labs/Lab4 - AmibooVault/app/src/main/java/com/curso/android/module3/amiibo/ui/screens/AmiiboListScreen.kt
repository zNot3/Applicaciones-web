package com.curso.android.module3.amiibo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.curso.android.module3.amiibo.R
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import com.curso.android.module3.amiibo.domain.error.ErrorType
import com.curso.android.module3.amiibo.ui.viewmodel.AmiiboUiState
import com.curso.android.module3.amiibo.ui.viewmodel.AmiiboViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmiiboListScreen(
    onAmiiboClick: (String) -> Unit = {},
    viewModel: AmiiboViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pageSize by viewModel.pageSize.collectAsStateWithLifecycle()
    val hasMorePages by viewModel.hasMorePages.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val paginationError by viewModel.paginationError.collectAsStateWithLifecycle()

    var showPageSizeDropdown by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState is AmiiboUiState.Error) {
        val errorState = uiState as AmiiboUiState.Error
        if (!errorState.cachedData.isNullOrEmpty()) {
            LaunchedEffect(errorState.message) {
                val result = snackbarHostState.showSnackbar(
                    message = errorState.message,
                    actionLabel = "Reintentar",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.refreshAmiibos()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    Box {
                        TextButton(onClick = { showPageSizeDropdown = true }) {
                            Text("Página: $pageSize")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showPageSizeDropdown,
                            onDismissRequest = { showPageSizeDropdown = false }
                        ) {
                            viewModel.pageSizeOptions.forEach { size ->
                                DropdownMenuItem(
                                    text = { Text("$size por página") },
                                    onClick = {
                                        viewModel.setPageSize(size)
                                        showPageSizeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.refreshAmiibos() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->

        when (val state = uiState) {
            is AmiiboUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is AmiiboUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refreshAmiibos() },
                    modifier = Modifier.padding(paddingValues)
                ) {
                    AmiiboGrid(
                        amiibos = state.amiibos,
                        onAmiiboClick = onAmiiboClick,
                        hasMorePages = hasMorePages,
                        isLoadingMore = isLoadingMore,
                        paginationError = paginationError,
                        onLoadMore = { viewModel.loadNextPage() },
                        onRetryLoadMore = { viewModel.retryLoadMore() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            is AmiiboUiState.Error -> {
                if (!state.cachedData.isNullOrEmpty()) {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.refreshAmiibos() },
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        AmiiboGrid(
                            amiibos = state.cachedData,
                            onAmiiboClick = onAmiiboClick,
                            hasMorePages = false,
                            isLoadingMore = false,
                            paginationError = null,
                            onLoadMore = {},
                            onRetryLoadMore = {},
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.refreshAmiibos() },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff, // Icono genérico por defecto
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Error de conexión",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text(text = "Reintentar")
            }
        }
    }
}

private fun ErrorType.toIcon(): ImageVector = when (this) {
    ErrorType.NETWORK -> Icons.Default.CloudOff   // Sin conexión
    ErrorType.PARSE -> Icons.Default.Warning      // Error de datos
    ErrorType.DATABASE -> Icons.Default.Storage   // Error de BD
    ErrorType.UNKNOWN -> Icons.Default.Error      // Error genérico
}

@Composable
private fun ErrorBanner(
    message: String,
    errorType: ErrorType,
    isRetryable: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = errorType.toIcon(),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (isRetryable) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = stringResource(R.string.retry))
                }
            }
        }
    }
}

@Composable
private fun AmiiboGrid(
    amiibos: List<AmiiboEntity>,
    onAmiiboClick: (String) -> Unit,
    hasMorePages: Boolean,
    isLoadingMore: Boolean,
    paginationError: String?,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            lastVisibleItem >= totalItems - 6 &&
                    hasMorePages &&
                    !isLoadingMore &&
                    paginationError == null &&
                    totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        state = gridState,
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = amiibos,
            key = { it.id }
        ) { amiibo ->
            AmiiboCard(
                amiibo = amiibo,
                onClick = { onAmiiboClick(amiibo.name) }
            )
        }

        if (isLoadingMore) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        if (paginationError != null) {
            item(span = { GridItemSpan(2) }) {
                PaginationErrorItem(
                    errorMessage = paginationError,
                    onRetry = onRetryLoadMore
                )
            }
        }

        if (!hasMorePages && amiibos.isNotEmpty() && paginationError == null) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "— Fin de la lista (${amiibos.size} amiibos) —",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PaginationErrorItem(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reintentar",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun AmiiboCard(
    amiibo: AmiiboEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen con fondo degradado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = amiibo.imageUrl,
                        contentDescription = stringResource(
                            R.string.amiibo_image_description,
                            amiibo.name
                        ),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = amiibo.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Surface(
                            modifier = Modifier.padding(top = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = amiibo.gameSeries,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
