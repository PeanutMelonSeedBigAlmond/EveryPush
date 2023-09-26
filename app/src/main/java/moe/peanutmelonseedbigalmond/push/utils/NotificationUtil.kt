package moe.peanutmelonseedbigalmond.push.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigPictureStyle
import moe.peanutmelonseedbigalmond.push.R

object NotificationUtil {
    private const val notificationChannelId = "high_system"
    fun setupNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(
                    notificationChannelId,
                    "notification",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    fun sendNotification(
        context: Context,
        title: String,
        content: String,
        id: Int? = null,
        time: Long = System.currentTimeMillis()
    ): Int {
        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle(title)
            .setContentText(content)
            .setWhen(time)
            .setSmallIcon(R.drawable.ic_notifications)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_notifications
                )
            )
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = id ?: time.toInt()
        nm.notify(notificationId, notification)
        return notificationId
    }

    fun sendNotificationWithImage(
        context: Context,
        title: String,
        content: Bitmap,
        id: Int? = null,
        time: Long = System.currentTimeMillis()
    ): Int {
        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle(title)
            .setStyle(
                BigPictureStyle()
                    .bigPicture(content)
            )
            .setWhen(time)
            .setContentText(context.getString(R.string.image_notification_brief))
            .setSmallIcon(R.drawable.ic_notifications)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_notifications
                )
            )
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = id ?: time.toInt()
        nm.notify(notificationId, notification)
        return notificationId
    }
}