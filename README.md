# Projectivy Plugin : Booru Wallpaper Provider

This is a [Projectivy](https://xdaforums.com/t/app-android-tv-projectivy-launcher.4436549/) Plugin that allows you to set a wallpaper from a Booru imageboard.

## Repository layout
- /booru : code for the Booru plugin service and its setting activity
- /api : api used to communicate with Projectivy through AIDL
 
# Usage
- Download the `booru-release.apk` from the releases tab and install it on your TV
- Navigate to Projectivy -> Launcher settings -> Appearance -> Wallpaper -> Launcher wallpaper
- The "Booru Wallpaper Provider" should be available.
- Configure the plugin via the "Configure" button. The following settings are available:
  - Booru URL: The base URL to the Booru site, including protocol. For example: `https://danbooru.donmai.us`
  - Booru Type: Only `danbooru`-based boards are currently supported. Support for more types is planned.
  - Search query: The search query from which images are pulled. This is the same as you would fill in on the imageboard website's search box. Check the help pages of your booru for guidance. (i.e. the [cheatsheet](https://danbooru.donmai.us/wiki_pages/help%3Acheatsheet) for Danbooru) All tag types should work normally (including ordering, ratio, and other metatags).
  - Username and API Key: These are optional. Some boorus have limitations on how many tags can be searched anonymously or by standard users. For example, Danbooru allows 2 tags for logged out and normal users, but 6 for premium accounts. Fill in these fields to authenticate. 

# Screenshots
![screenshot](./.github/readme-images/background.png)

![screenshot](./.github/readme-images/settings.png)

# Note
This plugin is provided as an open-source project and is distributed "as is." While the author may offer voluntary support, there is no guarantee of availability or resolution. The author is not responsible for any damages, data loss, or issues arising from the use of this plugin. Use at your own risk.
