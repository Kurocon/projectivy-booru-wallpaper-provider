package nl.kurocon.plugin.wallpaperprovider.booru

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class BooruAPI(context: Context) {
    private val client = OkHttpClient()

    init {
        PreferencesManager.init(context)
    }

    @Throws(IOException::class)
    fun getImageUrls(limit: Int = 20): List<BooruImage> {
        return when (PreferencesManager.booruType) {
            "danbooru" -> getDanbooruImageUrls(limit = limit)
            else -> getDanbooruImageUrls(limit = limit)
        }
    }

    @Throws(IOException::class)
    fun getDanbooruImageUrls(limit: Int = 20): List<BooruImage> {
        val tags = PreferencesManager.booruTagSearch
        val encodedTags = URLEncoder.encode(tags, "UTF-8")
        val url = "${PreferencesManager.booruUrl}/posts.json?tags=$encodedTags&limit=$limit"
        var requestBuilder = Request.Builder()
            .url(url)
        if (PreferencesManager.booruUserId.isNotEmpty() && PreferencesManager.booruApiKey.isNotEmpty()) {
            requestBuilder = requestBuilder.addHeader("Authorization", okhttp3.Credentials.basic(PreferencesManager.booruUserId, PreferencesManager.booruApiKey))
        }
        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                // Try to parse error message from body
                val responseBody = response.body?.string() ?: throw IOException("Unexpected code ${response.code}, and response body is empty")
                val jsonBody = JSONObject(responseBody)
                val errorMessage = jsonBody.optString("message")
                if (errorMessage.isNotEmpty()) {
                    throw IOException("Error ${response.code}. $errorMessage")
                }
                throw IOException("Unexpected code ${response.code} - ${response.body}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Response body is null")
            val jsonArray = JSONArray(responseBody)

            // Parse JSON response into BooruImage instances
            val images = mutableListOf<BooruImage>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val imageUrl = jsonObject.optString("file_url")
                val title = jsonObject.optString("tag_string")
                val author = jsonObject.optString("tag_string_artist")
                val sourceUri = jsonObject.optString("source")
                if (imageUrl.isNotEmpty()) {
                    images.add(BooruImage(imageUrl, title, author, sourceUri))
                }
            }
            return images
        }
    }
}

class BooruImage(
    var imageUrl: String,
    var title: String,
    var author: String,
    var sourceUri: String
) {}
