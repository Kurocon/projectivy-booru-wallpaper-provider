package nl.kurocon.plugin.wallpaperprovider.booru


import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import kotlin.CharSequence

class SettingsFragment : GuidedStepSupportFragment() {
    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        return Guidance(
            getString(R.string.plugin_name),
            getString(R.string.plugin_description),
            getString(R.string.settings),
            AppCompatResources.getDrawable(requireActivity(), R.drawable.danbooru_logo_500)
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        PreferencesManager.init(requireContext())

        // Booru URL setting
        val currentBooruUrl = PreferencesManager.booruUrl
        val actionBooruUrl = GuidedAction.Builder(context)
            .id(ACTION_ID_BOORU_URL)
            .title(R.string.setting_booru_url_title)
            .description(currentBooruUrl)
            .editDescription(currentBooruUrl)
            .descriptionEditable(true)
            .build()
        actions.add(actionBooruUrl)

        // Booru Type setting (choice menu with only Danbooru currently)
        val booruTypeSubActions: MutableList<GuidedAction> = mutableListOf();
        val typeDanbooruSubAction = GuidedAction.Builder(context)
            .id(SUBACTION_ID_BOORU_TYPE_DANBOORU)
            .title(R.string.setting_booru_type_subtype_danbooru_title)
            .description(R.string.setting_booru_type_subtype_danbooru_description)
            .build()
        booruTypeSubActions.add(typeDanbooruSubAction)

        val currentBooruType = PreferencesManager.booruType
        val actionBooruType = GuidedAction.Builder(context)
            .id(ACTION_ID_BOORU_TYPE)
            .title(R.string.setting_booru_type_title)
            .description(currentBooruType)
            .subActions(booruTypeSubActions)
            .build()
        actions.add(actionBooruType)

        // Booru search tags setting
        val currentBooruTagSearch = PreferencesManager.booruTagSearch
        val actionBooruTagSearch = GuidedAction.Builder(context)
            .id(ACTION_ID_BOORU_TAG_SEARCH)
            .title(R.string.setting_booru_tag_search_title)
            .description(currentBooruTagSearch)
            .editDescription(currentBooruTagSearch)
            .descriptionEditable(true)
            .build()
        actions.add(actionBooruTagSearch)

        // Divider
        actions.add(GuidedAction.Builder(context)
            .title("")
            .description("")
            .focusable(false)
            .build()
        )

        // User settings information box
        actions.add(GuidedAction.Builder(context)
            .id(ACTION_ID_USER_SETTINGS_LABEL)
            .title(R.string.setting_users_title)
            .description(R.string.setting_users_description)
            .infoOnly(true)
            .focusable(false)
            .build()
        )

        // Booru username setting
        val currentBooruUserId = PreferencesManager.booruUserId
        val actionBooruUserId = GuidedAction.Builder(context)
            .id(ACTION_ID_BOORU_USER_ID)
            .title(R.string.setting_booru_user_id_title)
            .description(currentBooruUserId)
            .editDescription(currentBooruUserId)
            .descriptionEditable(true)
            .build()
        actions.add(actionBooruUserId)

        // Booru API key setting
        val currentBooruApiKey = PreferencesManager.booruApiKey
        val actionBooruApiKey = GuidedAction.Builder(context)
            .id(ACTION_ID_BOORU_API_KEY)
            .title(R.string.setting_booru_api_key_title)
            .description(currentBooruApiKey)
            .editDescription(currentBooruApiKey)
            .descriptionEditable(true)
            .build()
        actions.add(actionBooruApiKey)
    }

    override fun onSubGuidedActionClicked(action: GuidedAction): Boolean {
        when (action.id) {
            SUBACTION_ID_BOORU_TYPE_DANBOORU -> {
                findActionById(ACTION_ID_BOORU_TYPE)?.description = "danbooru"
                notifyActionChanged(findActionPositionById(ACTION_ID_BOORU_TYPE))
                PreferencesManager.booruType = "danbooru"
            }
        }
        return true
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_ID_BOORU_URL -> {
                val params: CharSequence? = action.editDescription
                findActionById(ACTION_ID_BOORU_URL)?.description = params
                notifyActionChanged(findActionPositionById(ACTION_ID_BOORU_URL))
                PreferencesManager.booruUrl = params.toString()
            }
            ACTION_ID_BOORU_TAG_SEARCH -> {
                val params: CharSequence? = action.editDescription
                findActionById(ACTION_ID_BOORU_TAG_SEARCH)?.description = params
                notifyActionChanged(findActionPositionById(ACTION_ID_BOORU_TAG_SEARCH))
                PreferencesManager.booruTagSearch = params.toString()
            }
            ACTION_ID_BOORU_USER_ID -> {
                val params: CharSequence? = action.editDescription
                findActionById(ACTION_ID_BOORU_USER_ID)?.description = params
                notifyActionChanged(findActionPositionById(ACTION_ID_BOORU_USER_ID))
                PreferencesManager.booruUserId = params.toString()
            }
            ACTION_ID_BOORU_API_KEY -> {
                val params: CharSequence? = action.editDescription
                findActionById(ACTION_ID_BOORU_API_KEY)?.description = params
                notifyActionChanged(findActionPositionById(ACTION_ID_BOORU_API_KEY))
                PreferencesManager.booruApiKey = params.toString()
            }
        }
    }

    companion object {
        private const val ACTION_ID_BOORU_URL = 1L
        private const val ACTION_ID_BOORU_TYPE = 2L
        private const val ACTION_ID_BOORU_TAG_SEARCH = 3L
        private const val ACTION_ID_USER_SETTINGS_LABEL = 4L
        private const val ACTION_ID_BOORU_USER_ID = 5L
        private const val ACTION_ID_BOORU_API_KEY = 6L

        private const val SUBACTION_ID_BOORU_TYPE_DANBOORU = 7L
    }
}
