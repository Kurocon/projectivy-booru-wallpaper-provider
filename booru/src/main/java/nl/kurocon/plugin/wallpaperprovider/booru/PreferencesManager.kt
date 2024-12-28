package nl.kurocon.plugin.wallpaperprovider.booru

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.IntDef
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import nl.kurocon.plugin.wallpaperprovider.booru.PreferencesManager.BooruType.Companion.DANBOORU
import nl.kurocon.plugin.wallpaperprovider.booru.PreferencesManager.BooruType.Companion.MOEBOORU
import nl.kurocon.plugin.wallpaperprovider.booru.PreferencesManager.BooruType.Companion.GELBOORU
import nl.kurocon.plugin.wallpaperprovider.booru.PreferencesManager.BooruType.Companion.ZEROCHAN
import nl.kurocon.plugin.wallpaperprovider.booru.PreferencesManager.BooruType.Companion.WALLHAVEN

object PreferencesManager {

    @Target(AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(DANBOORU, MOEBOORU, GELBOORU, ZEROCHAN, WALLHAVEN)
    annotation class BooruType {
        companion object {
            const val DANBOORU = 1
            const val MOEBOORU = 2
            const val GELBOORU = 3
            const val ZEROCHAN = 4
            const val WALLHAVEN = 5
        }
    }
    private val BOORU_TYPE_NAMES = mapOf(
        DANBOORU to "Danbooru",
        MOEBOORU to "Moebooru",
        GELBOORU to "Gelbooru",
        ZEROCHAN to "Zerochan",
        WALLHAVEN to "Wallhaven",
    )

    private const val BOORU_URL_KEY = "booru_url_key"
    private const val BOORU_TYPE_KEY = "booru_type_key"
    private const val BOORU_TAG_SEARCH_KEY = "booru_tag_search_key"
    private const val BOORU_USER_ID_KEY = "booru_user_id_key"
    private const val BOORU_API_KEY_KEY = "booru_api_key_key"

    lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    fun getBooruName(type: @BooruType Int): String {
        return BOORU_TYPE_NAMES[type]!!
    }

    operator fun set(key: String, value: Any?) =
        when (value) {
            is String? -> preferences.edit { it.putString(key, value) }
            is Int -> preferences.edit { it.putInt(key, value) }
            is Boolean -> preferences.edit { it.putBoolean(key, value) }
            is Float -> preferences.edit { it.putFloat(key, value) }
            is Long -> preferences.edit { it.putLong(key, value) }
            else -> throw UnsupportedOperationException("Not yet implemented")
        }

    inline operator fun <reified T : Any> get(
        key: String,
        defaultValue: T? = null
    ): T {
        try {
            when (T::class) {
                String::class -> return preferences.getString(key, defaultValue as String? ?: "") as T
                Int::class -> return preferences.getInt(key, defaultValue as? Int ?: -1) as T
                Boolean::class -> return preferences.getBoolean(
                    key,
                    defaultValue as? Boolean ?: false
                ) as T

                Float::class -> return preferences.getFloat(key, defaultValue as? Float ?: -1f) as T
                Long::class -> return preferences.getLong(key, defaultValue as? Long ?: -1) as T
                else -> throw UnsupportedOperationException("Not yet implemented")
            }
        } catch (e: ClassCastException) {
            return defaultValue as T
        }
    }

    var booruUrl: String
        get () = PreferencesManager[BOORU_URL_KEY, "https://danbooru.donmai.us"]
        set(value) { PreferencesManager[BOORU_URL_KEY] = value }
    var booruType: @BooruType Int
        get () = PreferencesManager[BOORU_TYPE_KEY, DANBOORU]
        set(value) { PreferencesManager[BOORU_TYPE_KEY] = value }
    var booruTagSearch: String
        get () = PreferencesManager[BOORU_TAG_SEARCH_KEY, "ratio:16:9 rating:general order:random"]
        set(value) { PreferencesManager[BOORU_TAG_SEARCH_KEY] = value }
    var booruUserId: String
        get () = PreferencesManager[BOORU_USER_ID_KEY, ""]
        set(value) { PreferencesManager[BOORU_USER_ID_KEY] = value }
    var booruApiKey: String
        get () = PreferencesManager[BOORU_API_KEY_KEY, ""]
        set(value) { PreferencesManager[BOORU_API_KEY_KEY] = value }

    fun export(): String {
        return Gson().toJson(preferences.all)
    }

    fun import(prefs: String): Boolean {
        val gson = GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create()

        try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map = gson.fromJson<Map<String, Any>>(prefs, type)
            val editor = preferences.edit()
            editor.clear()
            map.forEach { (key: String, value: Any) ->
                when(value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Double -> editor.putFloat(key, value.toFloat())
                    is Float -> editor.putFloat(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putInt(key, value.toInt())
                    is String -> editor.putString(key, value)
                    is ArrayList<*> -> editor.putStringSet(key, java.util.HashSet(value as java.util.ArrayList<String>))
                    is Set<*> -> editor.putStringSet(key, value as Set<String>)
                }
            }
            editor.apply()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }
        return true
    }
}
