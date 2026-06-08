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
import com.mauricior8.enfoque.ui.home.HomeScreen
import com.mauricior8.enfoque.ui.oasis.NoteFullScreen
import com.mauricior8.enfoque.ui.oasis.OasisScreen
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

            // Re-render icons whenever the icon mode or background changes.
            LaunchedEffect(uiState.iconMode, uiState.backgroundColorId) {
                viewModel.reloadItems(uiState.iconMode, uiState.backgroundColorId)
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
        viewModel.reloadItems(viewModel.uiState.value.iconMode, viewModel.uiState.value.backgroundColorId)
    }

    private fun handleDrawerAction(item: LaunchableItem, action: DrawerItemAction) {
        when (action) {
            DrawerItemAction.ADD_TO_HOME -> viewModel.addEntryToHome(item.toHomeEntry())
            DrawerItemAction.APP_INFO -> item.packageName?.let(viewModel::openAppInfo)
            DrawerItemAction.UNINSTALL -> item.packageName?.let(viewModel::requestUninstall)
            DrawerItemAction.REMOVE_SHORTCUT -> {
                item.url?.let { url ->
                    container.webShortcutStore.all().firstOrNull { it.url == url }?.let {
                        container.webShortcutStore.remove(it.id)
                    }
                    viewModel.reloadItems(viewModel.uiState.value.iconMode, viewModel.uiState.value.backgroundColorId)
                }
            }
            DrawerItemAction.ADD_TO_FOLDER -> {
                // Handled in Compose layer (opens the folder editor preselecting this item).
                pendingFolderItemKey = item.key
            }
        }
    }

    // Bridge for "add to folder" requests coming from the drawer menu.
    var pendingFolderItemKey: String? by mutableStateOf(null)

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
    data class FolderEditor(val folder: Folder?) : Overlay()
    data class Note(val widgetId: String, val page: Int) : Overlay()
}

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

    val pagerState = rememberPagerState(initialPage = 1) { 3 }

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        when (page) {
            0 -> OasisScreen(
                board = uiState.oasisBoard,
                onWidgetChange = viewModel::upsertWidget,
                onWidgetRemove = viewModel::removeWidget,
                onAddWidget = { type -> viewModel.upsertWidget(WidgetConfig(type = type, title = defaultTitle(type))) },
                onExpandNote = { config, pageIndex -> overlay = Overlay.Note(config.id, pageIndex) },
            )
            1 -> HomeScreen(
                launcherName = uiState.launcherName,
                layout = uiState.homeLayout,
                items = items,
                onItemClick = onLaunch,
                onFolderClick = { folder -> overlay = Overlay.FolderEditor(folder) },
                onItemLongClick = { entry ->
                    if (entry is HomeEntry.FolderRef) {
                        uiState.homeLayout.folders.firstOrNull { it.id == entry.id }?.let {
                            overlay = Overlay.FolderEditor(it)
                        }
                    } else {
                        viewModel.removeEntryFromHome(entry.id)
                    }
                },
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

        Overlay.Settings -> FullScreenOverlay(uiState) {
            SettingsScreen(
                iconMode = uiState.iconMode,
                backgroundColorId = uiState.backgroundColorId,
                launcherName = uiState.launcherName,
                onIconModeChange = viewModel::setIconMode,
                onBackgroundColorChange = viewModel::setBackgroundColorId,
                onLauncherNameChange = viewModel::setLauncherName,
                onSetDefaultLauncher = onSetDefaultLauncher,
                onBack = { overlay = Overlay.None },
            )
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
    WidgetType.MINI_GAME -> "Mini-juego"
    WidgetType.CUSTOM -> ""
}
