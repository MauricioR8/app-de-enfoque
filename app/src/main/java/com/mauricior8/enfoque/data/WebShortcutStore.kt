package com.mauricior8.enfoque.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * A web shortcut (e.g. created from Chrome's "Add to Home screen"). The launcher
 * recognises these and shows them next to normal apps.
 */
@Serializable
data class WebShortcut(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val url: String,
    /** Optional local path to a saved favicon/icon image. */
    val iconPath: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
)

/**
 * Lightweight persistence for web shortcuts using SharedPreferences. Kept
 * separate from app discovery so Chrome-added shortcuts survive restarts.
 */
class WebShortcutStore(context: Context) {

    private val prefs = context.getSharedPreferences("web_shortcuts", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun all(): List<WebShortcut> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<WebShortcut>>(raw) }.getOrDefault(emptyList())
    }

    fun add(shortcut: WebShortcut) {
        val current = all().toMutableList()
        if (current.none { it.url == shortcut.url }) {
            current += shortcut
            persist(current)
        }
    }

    fun remove(id: String) {
        persist(all().filterNot { it.id == id })
    }

    private fun persist(list: List<WebShortcut>) {
        prefs.edit().putString(KEY, json.encodeToString(list)).apply()
    }

    private companion object {
        const val KEY = "shortcuts_json"
    }
}
