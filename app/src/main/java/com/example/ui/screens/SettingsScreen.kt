package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.SourcesViewModel
import com.example.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = ViewModelFactory.Factory),
    sourcesViewModel: SourcesViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    var inputKey by remember { mutableStateOf(apiKey) }
    
    var customUrl by remember { mutableStateOf("") }
    val addSourceStatus by sourcesViewModel.addSourceStatus.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Gemini API Key", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Set your own Gemini API key to allow dynamic website scraping. Overrides BuildConfig environment variable.", 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = inputKey,
                onValueChange = { inputKey = it },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.saveApiKey(inputKey) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Key")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Add Custom Source", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    label = { Text("Manga URL") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { sourcesViewModel.addCustomSource(customUrl) }) {
                    Text("Add")
                }
            }
            
            if (addSourceStatus != null) {
                 Text(
                    text = addSourceStatus!!, 
                    color = if (addSourceStatus!!.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Fallback Key Mode", style = MaterialTheme.typography.titleSmall)
            Text(
                text = if (viewModel.fallbackApiKey.isBlank()) "No key found in .env" else "Fallback key is loaded via BuildConfig.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
