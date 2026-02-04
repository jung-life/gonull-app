package app.gonull.util

import java.util.Calendar
import java.util.TimeZone

object DateHelper {
    /**
     * Returns the timestamp for the start of today (midnight) in the local timezone
     */
    fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Returns the date string in YYYY-MM-DD format for a given timestamp
     */
    fun getDateString(timestamp: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = timestamp
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    /**
     * Returns today's date string in YYYY-MM-DD format
     */
    fun getTodayString(): String = getDateString()

    /**
     * Returns the timestamp for the start of tomorrow (next midnight)
     */
    fun getStartOfTomorrow(): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Returns milliseconds until midnight (next day reset)
     */
    fun getMillisUntilMidnight(): Long {
        return getStartOfTomorrow() - System.currentTimeMillis()
    }

    /**
     * Returns the date string in YYYY-MM-DD format for a day offset from today.
     * @param daysOffset Negative for past days, positive for future days
     */
    fun getDateString(daysOffset: Int): String {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    /**
     * Returns the start of the week (Monday) date string
     */
    fun getStartOfWeekString(): String {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }
}
