package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import com.example.data.remote.MangaScraperService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scraperService = MangaScraperService(applicationContext)

        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ScraperScreen(scraperService)
                }
            }
        }
    }
}

@Composable
fun ScraperScreen(scraperService: MangaScraperService) {
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf("https://example.com") }
    var result by remember { mutableStateOf("Enter a URL and press fetch.") }
    var isFetching by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isFetching = true
                result = "Fetching..."
                scope.launch {
                    val doc = scraperService.parseMangaSite(url)
                    isFetching = false
                    if (doc != null) {
                        result = "Success! Title: ${doc.title()}\n\nHTML snippet:\n${doc.html().take(300)}..."
                    } else {
                        result = "Failed to fetch document."
                    }
                }
            },
            enabled = !isFetching
        ) {
            Text("Fetch Data")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isFetching) {
            CircularProgressIndicator()
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(result)
    }
}
