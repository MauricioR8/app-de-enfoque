package com.mauricior8.enfoque.data.model

import android.graphics.drawable.Drawable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/* ----------------------------------------------------------------------------
 * Settings enums
 * ------------------------------------------------------------------------- */

/** How app icons are rendered everywhere (drawer, search, home, folders). */
enum class IconMode {
    /** Original colored icons. */
    COLOR,

    /** Icons forced to monochrome (white on dark backgrounds, black on light). */
    BLACK_AND_WHITE,
}

/* ----------------------------------------------------------------------------
 * Runtime app model (NOT serialized — holds a live Drawable)
 * ------------------------------------------------------------------------- */

/**
 * A launchable item shown in the drawer / home. It can be a real installed app
 * or a web shortcut created from Chrome (treated visually like an app).
 */
data class LaunchableItem(
    val key: String,
    val label: String,
    val icon: Drawable?,
    val type: Type,
    /** For apps. */
    val packageName: String? = null,
    val activityName: String? = null,
    /** For web shortcuts. */
    val url: String? = null,
    val firstInstallTime: Long = 0L,
) {
    enum class Type { APP, WEB_SHORTCUT }

    val sortKey: String get() = label.trim().ifEmpty { "#" }
    val sectionLetter: String
        get() {
            val c = sortKey.first().uppercaseChar()
            return if (c.isLetter()) c.toString() else "#"
        }
}

/* ----------------------------------------------------------------------------
 * Persisted home-screen layout
 * ------------------------------------------------------------------------- */

@Serializable
sealed class HomeEntry {
    abstract val id: String

    @Serializable
    @SerialName("app")
    data class AppRef(
        override val id: String,
        val packageName: String,
        val activityName: String,
    ) : HomeEntry()

    @Serializable
    @SerialName("web")
    data class WebRef(
        override val id: String,
        val label: String,
        val url: String,
    ) : HomeEntry()

    @Serializable
    @SerialName("folder")
    data class FolderRef(
        override val id: String,
    ) : HomeEntry()
}

/**
 * A customizable folder shown on the home screen. Holds up to [MAX_APPS] apps
 * and may have a custom image (content URI string) used as its icon.
 */
@Serializable
data class Folder(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Carpeta",
    /** Keys of contained items (package/activity or web url keys). */
    val itemKeys: List<String> = emptyList(),
    /** Persisted local file path of the cropped custom image, or null. */
    val customImagePath: String? = null,
) {
    companion object {
        const val MAX_APPS = 10
    }
}

/** Which time span the single home-screen progress bar shows ("none" hides it). */
enum class ProgressSpan { NONE, DAY, WEEK, MONTH, YEAR }

@Serializable
data class HomeLayout(
    val entries: List<HomeEntry> = emptyList(),
    val folders: List<Folder> = emptyList(),
    /** Stored as the enum name; defaults to the day progress bar. */
    val homeProgress: String = "DAY",
) {
    val progressSpan: ProgressSpan
        get() = runCatching { ProgressSpan.valueOf(homeProgress) }.getOrDefault(ProgressSpan.DAY)
}

/* ----------------------------------------------------------------------------
 * Oasis widgets
 * ------------------------------------------------------------------------- */

enum class WidgetType {
    TODO,
    NOTES,
    CALENDAR,
    POMODORO,
    TIME_PROGRESS,
    MINI_GAME,
    CUSTOM,
    APP_USAGE,
    QUOTES,
    JOKES,
    MUSIC,
}

@Serializable
data class TriviaItem(
    val id: String = UUID.randomUUID().toString(),
    val question: String = "",
    val answer: String = "",
)

@Serializable
data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val done: Boolean = false,
)

@Serializable
data class NotePage(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
)

@Serializable
data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    /** Epoch day (LocalDate.toEpochDay) the event belongs to. */
    val epochDay: Long = 0L,
    val title: String = "",
    /** Minutes from midnight, or -1 if all-day. */
    val startMinutes: Int = -1,
    val endMinutes: Int = -1,
)

enum class MiniGameType { GAME_2048, SUDOKU, SNAKE, BRICKS, TRIVIA }

/**
 * One configurable card in the Oasis tools screen. [type] decides which payload
 * fields are relevant. This single flexible model keeps persistence simple.
 */
@Serializable
data class WidgetConfig(
    val id: String = UUID.randomUUID().toString(),
    val type: WidgetType,
    val title: String = "",
    val favorite: Boolean = false,

    // TODO payload
    val todos: List<TodoItem> = emptyList(),

    // NOTES payload
    val notes: List<NotePage> = listOf(NotePage()),

    // CALENDAR payload (locally stored events)
    val events: List<CalendarEvent> = emptyList(),

    // POMODORO payload (minutes)
    val pomodoroFocus: Int = 25,
    val pomodoroShortBreak: Int = 5,
    val pomodoroLongBreak: Int = 15,
    val pomodoroAlarm: Boolean = true,

    // TIME_PROGRESS payload: which spans to show
    val showYear: Boolean = true,
    val showMonth: Boolean = true,
    val showWeek: Boolean = true,
    val showDay: Boolean = true,

    // TODO options
    val hideCompleted: Boolean = false,

    // MINI_GAME payload
    val game: MiniGameType = MiniGameType.GAME_2048,
    val trivia: List<TriviaItem> = emptyList(),

    // APP_USAGE payload: window in days (1 = last 24h, 7 = last week)
    val usageWindowDays: Int = 1,

    // QUOTES / JOKES payload (user supplied lines)
    val quotes: List<String> = emptyList(),
    val jokes: List<String> = emptyList(),

    // CUSTOM payload: free text body (e.g. "Ejercicio diario de ajedrez")
    val customBody: String = "",
    val customChecklist: List<TodoItem> = emptyList(),
)

@Serializable
data class OasisBoard(
    val widgets: List<WidgetConfig> = emptyList(),
)
