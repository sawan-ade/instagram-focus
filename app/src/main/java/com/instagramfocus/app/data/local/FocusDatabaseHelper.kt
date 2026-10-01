package com.instagramfocus.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.instagramfocus.app.domain.model.BlockEvent
import com.instagramfocus.app.domain.model.ScreenType
import com.instagramfocus.app.domain.model.StatisticsSummary
import java.util.Calendar

/**
 * 100% On-Device local SQLite storage for distraction block events and session counters.
 * Zero external transmission, zero cloud, private by design.
 */
class FocusDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "instagram_focus.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_BLOCK_EVENTS = "block_events"
        const val COL_ID = "id"
        const val COL_SCREEN_TYPE = "screen_type"
        const val COL_TIMESTAMP = "timestamp"
        const val COL_REASON = "reason"
        const val COL_RULE = "matched_rule"

        const val TABLE_SESSIONS = "sessions"
        const val COL_SESSION_ID = "session_id"
        const val COL_SESSION_TYPE = "session_type" // "INSTAGRAM" or "DM"
        const val COL_SESSION_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_BLOCK_EVENTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SCREEN_TYPE TEXT NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL,
                $COL_REASON TEXT,
                $COL_RULE TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_SESSIONS (
                $COL_SESSION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SESSION_TYPE TEXT NOT NULL,
                $COL_SESSION_TIMESTAMP INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BLOCK_EVENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSIONS")
        onCreate(db)
    }

    fun recordBlockEvent(event: BlockEvent): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SCREEN_TYPE, event.screenType.name)
            put(COL_TIMESTAMP, event.timestamp)
            put(COL_REASON, event.reason)
            put(COL_RULE, event.matchedRule)
        }
        return db.insert(TABLE_BLOCK_EVENTS, null, values)
    }

    fun recordSession(type: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SESSION_TYPE, type)
            put(COL_SESSION_TIMESTAMP, System.currentTimeMillis())
        }
        db.insert(TABLE_SESSIONS, null, values)
    }

    fun getStatisticsSummary(): StatisticsSummary {
        val db = readableDatabase
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, -7)
        val startOfWeek = cal.timeInMillis

        var reelsToday = 0
        var exploreToday = 0
        var feedToday = 0
        var totalToday = 0
        var weeklyTotal = 0

        // Query Today's Block Events
        val queryToday = """
            SELECT $COL_SCREEN_TYPE, COUNT(*) as count 
            FROM $TABLE_BLOCK_EVENTS 
            WHERE $COL_TIMESTAMP >= ? 
            GROUP BY $COL_SCREEN_TYPE
        """.trimIndent()

        db.rawQuery(queryToday, arrayOf(startOfToday.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                val screenType = cursor.getString(0)
                val count = cursor.getInt(1)
                totalToday += count
                when (screenType) {
                    ScreenType.REELS.name -> reelsToday = count
                    ScreenType.EXPLORE.name -> exploreToday = count
                    ScreenType.HOME_FEED.name -> feedToday = count
                }
            }
        }

        // Query Weekly Total
        val queryWeek = """
            SELECT COUNT(*) 
            FROM $TABLE_BLOCK_EVENTS 
            WHERE $COL_TIMESTAMP >= ?
        """.trimIndent()

        db.rawQuery(queryWeek, arrayOf(startOfWeek.toString())).use { cursor ->
            if (cursor.moveToNext()) {
                weeklyTotal = cursor.getInt(0)
            }
        }

        // Query Sessions Today
        var igSessions = 0
        var dmSessions = 0
        val querySessions = """
            SELECT $COL_SESSION_TYPE, COUNT(*) 
            FROM $TABLE_SESSIONS 
            WHERE $COL_SESSION_TIMESTAMP >= ? 
            GROUP BY $COL_SESSION_TYPE
        """.trimIndent()

        db.rawQuery(querySessions, arrayOf(startOfToday.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                val type = cursor.getString(0)
                val count = cursor.getInt(1)
                if (type == "INSTAGRAM") igSessions = count
                if (type == "DM") dmSessions = count
            }
        }

        return StatisticsSummary(
            reelsBlockedToday = reelsToday,
            exploreBlockedToday = exploreToday,
            feedBlockedToday = feedToday,
            totalBlockedToday = totalToday,
            weeklyBlockedTotal = weeklyTotal,
            instagramSessionsCount = igSessions,
            dmSessionsCount = dmSessions
        )
    }

    fun clearAllData() {
        val db = writableDatabase
        db.delete(TABLE_BLOCK_EVENTS, null, null)
        db.delete(TABLE_SESSIONS, null, null)
    }
}
