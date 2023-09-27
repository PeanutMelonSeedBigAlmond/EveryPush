package moe.peanutmelonseedbigalmond.push.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.text.Spanned
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigPictureStyle
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationManagerCompat
import moe.peanutmelonseedbigalmond.push.App
import moe.peanutmelonseedbigalmond.push.R

object NotificationUtil {
    /**
     * 通知渠道
     * @property id String
     * @property notificationChannelName String
     * @constructor
     */
    private sealed class AppNotificationChannel(
        val id: String,
        val notificationChannelName: String
    ) {
        constructor(id: String, @StringRes notificationChannelName: Int) : this(
            id,
            App.context.getString(notificationChannelName)
        )

        object Markdown : AppNotificationChannel("markdown", R.string.notification_channel_markdown)
        object Text : AppNotificationChannel("text", R.string.notification_channel_text)
        object Image : AppNotificationChannel("image", R.string.notification_channel_image)
    }

    /**
     * 通知渠道组
     * @property id String
     * @property groupName String
     * @constructor
     */
    private sealed class AppNotificationChannelGroup(val id: String, val groupName: String) {
        constructor(id: String, @StringRes groupName: Int) : this(
            id,
            App.context.getString(groupName)
        )

        object ReceivedMessages : AppNotificationChannelGroup(
            "received_messages",
            R.string.notification_channel_group_received_messages
        )
    }

    fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = NotificationManagerCompat.from(App.context)
            notificationManager.createNotificationChannelGroup(AppNotificationChannelGroup.ReceivedMessages)
            notificationManager.createNotificationChannel(
                channel = AppNotificationChannel.Image,
                group = AppNotificationChannelGroup.ReceivedMessages
            )
            notificationManager.createNotificationChannel(
                channel = AppNotificationChannel.Text,
                group = AppNotificationChannelGroup.ReceivedMessages
            )
            notificationManager.createNotificationChannel(
                channel = AppNotificationChannel.Markdown,
                group = AppNotificationChannelGroup.ReceivedMessages
            )
        }
    }

    fun sendTextNotification(
        title: String,
        content: CharSequence,
        id: Int? = null,
        time: Long = System.currentTimeMillis()
    ): Int? {
        val notification = AppNotificationChannel.Text.notificationBuilder()
            .setContentTitle(title)
            .setContentText(content)
            .setWhen(time)
            .setSmallIcon(R.drawable.ic_notifications)
            .setLargeIcon(
                BitmapFactory.decodeResource(App.context.resources, R.drawable.ic_notifications)
            )
            .build()
        return sendNotification(notification, id ?: time.toInt())
    }

    fun sendMarkdownTextNotification(
        title: String,
        content: Spanned,
        id: Int? = null,
        time: Long = System.currentTimeMillis()
    ): Int? {
        val notification = AppNotificationChannel.Markdown.notificationBuilder()
            .setStyle(
                BigTextStyle()
                    .bigText(content)
            )
            .setContentText(content)
            .setContentTitle(title)
            .setWhen(time)
            .setSmallIcon(R.drawable.ic_notifications)
            .setLargeIcon(
                BitmapFactory.decodeResource(App.context.resources, R.drawable.ic_notifications)
            )
            .build()
        return sendNotification(notification, id ?: time.toInt())
    }

    fun sendNotificationWithImage(
        title: String,
        content: Bitmap,
        id: Int? = null,
        time: Long = System.currentTimeMillis()
    ): Int? {
        val notification = AppNotificationChannel.Image.notificationBuilder()
            .setContentTitle(title)
            .setLargeIcon(content)
            .setStyle(
                BigPictureStyle()
                    .bigPicture(content)
                    .bigLargeIcon(null as Bitmap?)
            )
            .setWhen(time)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentText(App.context.getString(R.string.image_notification_brief))
            .build()
        return sendNotification(notification, id ?: time.toInt())
    }

    /**
     * 发送/修改现有通知
     * @param notification Notification
     * @param id Int?
     * @return Int?
     */
    private fun sendNotification(notification: Notification, id: Int): Int? {
        return if (ActivityCompat.checkSelfPermission(
                App.context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("NotificationUtil", "sendTextNotification: 通知权限被拒绝")
            null
        } else {
            val nm = NotificationManagerCompat.from(App.context)
            nm.notify(id, notification)
            id
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun NotificationManagerCompat.createNotificationChannelGroup(group: AppNotificationChannelGroup) {
        this.createNotificationChannelGroup(NotificationChannelGroup(group.id, group.groupName))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun NotificationManagerCompat.createNotificationChannel(
        channel: AppNotificationChannel,
        importance: Int = NotificationManager.IMPORTANCE_HIGH,
        group: AppNotificationChannelGroup? = null
    ) {
        val mChannel = NotificationChannel(channel.id, channel.notificationChannelName, importance)
        if (group != null) {
            mChannel.group = group.id
        }
        this.createNotificationChannel(mChannel)
    }

    private fun AppNotificationChannel.notificationBuilder(): NotificationCompat.Builder {
        return NotificationCompat.Builder(App.context, this.id)
    }
}