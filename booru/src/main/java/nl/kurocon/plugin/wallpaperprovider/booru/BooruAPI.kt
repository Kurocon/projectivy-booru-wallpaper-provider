package nl.kurocon.plugin.wallpaperprovider.booru

import android.content.Context
import nl.kurocon.plugin.wallpaperprovider.booru.PreferencesManager.BooruType.Companion.DANBOORU
import nl.kurocon.plugin.wallpaperprovider.booru.PreferencesManager.BooruType.Companion.GELBOORU
import nl.kurocon.plugin.wallpaperprovider.booru.PreferencesManager.BooruType.Companion.MOEBOORU
import nl.kurocon.plugin.wallpaperprovider.booru.PreferencesManager.BooruType.Companion.WALLHAVEN
import nl.kurocon.plugin.wallpaperprovider.booru.PreferencesManager.BooruType.Companion.ZEROCHAN
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder

class BooruAPI(context: Context) {
    private val client = OkHttpClient()
    private var booruPluginUserAgent = "ProjectivyBooruWallpaperPlugin"

    init {
        PreferencesManager.init(context)
        val pluginName = context.getString(R.string.plugin_name)
        val pluginVersion = context.getString(R.string.plugin_version)
        booruPluginUserAgent = "$pluginName/$pluginVersion"
    }


    @Throws(IOException::class)
    fun getImageUrls(limit: Int = 20): List<BooruImage> {
        return when (PreferencesManager.booruType) {
            DANBOORU -> getDanbooruImageUrls(limit = limit)
            GELBOORU -> getGelbooruImageUrls(limit = limit)
            MOEBOORU -> getMoebooruImageUrls(limit = limit)
            ZEROCHAN -> getZerochanImageUrls(limit = limit)
            WALLHAVEN -> getWallhavenImageUrls(limit = limit)
            else -> getDanbooruImageUrls(limit = limit)
        }
    }

    private fun getRequestBuilder(url: String, addAuth: Boolean = false): Request.Builder {
        // Add default headers (User-Agent) and set URL
        var builder = Request.Builder()
            .url(url)
            .addHeader("User-Agent", booruPluginUserAgent)
        if (addAuth && (PreferencesManager.booruUserId.isNotEmpty() && PreferencesManager.booruApiKey.isNotEmpty())) {
            builder = builder.addHeader(
                "Authorization",
                okhttp3.Credentials.basic(PreferencesManager.booruUserId, PreferencesManager.booruApiKey)
            )
        }
        return builder
    }

    @Throws(IOException::class)
    fun getDanbooruImageUrls(limit: Int = 20): List<BooruImage> {
        val tags = PreferencesManager.booruTagSearch
        val encodedTags = URLEncoder.encode(tags, "UTF-8")
        val url = "${PreferencesManager.booruUrl}/posts.json?tags=$encodedTags&limit=$limit"
        val request = getRequestBuilder(url, addAuth=true).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                // Try to parse error message from body
                val responseBody = response.body?.string() ?: throw IOException("Unexpected code ${response.code}, and response body is empty")
                val jsonBody = JSONObject(responseBody)
                val errorMessage = jsonBody.optString("message")
                if (errorMessage.isNotEmpty()) {
                    throw IOException("Error ${response.code}. $errorMessage")
                }
                throw IOException("Error ${response.code} - ${response.body}")
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

    @Throws(IOException::class)
    fun getMoebooruImageUrls(limit: Int = 20): List<BooruImage> {
        val tags = PreferencesManager.booruTagSearch
        val encodedTags = URLEncoder.encode(tags, "UTF-8")
        var url = "${PreferencesManager.booruUrl}/post.json?tags=$encodedTags&limit=$limit"
        if (PreferencesManager.booruUserId.isNotEmpty() && PreferencesManager.booruApiKey.isNotEmpty()) {
            val encUsername = URLEncoder.encode(PreferencesManager.booruUserId, "UTF-8")
            val encApiKey = URLEncoder.encode(PreferencesManager.booruApiKey, "UTF-8")
            url = "$url&login=$encUsername&password_hash=$encApiKey"
        }
        val request = getRequestBuilder(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                // Try to parse error message from body
                val responseBody = response.body?.string() ?: throw IOException("Unexpected code ${response.code}, and response body is empty")
                val jsonBody = JSONObject(responseBody)
                val errorMessage = jsonBody.optString("message")
                if (errorMessage.isNotEmpty()) {
                    throw IOException("Error ${response.code}. $errorMessage")
                }
                throw IOException("Error ${response.code} - ${response.body}")
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

    @Throws(IOException::class)
    fun getGelbooruImageUrls(limit: Int = 20): List<BooruImage> {
        val tags = PreferencesManager.booruTagSearch
        val encodedTags = URLEncoder.encode(tags, "UTF-8")
        var url = "${PreferencesManager.booruUrl}/index.php?page=dapi&s=post&q=index&tags=$encodedTags&limit=$limit&json=1"
        if (PreferencesManager.booruUserId.isNotEmpty() && PreferencesManager.booruApiKey.isNotEmpty()) {
            val encUsername = URLEncoder.encode(PreferencesManager.booruUserId, "UTF-8")
            val encApiKey = URLEncoder.encode(PreferencesManager.booruApiKey, "UTF-8")
            url = "$url&api_key=$encApiKey&user_id=$encUsername"
        }
        val request = getRequestBuilder(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                // Try to parse error message from body
                val responseBody = response.body?.string() ?: throw IOException("Unexpected code ${response.code}, and response body is empty")
                val jsonBody = JSONObject(responseBody)
                val errorMessage = jsonBody.optString("message")
                if (errorMessage.isNotEmpty()) {
                    throw IOException("Error ${response.code}. $errorMessage")
                }
                throw IOException("Error ${response.code} - ${response.body}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Response body is null")
            val jsonBody: JSONObject
            try {
                jsonBody = JSONObject(responseBody)
            } catch (e: Exception) {
                // gelbooru returns xml response if request was denied for some reason
                // i.e. user hit a rate limit because he didn't include api key
                throw IOException("Error. Unexpected response. You might be rate limited.")
            }

            // Parse JSON response into BooruImage instances
            val images = mutableListOf<BooruImage>()
            val jsonArray = jsonBody.getJSONArray("post")
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val imageUrl = jsonObject.optString("file_url")
                val title = jsonObject.optString("tags")
                // Gelbooru does not have an easily accessible Artist tag. Link to the source in a best-effort way.
                val author = jsonObject.optString("source")
                val sourceUri = jsonObject.optString("source")
                if (imageUrl.isNotEmpty()) {
                    images.add(BooruImage(imageUrl, title, author, sourceUri))
                }
            }
            return images
        }
    }

    @Throws(IOException::class)
    fun getZerochanImageUrls(limit: Int = 20): List<BooruImage> {
        val tags = PreferencesManager.booruTagSearch
        val encodedTags = URLEncoder.encode(tags, "UTF-8")
        val url = "${PreferencesManager.booruUrl}/$encodedTags?json&l=$limit"
        var requestBuilder = getRequestBuilder(url)
        // Zerochan has no authentication, but asks to put the username in the User-Agent string.
        // This header is already added by the `getRequestBuilder`, so need to override it.
        if (PreferencesManager.booruUserId.isNotEmpty()) {
             requestBuilder = requestBuilder.header(
                 "User-Agent", "$booruPluginUserAgent - ${PreferencesManager.booruUserId}"
             )
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
                throw IOException("Error ${response.code} - ${response.body}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Response body is null")
            val jsonArray = JSONObject(responseBody).getJSONArray("items")

            // Parse JSON response into BooruImage instances
            val images = mutableListOf<BooruImage>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val imageId = jsonObject.optInt("id")
                if (imageId != 0) {
                    // Do a second request for the complete image information, because
                    // zerochan does not include the full image link in the list results...
                    var requestBuilder2 =
                        getRequestBuilder("${PreferencesManager.booruUrl}/$imageId?json")
                    if (PreferencesManager.booruUserId.isNotEmpty()) {
                        requestBuilder2 = requestBuilder2.header(
                            "User-Agent",
                            "$booruPluginUserAgent - ${PreferencesManager.booruUserId}"
                        )
                    }
                    val request2 = requestBuilder2.build()
                    client.newCall(request2).execute().use { response2 ->
                        if (response2.isSuccessful) {
                            val responseBody2 = response2.body?.string()
                                ?: throw IOException("Response body is null")
                            val jsonBody: JSONObject
                            try {
                                jsonBody = JSONObject(responseBody2)
                                val imageUrl = jsonBody.optString("full")
                                val title1 = jsonBody.optString("primary")
                                val title2 = jsonBody.optJSONArray("tags")?.join(" ")
                                val title = "$title1 - $title2"
                                // Zerochan does not have an easily accessible Artist tag. Link to the source in a best-effort way.
                                val author = jsonBody.optString("source")
                                val sourceUri = jsonBody.optString("source")
                                if (imageUrl.isNotEmpty()) {
                                    images.add(BooruImage(imageUrl, title, author, sourceUri))
                                }
                            } catch (e: JSONException) {
                                // Invalid response, do nothing (probably rate limited, HTML output)
                            }
                        }
                    }
                }
            }
            return images
        }
    }

    @Throws(IOException::class)
    fun getWallhavenImageUrls(limit: Int = 20): List<BooruImage> {
        val tags = PreferencesManager.booruTagSearch
        val encodedTags = URLEncoder.encode(tags, "UTF-8")
        var url = "${PreferencesManager.booruUrl}/api/v1/search?q=$encodedTags&sorting=random"
        val requestBuilder = getRequestBuilder(url)
        if (PreferencesManager.booruApiKey.isNotEmpty()) {
            // Users can authenticate by including their API key either in a request URL by appending
            // ?apikey=<API KEY>, or by including the X-API-Key: <API KEY> header with the request.
            // API key grants access to NSFW images (but the purity=111 flag is not set by this plugin, default is SFW, 100).
            requestBuilder.addHeader("X-API-Key", PreferencesManager.booruApiKey)
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
                throw IOException("Error ${response.code} - ${response.body}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Response body is null")
            val jsonBody = JSONObject(responseBody)
            val jsonArray = jsonBody.getJSONArray("data")

            // Parse JSON response into BooruImage instances
            val images = mutableListOf<BooruImage>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val imageUrl = jsonObject.optString("path")
                val title = jsonObject.optString("id")
                val author = jsonObject.optString("source")
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
