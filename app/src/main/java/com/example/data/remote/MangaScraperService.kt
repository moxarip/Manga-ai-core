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
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .timeout(30_000)
                        .get()
                }
                if (doc != null) break
            } catch (e: Exception) {
                Log.e("Scraper", "Jsoup attempt ${attempts + 1} failed: ${e.message}")
                attempts++
                if (attempts < maxRetries) delay(1000)
            }
        }

        if (doc == null || doc.text().contains("Just a moment...") || doc.text().contains("cloudflare")) {
            Log.w("Scraper", "Jsoup failed or hit Cloudflare. Falling back to WebView.")
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
                settings.databaseEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                var checkCount = 0
                val handler = android.os.Handler(android.os.Looper.getMainLooper())
                
                fun checkHtml() {
                    if (resumed) return
                    webView.evaluateJavascript(
                        "(function() { return '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'; })();"
                    ) { htmlString ->
                        if (resumed) return@evaluateJavascript
                        
                        val unescaped = htmlString?.removeSurrounding("\"")
                            ?.replace("\\u003C", "<")
                            ?.replace("\\\"", "\"")
                            
                        // Check if it's still Cloudflare challenge
                        val lowerHtml = unescaped?.lowercase() ?: ""
                        if (lowerHtml.contains("just a moment...") || lowerHtml.contains("cloudflare") || lowerHtml.contains("please wait")) {
                            checkCount++
                            if (checkCount < 15) { // up to 30s
                                handler.postDelayed({ checkHtml() }, 2000)
                            } else {
                                resumed = true
                                continuation.resume(unescaped)
                            }
                        } else {
                            resumed = true
                            continuation.resume(unescaped)
                        }
                    }
                }

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Trigger check loop instead of immediate return
                        handler.postDelayed({ checkHtml() }, 2000)
                    }
                }
                webView.loadUrl(url)
                
                handler.postDelayed({
                    if (!resumed) {
                        resumed = true
                        Log.e("Scraper", "WebView timeout")
                        continuation.resume(null)
                    }
                }, 45_000)

            } catch (e: Exception) {
                if (!resumed) {
                    resumed = true
                    continuation.resume(null)
                }
            }
        }
    }
}
