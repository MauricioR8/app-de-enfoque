package com.mauricior8.enfoque.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One app's usage over the selected window. */
data class AppUsage(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val totalTimeMs: Long,
    val openCount: Int,
)

/**
 * Reads per-app screen time and open counts using [UsageStatsManager]. Requires
 * the user to grant "Usage access" (PACKAGE_USAGE_STATS), which cannot be granted
 * by a normal runtime permission dialog — it opens a system settings screen.
 */
class UsageStatsRepository(private val context: Context) {

    /** True if the user granted usage-access to this app. */
    fun hasUsageAccess(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName,
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName,
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * @param windowDays 1 for the last 24h, 7 for the last week.
     * @return apps sorted by screen time descending (only those actually used).
     */
    suspend fun loadUsage(windowDays: Int): List<AppUsage> = withContext(Dispatchers.IO) {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return@withContext emptyList()
        val pm = context.packageManager
        val end = System.currentTimeMillis()
        val begin = end - windowDays * 24L * 60L * 60L * 1000L

        // Aggregate foreground time per package.
        val aggregate = usm.queryAndAggregateUsageStats(begin, end)
        val timeByPkg = HashMap<String, Long>()
        for ((pkg, stats) in aggregate) {
            val t = stats.totalTimeInForeground
            if (t > 0) timeByPkg[pkg] = (timeByPkg[pkg] ?: 0L) + t
        }

        // Count app launches (foreground transitions) from raw events.
        val openByPkg = HashMap<String, Int>()
        val events = usm.queryEvents(begin, end)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                openByPkg[event.packageName] = (openByPkg[event.packageName] ?: 0) + 1
            }
        }

        val myPackage = context.packageName
        timeByPkg.entries
            .filter { it.key != myPackage }
            .mapNotNull { (pkg, time) ->
                val label = runCatching {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                }.getOrNull() ?: return@mapNotNull null
                val icon = runCatching { pm.getApplicationIcon(pkg) }.getOrNull()
                AppUsage(
                    packageName = pkg,
                    label = label,
                    icon = icon,
                    totalTimeMs = time,
                    openCount = openByPkg[pkg] ?: 0,
                )
            }
            .sortedByDescending { it.totalTimeMs }
            .take(15)
    }
}
