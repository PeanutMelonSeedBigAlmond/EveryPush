package moe.peanutmelonseedbigalmond.push.utils

import android.content.Context
import moe.peanutmelonseedbigalmond.push.R
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

object DatetimeUtils {
    private val timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT)
    private val dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT)
    fun getDateString(context: Context, timeStamp: Long): String {
        val c1 = Calendar.getInstance()
        val c2 = Calendar.getInstance()
        c1.time = Date()
        c2.time = Date(timeStamp)
        c1.set(Calendar.HOUR_OF_DAY, 0)
        c1.set(Calendar.MINUTE, 0)
        c1.set(Calendar.SECOND, 0)
        c2.set(Calendar.HOUR_OF_DAY, 0)
        c2.set(Calendar.MINUTE, 0)
        c2.set(Calendar.SECOND, 0)

        val duration = c1.time.time.milliseconds - c2.time.time.milliseconds
        if (duration < 1.days) {
            context.getString(R.string.tips_yesterday_datetime)
            return context.getString(
                R.string.tips_today_datetime,
                timeFormatter.format(Date(timeStamp))
            )
        } else if (duration >= 1.days && duration < 2.days) {
            return context.getString(
                R.string.tips_yesterday_datetime,
                timeFormatter.format(Date(timeStamp))
            )
        } else if (duration >= 2.days && duration < 3.days) {
            return context.getString(
                R.string.tip_two_days_ago_datetime,
                timeFormatter.format(Date(timeStamp))
            )
        } else {
            return dateFormatter.format(Date(timeStamp))
        }
    }
}