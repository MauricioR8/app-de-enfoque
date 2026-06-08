package com.mauricior8.enfoque.ui

import android.app.Application
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mauricior8.enfoque.EnfoqueApp
import com.mauricior8.enfoque.data.AppRepository
import com.mauricior8.enfoque.data.PreferencesManager
import com.mauricior8.enfoque.data.model.Folder
import com.mauricior8.enfoque.data.model.HomeEntry
import com.mauricior8.enfoque.data.model.HomeLayout
import com.mauricior8.enfoque.data.model.IconMode
import com.mauricior8.enfoque.data.model.LaunchableItem
import com.mauricior8.enfoque.data.model.OasisBoard
import com.mauricior8.enfoque.data.model.WidgetConfig
import com.mauricior8.enfoque.ui.theme.BackgroundPalette
import com.mauricior8.enfoque.ui.theme.PureBlack
import com.mauricior8.enfoque.ui.theme.PureWhite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Immutable UI state consumed by the screens. */
data class EnfoqueUiState(
    val iconMode: IconMode = IconMode.COLOR,
    val backgroundColorId: String = "black",
    val launcherName: String = "Enfoque",
    val homeLayout: HomeLayout = HomeLayout(),
    val oasisBoard: OasisBoard = OasisBoard(),
    val clock24h: Boolean = true,
    val showSeconds: Boolean = false,
    val arcBattery: Boolean = true,
    val showRecentApps: Boolean = true,
) {
    val backgroundColor get() = BackgroundPalette.fromId(backgroundColorId).color
    val isDarkBackground get() = backgroundColor.luminance() < 0.5f
    val contentColor get() = if (isDarkBackground) PureWhite else PureBlack
}

class EnfoqueViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as EnfoqueApp).container
    private val prefs: PreferencesManager = container.preferences
    private val appRepository: AppRepository = container.appRepository

    private data class CorePrefs(
        val iconMode: IconMode,
        val bgId: String,
        val name: String,
        val home: HomeLayout,
        val board: OasisBoard,
    )

    private data class DisplayPrefs(
        val clock24h: Boolean,
        val showSeconds: Boolean,
        val arcBattery: Boolean,
        val showRecent: Boolean,
    )

    private val coreFlow = combine(
        prefs.iconMode,
        prefs.backgroundColorId,
        prefs.launcherName,
        prefs.homeLayout,
        prefs.oasisBoard,
    ) { iconMode, bgId, name, home, board -> CorePrefs(iconMode, bgId, name, home, board) }

    private val displayFlow = combine(
        prefs.clock24h,
        prefs.showSeconds,
        prefs.arcBattery,
        prefs.showRecentApps,
    ) { c24, secs, arc, recent -> DisplayPrefs(c24, secs, arc, recent) }

    val uiState: StateFlow<EnfoqueUiState> = combine(coreFlow, displayFlow) { core, display ->
        EnfoqueUiState(
            iconMode = core.iconMode,
            backgroundColorId = core.bgId,
            launcherName = core.name,
            homeLayout = core.home,
            oasisBoard = core.board,
            clock24h = display.clock24h,
            showSeconds = display.showSeconds,
            arcBattery = display.arcBattery,
            showRecentApps = display.showRecent,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EnfoqueUiState())

    /** Loaded launchable items (apps + web shortcuts), re-rendered for current theme. */
    private val _items = MutableStateFlow<List<LaunchableItem>>(emptyList())
    val items: StateFlow<List<LaunchableItem>> = _items

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    /** Reloads the app/shortcut list using the current icon mode. */
    fun reloadItems(iconMode: IconMode) {
        viewModelScope.launch {
            _loading.value = true
            _items.value = appRepository.loadItems(iconMode)
            _loading.value = false
        }
    }

    fun launch(item: LaunchableItem) = appRepository.launch(item)
    fun openAppInfo(pkg: String) = appRepository.openAppInfo(pkg)
    fun requestUninstall(pkg: String) = appRepository.requestUninstall(pkg)

    /* ----------------------- Settings actions ----------------------- */

    fun setIconMode(mode: IconMode) = viewModelScope.launch { prefs.setIconMode(mode) }
    fun setBackgroundColorId(id: String) = viewModelScope.launch { prefs.setBackgroundColorId(id) }
    fun setLauncherName(name: String) = viewModelScope.launch { prefs.setLauncherName(name) }
    fun setClock24h(value: Boolean) = viewModelScope.launch { prefs.setClock24h(value) }
    fun setShowSeconds(value: Boolean) = viewModelScope.launch { prefs.setShowSeconds(value) }
    fun setArcBattery(value: Boolean) = viewModelScope.launch { prefs.setArcBattery(value) }
    fun setShowRecentApps(value: Boolean) = viewModelScope.launch { prefs.setShowRecentApps(value) }

    /* ----------------------- Home layout actions ----------------------- */

    fun updateHomeLayout(layout: HomeLayout) = viewModelScope.launch { prefs.setHomeLayout(layout) }

    fun addEntryToHome(entry: HomeEntry) {
        val current = uiState.value.homeLayout
        if (current.entries.any { it.id == entry.id }) return
        updateHomeLayout(current.copy(entries = current.entries + entry))
    }

    fun removeEntryFromHome(entryId: String) {
        val current = uiState.value.homeLayout
        updateHomeLayout(current.copy(entries = current.entries.filterNot { it.id == entryId }))
    }

    fun upsertFolder(folder: Folder) {
        val current = uiState.value.homeLayout
        val folders = current.folders.toMutableList()
        val index = folders.indexOfFirst { it.id == folder.id }
        if (index >= 0) folders[index] = folder else folders += folder

        // Make sure there is a home entry referencing this folder.
        val entries = current.entries.toMutableList()
        if (entries.none { it is HomeEntry.FolderRef && it.id == folder.id }) {
            entries += HomeEntry.FolderRef(folder.id)
        }
        updateHomeLayout(current.copy(entries = entries, folders = folders))
    }

    fun deleteFolder(folderId: String) {
        val current = uiState.value.homeLayout
        updateHomeLayout(
            current.copy(
                folders = current.folders.filterNot { it.id == folderId },
                entries = current.entries.filterNot { it is HomeEntry.FolderRef && it.id == folderId },
            )
        )
    }

    /* ----------------------- Oasis board actions ----------------------- */

    fun updateOasisBoard(board: OasisBoard) = viewModelScope.launch { prefs.setOasisBoard(board) }

    fun upsertWidget(widget: WidgetConfig) {
        val current = uiState.value.oasisBoard
        val widgets = current.widgets.toMutableList()
        val index = widgets.indexOfFirst { it.id == widget.id }
        if (index >= 0) widgets[index] = widget else widgets += widget
        updateOasisBoard(current.copy(widgets = widgets))
    }

    fun removeWidget(widgetId: String) {
        val current = uiState.value.oasisBoard
        updateOasisBoard(current.copy(widgets = current.widgets.filterNot { it.id == widgetId }))
    }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return EnfoqueViewModel(app) as T
            }
        }
    }
}
