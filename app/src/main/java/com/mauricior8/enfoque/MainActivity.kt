package com.mauricior8.enfoque

import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.mauricior8.enfoque.data.ImageStorage
import com.mauricior8.enfoque.data.WebShortcut
import com.mauricior8.enfoque.data.model.Folder
import com.mauricior8.enfoque.data.model.HomeEntry
import com.mauricior8.enfoque.data.model.LaunchableItem
import com.mauricior8.enfoque.data.model.WidgetConfig
import com.mauricior8.enfoque.data.model.WidgetType
import com.mauricior8.enfoque.ui.EnfoqueViewModel
import com.mauricior8.enfoque.ui.drawer.AppDrawerScreen
import com.mauricior8.enfoque.ui.drawer.DrawerItemAction
import com.mauricior8.enfoque.ui.folder.FolderEditorDialog
import com.mauricior8.enfoque.ui.folder.FolderViewDialog
import com.mauricior8.enfoque.ui.home.HomeScreen
import com.mauricior8.enfoque.ui.oasis.NoteFullScreen
import com.mauricior8.enfoque.ui.oasis.OasisScreen
import com.mauricior8.enfoque.ui.settings.ClockSettingsDialog
import com.mauricior8.enfoque.ui.settings.SettingsScreen
import com.mauricior8.enfoque.ui.theme.AppDeEnfoqueTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val viewModel: EnfoqueViewModel by viewModels { EnfoqueViewModel.factory(application) }

    private val container get() = (application as EnfoqueApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        handlePinShortcut(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val items by viewModel.items.collectAsState()

            // Re-render icons whenever the icon mode or custom icons change.
            LaunchedEffect(uiState.iconMode, uiState.customIcons) {
                viewModel.reloadItems(uiState.iconMode, uiState.customIcons)
            }

            AppDeEnfoqueTheme(backgroundColor = uiState.backgroundColor) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(uiState.backgroundColor)
                ) {
                    EnfoqueRoot(
                        uiState = uiState,
                        items = items,
                        onLaunch = viewModel::launch,
                        onItemAction = ::handleDrawerAction,
                        viewModel = viewModel,
                        onOpenPhone = ::openPhone,
                        onOpenCamera = ::openCamera,
                        onSetDefaultLauncher = ::openHomeSettings,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePinShortcut(intent)
    }

    /** Accepts pinned shortcuts (e.g. Chrome web shortcuts) and stores them. */
    private fun handlePinShortcut(intent: Intent?) {
        if (intent == null) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val launcherApps = getSystemService(LauncherApps::class.java) ?: return
        val request = runCatching { launcherApps.getPinItemRequest(intent) }.getOrNull() ?: return
        if (request.requestType != LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) return
        if (!request.isValid) return
        val info = request.shortcutInfo ?: return
        val label = (info.longLabel ?: info.shortLabel ?: "Acceso directo").toString()
        request.accept()
        container.webShortcutStore.add(
            WebShortcut(label = label, url = "shortcut://${info.`package`}/${info.id}")
        )
        viewModel.reloadItems(viewModel.uiState.value.iconMode)
    }

    private fun handleDrawerAction(item: LaunchableItem, action: DrawerItemAction) {
        when (action) {
            DrawerItemAction.ADD_TO_HOME -> viewModel.addEntryToHome(item.toHomeEntry())
            DrawerItemAction.APP_INFO -> item.packageName?.let(viewModel::openAppInfo)
            DrawerItemAction.UNINSTALL -> item.packageName?.let(viewModel::requestUninstall)
            DrawerItemAction.CHANGE_ICON -> pendingIconKey = item.key
            DrawerItemAction.RESET_ICON -> viewModel.removeCustomIcon(item.key)
            DrawerItemAction.REMOVE_SHORTCUT -> {
                item.url?.let { url ->
                    container.webShortcutStore.all().firstOrNull { it.url == url }?.let {
                        container.webShortcutStore.remove(it.id)
                    }
                    viewModel.reloadItems(viewModel.uiState.value.iconMode)
                }
            }
            DrawerItemAction.ADD_TO_FOLDER -> {
                // Handled in Compose layer (opens the folder editor preselecting this item).
                pendingFolderItemKey = item.key
            }
        }
    }

    // Bridges for requests coming from the drawer menu.
    var pendingFolderItemKey: String? by mutableStateOf(null)
    var pendingIconKey: String? by mutableStateOf(null)

    private fun openPhone() {
        runCatching {
            startActivity(Intent(Intent.ACTION_DIAL).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }

    private fun openCamera() {
        runCatching {
            startActivity(
                Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        }
    }

    private fun openHomeSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent(Settings.ACTION_HOME_SETTINGS)
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }
        runCatching { startActivity(intent) }
    }

    /** Opens the default clock app (so the user can set time/alarms). */
    fun openClockApp() {
        val showAlarms = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (showAlarms.resolveActivity(packageManager) != null) {
            runCatching { startActivity(showAlarms) }
        } else {
            runCatching { startActivity(Intent(Settings.ACTION_DATE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) }
        }
    }

    /** Opens the calendar app at today's date. */
    fun openCalendarApp() {
        val builder = android.provider.CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
        android.content.ContentUris.appendId(builder, System.currentTimeMillis())
        val intent = Intent(Intent.ACTION_VIEW, builder.build()).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        if (intent.resolveActivity(packageManager) != null) {
            runCatching { startActivity(intent) }
        } else {
            runCatching { startActivity(Intent(Settings.ACTION_DATE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) }
        }
    }
}

private fun LaunchableItem.toHomeEntry(): HomeEntry = when (type) {
    LaunchableItem.Type.APP -> HomeEntry.AppRef(
        id = UUID.randomUUID().toString(),
        packageName = packageName ?: "",
        activityName = activityName ?: "",
    )
    LaunchableItem.Type.WEB_SHORTCUT -> HomeEntry.WebRef(
        id = UUID.randomUUID().toString(),
        label = label,
        url = url ?: "",
    )
}

/* ------------------------------------------------------------------------- */

private sealed class Overlay {
    data object None : Overlay()
    data object Settings : Overlay()
    data object ClockSettings : Overlay()
    data object IconEditor : Overlay()
    data class FolderEditor(val folder: Folder?) : Overlay()
    data class FolderView(val folderId: String) : Overlay()
    data class Note(val widgetId: String, val page: Int) : Overlay()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EnfoqueRoot(
    uiState: com.mauricior8.enfoque.ui.EnfoqueUiState,
    items: List<LaunchableItem>,
    onLaunch: (LaunchableItem) -> Unit,
    onItemAction: (LaunchableItem, DrawerItemAction) -> Unit,
    viewModel: EnfoqueViewModel,
    onOpenPhone: () -> Unit,
    onOpenCamera: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
) {
    val activity = androidx.compose.ui.platform.LocalContext.current as MainActivity
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var overlay by remember { mutableStateOf<Overlay>(Overlay.None) }
    var pendingImagePath by remember { mutableStateOf<String?>(null) }

    // Custom icon editing state.
    var iconKeyForEdit by remember { mutableStateOf<String?>(null) }
    var pendingIconUri by remember { mutableStateOf<Uri?>(null) }

    // React to "add to folder" requests from the drawer menu.
    LaunchedEffect(activity.pendingFolderItemKey) {
        val key = activity.pendingFolderItemKey
        if (key != null) {
            overlay = Overlay.FolderEditor(Folder(itemKeys = listOf(key)))
            activity.pendingFolderItemKey = null
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) {
                    ImageStorage.saveCroppedSquare(activity, uri, UUID.randomUUID().toString())
                }
                pendingImagePath = path
            }
        }
    }

    // Picker used for custom app icons (opens the mini editor afterwards).
    val iconImagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingIconUri = uri
            overlay = Overlay.IconEditor
        } else {
            iconKeyForEdit = null
        }
    }

    val startIconChange: (String) -> Unit = { key ->
        iconKeyForEdit = key
        iconImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // React to "change icon" requests from the drawer menu.
    LaunchedEffect(activity.pendingIconKey) {
        val key = activity.pendingIconKey
        if (key != null) {
            activity.pendingIconKey = null
            startIconChange(key)
        }
    }

    val pagerState = rememberPagerState(initialPage = 1) { 3 }

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        when (page) {
            0 -> OasisScreen(
                board = uiState.oasisBoard,
                onWidgetChange = viewModel::upsertWidget,
                onWidgetRemove = viewModel::removeWidget,
                onAddWidget = { type -> viewModel.addWidget(type, defaultTitle(type)) },
                onExpandNote = { config, pageIndex -> overlay = Overlay.Note(config.id, pageIndex) },
                onPin = { id -> viewModel.toggleWidgetPin(id) },
                onMove = { id, dir -> viewModel.moveWidget(id, dir) },
                canAdd = { type -> viewModel.canAddWidget(type) },
            )
            1 -> HomeScreen(
                launcherName = uiState.launcherName,
                layout = uiState.homeLayout,
                items = items,
                clock24h = uiState.clock24h,
                showSeconds = uiState.showSeconds,
                arcBattery = uiState.arcBattery,
                arcColorByBattery = uiState.arcBatteryColor,
                onItemClick = onLaunch,
                onFolderOpen = { folder -> overlay = Overlay.FolderView(folder.id) },
                onFolderEdit = { folder -> overlay = Overlay.FolderEditor(folder) },
                onRemoveFromHome = { entryId -> viewModel.removeEntryFromHome(entryId) },
                onUninstall = { pkg -> viewModel.requestUninstall(pkg) },
                onAppInfo = { pkg -> viewModel.openAppInfo(pkg) },
                onChangeIcon = { key -> startIconChange(key) },
                onResetIcon = { key -> viewModel.removeCustomIcon(key) },
                onClickTime = { activity.openClockApp() },
                onClickDate = { activity.openCalendarApp() },
                onClockLongPress = { overlay = Overlay.ClockSettings },
                onSetProgress = { span -> viewModel.setHomeProgress(span) },
                onOpenPhone = onOpenPhone,
                onOpenCamera = onOpenCamera,
            )
            2 -> AppDrawerScreen(
                items = items,
                onItemClick = onLaunch,
                onItemAction = onItemAction,
                onOpenSettings = { overlay = Overlay.Settings },
            )
        }
    }

    // Floating "new folder" affordance is reachable by long-pressing the home,
    // but we also expose it through the drawer's "add to folder". When the home
    // is empty users can create one from a long press handled above.

    when (val current = overlay) {
        Overlay.None -> Unit

        Overlay.IconEditor -> {
            val uri = pendingIconUri
            val key = iconKeyForEdit
            if (uri == null || key == null) {
                overlay = Overlay.None
            } else {
                com.mauricior8.enfoque.ui.components.IconEditorDialog(
                    imageUri = uri,
                    onConfirm = { boxPx, scale, tx, ty ->
                        scope.launch {
                            val path = withContext(Dispatchers.IO) {
                                ImageStorage.saveAdjusted(activity, uri, boxPx, scale, tx, ty, UUID.randomUUID().toString())
                            }
                            if (path != null) viewModel.setCustomIcon(key, path)
                            pendingIconUri = null
                            iconKeyForEdit = null
                            overlay = Overlay.None
                        }
                    },
                    onCancel = {
                        pendingIconUri = null
                        iconKeyForEdit = null
                        overlay = Overlay.None
                    },
                )
            }
        }

        Overlay.Settings -> FullScreenOverlay(uiState) {
            SettingsScreen(
                iconMode = uiState.iconMode,
                backgroundColorId = uiState.backgroundColorId,
                launcherName = uiState.launcherName,
                clock24h = uiState.clock24h,
                showSeconds = uiState.showSeconds,
                arcBattery = uiState.arcBattery,
                arcBatteryColor = uiState.arcBatteryColor,
                showRecentApps = uiState.showRecentApps,
                homeProgress = uiState.homeLayout.progressSpan,
                onIconModeChange = viewModel::setIconMode,
                onBackgroundColorChange = viewModel::setBackgroundColorId,
                onLauncherNameChange = viewModel::setLauncherName,
                onClock24hChange = viewModel::setClock24h,
                onShowSecondsChange = viewModel::setShowSeconds,
                onArcBatteryChange = viewModel::setArcBattery,
                onArcBatteryColorChange = viewModel::setArcBatteryColor,
                onShowRecentAppsChange = viewModel::setShowRecentApps,
                onHomeProgressChange = { span -> viewModel.setHomeProgress(span) },
                onSetDefaultLauncher = onSetDefaultLauncher,
                onBack = { overlay = Overlay.None },
            )
        }

        Overlay.ClockSettings -> ClockSettingsDialog(
            clock24h = uiState.clock24h,
            showSeconds = uiState.showSeconds,
            arcBattery = uiState.arcBattery,
            onClock24hChange = viewModel::setClock24h,
            onShowSecondsChange = viewModel::setShowSeconds,
            onArcBatteryChange = viewModel::setArcBattery,
            onMoreSettings = { overlay = Overlay.Settings },
            onDismiss = { overlay = Overlay.None },
        )

        is Overlay.FolderView -> {
            val folder = uiState.homeLayout.folders.firstOrNull { it.id == current.folderId }
            if (folder == null) {
                overlay = Overlay.None
            } else {
                FolderViewDialog(
                    folder = folder,
                    items = items,
                    onLaunch = onLaunch,
                    onEdit = { overlay = Overlay.FolderEditor(folder) },
                    onDismiss = { overlay = Overlay.None },
                )
            }
        }

        is Overlay.FolderEditor -> {
            val existing = current.folder
            FolderEditorDialog(
                initial = existing ?: Folder(),
                allItems = items,
                pendingImagePath = pendingImagePath,
                onPickImage = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onSave = { folder ->
                    viewModel.upsertFolder(folder)
                    pendingImagePath = null
                    overlay = Overlay.None
                },
                onDelete = if (existing != null && uiState.homeLayout.folders.any { it.id == existing.id }) {
                    {
                        ImageStorage.delete(existing.customImagePath)
                        viewModel.deleteFolder(existing.id)
                        pendingImagePath = null
                        overlay = Overlay.None
                    }
                } else null,
                onDismiss = {
                    pendingImagePath = null
                    overlay = Overlay.None
                },
            )
        }

        is Overlay.Note -> {
            val widget = uiState.oasisBoard.widgets.firstOrNull { it.id == current.widgetId }
            if (widget == null) {
                overlay = Overlay.None
            } else {
                val notes = widget.notes
                val safePage = current.page.coerceIn(0, (notes.size - 1).coerceAtLeast(0))
                val text = notes.getOrNull(safePage)?.text ?: ""
                FullScreenOverlay(uiState) {
                    NoteFullScreen(
                        text = text,
                        onTextChange = { newText ->
                            val updated = notes.toMutableList()
                            if (updated.isNotEmpty()) {
                                updated[safePage] = updated[safePage].copy(text = newText)
                                viewModel.upsertWidget(widget.copy(notes = updated))
                            }
                        },
                        onBack = { overlay = Overlay.None },
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenOverlay(
    uiState: com.mauricior8.enfoque.ui.EnfoqueUiState,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(uiState.backgroundColor)
    ) {
        content()
    }
}

private fun defaultTitle(type: WidgetType): String = when (type) {
    WidgetType.TODO -> "Por hacer"
    WidgetType.NOTES -> "Notas"
    WidgetType.CALENDAR -> "Calendario"
    WidgetType.POMODORO -> "Temporizador Pomodoro"
    WidgetType.TIME_PROGRESS -> "Progreso del Tiempo"
    WidgetType.MINI_GAME -> "Minijuegos"
    WidgetType.APP_USAGE -> "Uso de la App"
    WidgetType.MUSIC -> "Música"
    WidgetType.QUOTES -> "Frases"
    WidgetType.JOKES -> "Chistes"
    WidgetType.CUSTOM -> ""
}
