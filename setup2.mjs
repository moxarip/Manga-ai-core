import fs from 'fs';
import path from 'path';
function write(f,c){ fs.mkdirSync(path.dirname(f),{recursive:true}); fs.writeFileSync(f,c.trim()+'\n'); }

write('app/src/main/java/com/example/ui/theme/Color.kt', `
package com.example.ui.theme
import androidx.compose.ui.graphics.Color
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
`);
write('app/src/main/java/com/example/ui/theme/Type.kt', `
package com.example.ui.theme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
val Typography = Typography(
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp)
)
`);
write('app/src/main/java/com/example/ui/theme/Theme.kt', `
package com.example.ui.theme
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val LightColorScheme = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = true, content: @Composable () -> Unit) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
`);
write('app/src/main/java/com/example/MangaApplication.kt', `
package com.example
import android.app.Application
class MangaApplication : Application()
`);
write('app/src/main/java/com/example/data/remote/MangaScraperService.kt', `
package com.example.data.remote

import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.util.Log

class MangaScraperService(private val context: Context) {
    private val desktopUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    suspend fun parseMangaSite(url: String): Document? = withContext(Dispatchers.IO) {
        var doc: Document? = null
        var attempts = 0
        val maxRetries = 3

        while (attempts < maxRetries) {
            try {
                // Jsoup timeout 30s as requested
                doc = withTimeoutOrNull(35_000) {
                    Jsoup.connect(url)
                        .userAgent(desktopUserAgent)
                        .timeout(30_000)
                        .get()
                }
                if (doc != null) break
            } catch (e: Exception) {
                Log.e("Scraper", "Jsoup attempt \\${attempts + 1} failed: \\${e.message}")
                attempts++
                if (attempts < maxRetries) delay(1000)
            }
        }

        if (doc == null) {
            Log.w("Scraper", "Jsoup failed after max retries. Falling back to WebView.")
            // Fallback to WebView in Dispatchers.Main
            val html = fetchWithWebViewFallback(url)
            if (html != null) {
                doc = Jsoup.parse(html)
            }
        }
        
        doc
    }

    private suspend fun fetchWithWebViewFallback(url: String): String? = withContext(Dispatchers.Main) {
        suspendCoroutine { continuation ->
            var resumed = false
            try {
                val webView = WebView(context)
                val settings: WebSettings = webView.settings
                settings.javaScriptEnabled = true
                settings.userAgentString = desktopUserAgent
                settings.domStorageEnabled = true

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            "(function() { return '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'; })();"
                        ) { htmlString ->
                            if (!resumed) {
                                resumed = true
                                val unescaped = htmlString?.removeSurrounding("\\"")
                                    ?.replace("\\\\u003C", "<")
                                    ?.replace("\\\\\\"", "\\"")
                                continuation.resume(unescaped)
                            }
                        }
                    }
                }
                webView.loadUrl(url)
                
                webView.postDelayed({
                    if (!resumed) {
                        resumed = true
                        Log.e("Scraper", "WebView timeout")
                        continuation.resume(null)
                    }
                }, 30_000)

            } catch (e: Exception) {
                if (!resumed) {
                    resumed = true
                    continuation.resume(null)
                }
            }
        }
    }
}
`);
write('app/src/main/java/com/example/MainActivity.kt', `
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
                        result = "Success! Title: \\${doc.title()}\\n\\nHTML snippet:\\n\\${doc.html().take(300)}..."
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
`);
console.log("SUCCESS");
