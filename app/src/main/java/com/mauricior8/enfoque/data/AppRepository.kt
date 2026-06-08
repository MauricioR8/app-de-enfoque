package com.mauricior8.enfoque.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.mauricior8.enfoque.data.model.IconMode
import com.mauricior8.enfoque.data.model.LaunchableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Discovers launchable items: installed apps (via PackageManager) plus web
 * shortcuts (via [WebShortcutStore]). Applies the icon rendering mode so icons
 * look consistent everywhere (drawer, search, home, folders).
 */
class AppRepository(private val context: Context) {

    private val pm: PackageManager get() = context.packageManager
    private val webStore = WebShortcutStore(context)

    /**
     * @param iconMode how to render icons (color or grayscale).
     */
    suspend fun loadItems(iconMode: IconMode): List<LaunchableItem> =
        withContext(Dispatchers.IO) {
            val apps = loadInstalledApps(iconMode)
            val web = loadWebShortcuts(iconMode)
            (apps + web).sortedBy { it.sortKey.lowercase() }
        }

    private fun loadInstalledApps(iconMode: IconMode): List<LaunchableItem> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val myPackage = context.packageName

        return resolveInfos.mapNotNull { info ->
            val activityInfo = info.activityInfo ?: return@mapNotNull null
            val packageName = activityInfo.packageName
            // Hide our own launcher from the drawer.
            if (packageName == myPackage) return@mapNotNull null

            val label = info.loadLabel(pm)?.toString().orEmpty()
            val rawIcon = runCatching { info.loadIcon(pm) }.getOrNull()
            val icon = renderIcon(rawIcon, iconMode)
            val firstInstall = runCatching {
                pm.getPackageInfo(packageName, 0).firstInstallTime
            }.getOrDefault(0L)

            LaunchableItem(
                key = "$packageName/${activityInfo.name}",
                label = label,
                icon = icon,
                type = LaunchableItem.Type.APP,
                packageName = packageName,
                activityName = activityInfo.name,
                firstInstallTime = firstInstall,
            )
        }
    }

    private fun loadWebShortcuts(iconMode: IconMode): List<LaunchableItem> {
        return webStore.all().map { ws ->
            val rawIcon = ws.iconPath?.let { path ->
                runCatching { android.graphics.drawable.Drawable.createFromPath(path) }.getOrNull()
            }
            LaunchableItem(
                key = "web/${ws.id}",
                label = ws.label,
                icon = rawIcon?.let { renderIcon(it, iconMode) },
                type = LaunchableItem.Type.WEB_SHORTCUT,
                url = ws.url,
                firstInstallTime = ws.addedAt,
            )
        }
    }

    private fun renderIcon(
        raw: android.graphics.drawable.Drawable?,
        iconMode: IconMode,
    ): android.graphics.drawable.Drawable? {
        if (raw == null) return null
        return when (iconMode) {
            IconMode.COLOR -> raw
            IconMode.BLACK_AND_WHITE -> IconUtils.toGrayscale(raw, context.resources)
        }
    }

    /** Launches an app by component, or a web shortcut by URL. */
    fun launch(item: LaunchableItem) {
        when (item.type) {
            LaunchableItem.Type.APP -> {
                val pkg = item.packageName ?: return
                val act = item.activityName
                val intent = if (act != null) {
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        component = ComponentName(pkg, act)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                } else {
                    pm.getLaunchIntentForPackage(pkg)?.apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                intent?.let { runCatching { context.startActivity(it) } }
            }

            LaunchableItem.Type.WEB_SHORTCUT -> {
                val url = item.url ?: return
                if (url.startsWith("shortcut://")) {
                    // Pinned shortcut captured from another app (e.g. Chrome).
                    launchPinnedShortcut(url)
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    runCatching { context.startActivity(intent) }
                }
            }
        }
    }

    /** Launches a pinned shortcut encoded as "shortcut://<package>/<id>". */
    private fun launchPinnedShortcut(encoded: String) {
        val body = encoded.removePrefix("shortcut://")
        val slash = body.indexOf('/')
        if (slash <= 0) return
        val pkg = body.substring(0, slash)
        val id = body.substring(slash + 1)
        runCatching {
            val launcherApps = context.getSystemService(android.content.pm.LauncherApps::class.java)
            launcherApps?.startShortcut(pkg, id, null, null, android.os.Process.myUserHandle())
        }
    }

    fun openAppInfo(packageName: String) {
        val intent = Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName"),
        ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        runCatching { context.startActivity(intent) }
    }

    fun requestUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }
}
