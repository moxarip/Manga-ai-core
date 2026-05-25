package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.MangaSource
import com.example.ui.viewmodel.SourcesUiState
import com.example.ui.viewmodel.SourcesViewModel
import com.example.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToMangaDetail: (String, String) -> Unit, // sourceId, mangaUrl
    modifier: Modifier = Modifier,
    viewModel: SourcesViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var customUrl by remember { mutableStateOf("") }
    val addSourceStatus by viewModel.addSourceStatus.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Manga Sources") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            
            Text("Available Sources", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            when (val state = uiState) {
                is SourcesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SourcesUiState.Error -> {
                    Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is SourcesUiState.Success -> {
                    LazyColumn {
                        items(state.sources) { source ->
                            SourceCard(source = source) {
                                // For the prototype, navigating to detail implies fetching info from that source url as a start
                                onNavigateToMangaDetail(source.id, source.baseUrl)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SourceCard(source: MangaSource, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = source.name, style = MaterialTheme.typography.titleMedium)
            Text(text = source.baseUrl, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Status: ${source.status}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
