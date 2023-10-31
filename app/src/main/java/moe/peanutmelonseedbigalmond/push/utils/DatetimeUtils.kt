package moe.peanutmelonseedbigalmond.push.utils

import android.content.Context
import moe.peanutmelonseedbigalmond.push.R
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

object DatetimeUtils {
    private val timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT)
    private val dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT)
    fun getDateString(context: Context, timeStamp: Long): String {
        val offset = Calendar.getInstance().get(Calendar.ZONE_OFFSET)
        val currentDay = (System.currentTimeMillis() + offset) / 86400_000
        val inputDay = (timeStamp + offset) / 86400_000

        return when (currentDay - inputDay) {
            0L -> context.getString(
                R.string.tips_today_datetime,
                timeFormatter.format(Date(timeStamp))
            )

            1L -> context.getString(
                R.string.tips_yesterday_datetime,
                timeFormatter.format(Date(timeStamp))
            )

            2L -> context.getString(
                R.string.tip_two_days_ago_datetime,
                timeFormatter.format(Date(timeStamp))
            )

            else -> dateFormatter.format(Date(timeStamp))
        }
    }
}