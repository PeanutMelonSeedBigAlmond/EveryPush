package moe.peanutmelonseedbigalmond.push.utils

import java.text.DateFormat
import java.util.Calendar
import java.util.Date

object DateUtils {
    fun timestampToString(timestamp: Long): String {
        val date = Date(timestamp)
        return DateFormat.getDateTimeInstance()
            .format(date)
    }

    fun timestampToTime(timestamp: Long): String {
        val date = Date(timestamp)
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(date)
    }

    fun timestampToDate(timestamp: Long): String {
        val currentDate = Calendar.getInstance()
        val inputDate = Calendar.getInstance()
        inputDate.timeInMillis = timestamp

        if (
            currentDate.get(Calendar.YEAR) == inputDate.get(Calendar.YEAR)
            &&currentDate.get(Calendar.DAY_OF_YEAR)==inputDate.get(Calendar.DAY_OF_YEAR)
        ) {
            return "Today"
        }
        currentDate.add(Calendar.DATE, -1)
        if (
            currentDate.get(Calendar.YEAR) == inputDate.get(Calendar.YEAR)
            &&currentDate.get(Calendar.DAY_OF_YEAR)==inputDate.get(Calendar.DAY_OF_YEAR)
        ) {
            return "Yesterday"
        }

        return DateFormat.getDateInstance().format(Date(timestamp))
    }
}