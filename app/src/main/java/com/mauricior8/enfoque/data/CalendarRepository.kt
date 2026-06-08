package com.mauricior8.enfoque.data

import android.content.Context
import android.provider.CalendarContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A read-only event coming from the device calendar (Google, Samsung, etc.). */
data class DeviceCalendarEvent(
    val title: String,
    val beginMs: Long,
    val endMs: Long,
    val allDay: Boolean,
)

/**
 * Reads events from the device calendar provider for a given day. This surfaces
 * events from any synced calendar account (Google Calendar included). Requires
 * the READ_CALENDAR runtime permission; without it, returns an empty list.
 */
class CalendarRepository(private val context: Context) {

    suspend fun eventsForDay(dayStartMs: Long, dayEndMs: Long): List<DeviceCalendarEvent> =
        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY,
            )
            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            android.content.ContentUris.appendId(builder, dayStartMs)
            android.content.ContentUris.appendId(builder, dayEndMs)

            val result = mutableListOf<DeviceCalendarEvent>()
            try {
                context.contentResolver.query(
                    builder.build(),
                    projection,
                    null,
                    null,
                    "${CalendarContract.Instances.BEGIN} ASC",
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val title = cursor.getString(0) ?: "(sin título)"
                        val begin = cursor.getLong(1)
                        val end = cursor.getLong(2)
                        val allDay = cursor.getInt(3) == 1
                        result += DeviceCalendarEvent(title, begin, end, allDay)
                    }
                }
            } catch (e: SecurityException) {
                // Permission not granted yet.
            } catch (e: Exception) {
                // Provider unavailable — ignore.
            }
            result
        }
}
