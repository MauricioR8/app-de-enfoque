package com.mauricior8.enfoque.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mauricior8.enfoque.data.model.HomeLayout
import com.mauricior8.enfoque.data.model.IconMode
import com.mauricior8.enfoque.data.model.OasisBoard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "enfoque_prefs")

/**
 * Single source of truth for persisted settings and layouts. Everything is
 * stored in one Preferences DataStore; complex structures are JSON-encoded.
 */
class PreferencesManager(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private object Keys {
        val ICON_MODE = stringPreferencesKey("icon_mode")
        val BACKGROUND_COLOR = stringPreferencesKey("background_color")
        val LAUNCHER_NAME = stringPreferencesKey("launcher_name")
        val HOME_LAYOUT = stringPreferencesKey("home_layout_json")
        val OASIS_BOARD = stringPreferencesKey("oasis_board_json")
        val CLOCK_24H = booleanPreferencesKey("clock_24h")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val ARC_BATTERY = booleanPreferencesKey("arc_battery")
        val SHOW_RECENT = booleanPreferencesKey("show_recent")
        val CUSTOM_ICONS = stringPreferencesKey("custom_icons_json")
    }

    /* ------------------------- Custom icons ------------------------- */

    /** Map of item key (package/activity or web/id) -> local image file path. */
    val customIcons: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.CUSTOM_ICONS]?.let {
            runCatching { json.decodeFromString<Map<String, String>>(it) }.getOrNull()
        } ?: emptyMap()
    }

    suspend fun setCustomIcon(key: String, path: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CUSTOM_ICONS]?.let {
                runCatching { json.decodeFromString<Map<String, String>>(it) }.getOrNull()
            } ?: emptyMap()
            prefs[Keys.CUSTOM_ICONS] = json.encodeToString(current + (key to path))
        }
    }

    suspend fun removeCustomIcon(key: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CUSTOM_ICONS]?.let {
                runCatching { json.decodeFromString<Map<String, String>>(it) }.getOrNull()
            } ?: emptyMap()
            prefs[Keys.CUSTOM_ICONS] = json.encodeToString(current - key)
        }
    }

    /* ------------------------- Icon mode ------------------------- */

    val iconMode: Flow<IconMode> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.ICON_MODE]) {
            IconMode.BLACK_AND_WHITE.name -> IconMode.BLACK_AND_WHITE
            else -> IconMode.COLOR
        }
    }

    suspend fun setIconMode(mode: IconMode) {
        context.dataStore.edit { it[Keys.ICON_MODE] = mode.name }
    }

    /* ------------------------- Background color ------------------------- */

    val backgroundColorId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.BACKGROUND_COLOR] ?: "black"
    }

    suspend fun setBackgroundColorId(id: String) {
        context.dataStore.edit { it[Keys.BACKGROUND_COLOR] = id }
    }

    /* ------------------------- Launcher name ------------------------- */

    val launcherName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAUNCHER_NAME] ?: "Enfoque"
    }

    suspend fun setLauncherName(name: String) {
        context.dataStore.edit { it[Keys.LAUNCHER_NAME] = name }
    }

    /* ------------------------- Clock & display settings ------------------------- */

    val clock24h: Flow<Boolean> = context.dataStore.data.map { it[Keys.CLOCK_24H] ?: true }
    suspend fun setClock24h(value: Boolean) {
        context.dataStore.edit { it[Keys.CLOCK_24H] = value }
    }

    val showSeconds: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_SECONDS] ?: false }
    suspend fun setShowSeconds(value: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_SECONDS] = value }
    }

    /** When true the clock arc represents the battery level (depletes as it drops). */
    val arcBattery: Flow<Boolean> = context.dataStore.data.map { it[Keys.ARC_BATTERY] ?: true }
    suspend fun setArcBattery(value: Boolean) {
        context.dataStore.edit { it[Keys.ARC_BATTERY] = value }
    }

    val showRecentApps: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_RECENT] ?: true }
    suspend fun setShowRecentApps(value: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_RECENT] = value }
    }

    /* ------------------------- Home layout ------------------------- */

    val homeLayout: Flow<HomeLayout> = context.dataStore.data.map { prefs ->
        prefs[Keys.HOME_LAYOUT]?.let { runCatching { json.decodeFromString<HomeLayout>(it) }.getOrNull() }
            ?: HomeLayout()
    }

    suspend fun setHomeLayout(layout: HomeLayout) {
        context.dataStore.edit { it[Keys.HOME_LAYOUT] = json.encodeToString(layout) }
    }

    /* ------------------------- Oasis board ------------------------- */

    val oasisBoard: Flow<OasisBoard> = context.dataStore.data.map { prefs ->
        prefs[Keys.OASIS_BOARD]?.let { runCatching { json.decodeFromString<OasisBoard>(it) }.getOrNull() }
            ?: defaultBoard()
    }

    suspend fun setOasisBoard(board: OasisBoard) {
        context.dataStore.edit { it[Keys.OASIS_BOARD] = json.encodeToString(board) }
    }

    private fun defaultBoard(): OasisBoard = OasisBoard(
        widgets = listOf(
            com.mauricior8.enfoque.data.model.WidgetConfig(
                type = com.mauricior8.enfoque.data.model.WidgetType.TODO,
                title = "Por hacer",
            ),
            com.mauricior8.enfoque.data.model.WidgetConfig(
                type = com.mauricior8.enfoque.data.model.WidgetType.NOTES,
                title = "Notas",
            ),
            com.mauricior8.enfoque.data.model.WidgetConfig(
                type = com.mauricior8.enfoque.data.model.WidgetType.POMODORO,
                title = "Temporizador Pomodoro",
            ),
            com.mauricior8.enfoque.data.model.WidgetConfig(
                type = com.mauricior8.enfoque.data.model.WidgetType.TIME_PROGRESS,
                title = "Progreso del Tiempo",
            ),
        )
    )
}
