package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.security.SecurityManager
import com.example.domain.model.Manga
import com.example.domain.model.MangaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

class MangaScraperService(
    private val securityManager: SecurityManager,
    private val apiService: GeminiApiService = RetrofitClient.geminiService
) {

    private fun getApiKey(): String {
        return securityManager.getGeminiApiKey()?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY
    }

    suspend fun getMangaData(url: String, sourceId: String): Manga = withContext(Dispatchers.IO) {
        val html = fetchAndCleanHtml(url)
        val prompt = "Extract manga details from this HTML: $html"

        val schema = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("title") { put("type", "STRING") }
                putJsonObject("description") { put("type", "STRING") }
                putJsonObject("coverUrl") { put("type", "STRING") }
                putJsonObject("chapters") {
                    put("type", "ARRAY")
                    putJsonObject("items") {
                        put("type", "OBJECT")
                        putJsonObject("properties") {
                            putJsonObject("title") { put("type", "STRING") }
                            putJsonObject("url") { put("type", "STRING") }
                        }
                        putJsonArray("required") { add("title"); add("url") }
                    }
                }
            }
            putJsonArray("required") { add("title"); add("description"); add("coverUrl"); add("chapters") }
        }

        val request = GenerateContentRequest(
            systemInstruction = Content(listOf(Part("You are a strict data extraction tool. Based on the provided raw HTML, extract the manga details. Do not inject conversational text. Return only valid JSON according to the schema. Ensure relative URLs are formatted or just capture what's there."))),
            contents = listOf(Content(listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseFormat = ResponseFormat(
                    text = ResponseFormatText(
                        mimeType = "application/json",
                        schema = schema
                    )
                ),
                temperature = 0.0f
            )
        )

        val response = apiService.generateContent(getApiKey(), request)
        val jsonText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from AI")

        val jsonObject = Json.parseToJsonElement(jsonText).jsonObject
        val title = jsonObject["title"]?.jsonPrimitive?.content ?: "Unknown"
        val desc = jsonObject["description"]?.jsonPrimitive?.content ?: ""
        var cover = jsonObject["coverUrl"]?.jsonPrimitive?.content ?: ""
        if (cover.startsWith("/")) {
            val uri = URI(url)
            val baseHost = "${uri.scheme}://${uri.host}"
            cover = "$baseHost$cover"
        }

        val chaptersList = jsonObject["chapters"]?.jsonArray?.toString() ?: "[]"
        
        Manga(
            id = url,
            title = title,
            description = desc,
            coverUrl = cover,
            sourceId = sourceId,
            chaptersListJson = chaptersList
        )
    }
    
    suspend fun extractSourceInfo(url: String): MangaSource = withContext(Dispatchers.IO) {
        val uri = URI(url)
        val baseUrl = "${uri.scheme}://${uri.host}"
        
        val html = fetchAndCleanHtml(url)
        val prompt = "Extract the general name of this manga website from this HTML: $html"
        
        val schema = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("name") { put("type", "STRING") }
            }
            putJsonArray("required") { add("name") }
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseFormat = ResponseFormat(
                    text = ResponseFormatText(
                        mimeType = "application/json",
                        schema = schema
                    )
                ),
                temperature = 0.0f
            )
        )

        val response = apiService.generateContent(getApiKey(), request)
        val jsonText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from AI")
            
        val jsonObject = Json.parseToJsonElement(jsonText).jsonObject
        val name = jsonObject["name"]?.jsonPrimitive?.content ?: uri.host

        MangaSource(id = baseUrl, name = name, baseUrl = baseUrl, status = "CUSTOM")
    }

    private suspend fun fetchAndCleanHtml(url: String): String = withContext(Dispatchers.IO) {
        try {
            val doc: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .header("Referer", url)
                .get()

            doc.select("script, style, noscript, iframe, header, footer, nav, svg").remove()
            val text = doc.outerHtml().replace(Regex("\\s+"), " ")
            // Trim to max characters to avoid overwhelming token limits
            text.take(30000)
        } catch (e: Exception) {
            Log.e("MangaScraper", "Error fetching HTML", e)
            throw Exception("Network error or invalid URL: ${e.message}")
        }
    }
}
