package nl.kurocon.plugin.wallpaperprovider.booru

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType
import java.io.IOException

class WallpaperProviderService: Service() {

    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
    }

    override fun onBind(intent: Intent): IBinder {
        // Return the interface.
        return binder
    }

    private fun getNewWallpapers(): List<Wallpaper> {
        try {
            return BooruAPI(this).getImageUrls(20).map {
                Wallpaper(
                    uri = it.imageUrl,
                    type = WallpaperType.IMAGE,
                    author = it.author,
                    actionUri = it.sourceUri,
                    title = it.title,
                    source = it.author
                )
            }
        } catch (e: IOException) {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@WallpaperProviderService,
                        "Failed to fetch new images: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return emptyList()
        }
    }


    private val binder = object : IWallpaperProviderService.Stub() {
        override fun getWallpapers(event: Event?): List<Wallpaper> {

            return when (event) {
                is Event.TimeElapsed -> {
                    // This is where you generate the wallpaper list that will be cycled every x minute
                    return getNewWallpapers()
                }

                // Below are "dynamic events" that might interest you in special cases
                // You will only receive dynamic events depending on the updateMode declared in your manifest
                // Don't subscribe if not interested :
                //  - this will consume device resources unnecessarily
                //  - some cache optimizations won't be enabled for dynamic wallpaper providers

                // When "now playing" changes (ex: a song starts or stops)
                is Event.NowPlayingChanged -> emptyList()
                // When the focused card changes
                is Event.CardFocused -> emptyList()
                // When the focused "program" card changes
                is Event.ProgramCardFocused -> emptyList()
                // When Projectivy enters or exits idle mode
                is Event.LauncherIdleModeChanged -> {
                    return if (event.isIdle) { getNewWallpapers() }
                        else  emptyList()
                }
                else -> emptyList()  // Returning an empty list won't change the currently displayed wallpaper
            }
        }

        override fun getPreferences(): String {
            return PreferencesManager.export()
        }

        override fun setPreferences(params: String) {
            PreferencesManager.import(params)
        }

        fun getDrawableUri(drawableId: Int): Uri {
            return Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(drawableId))
                .appendPath(resources.getResourceTypeName(drawableId))
                .appendPath(resources.getResourceEntryName(drawableId))
                .build()
        }


    }
}